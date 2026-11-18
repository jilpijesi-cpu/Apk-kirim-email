package com.gswift.relay.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.gswift.relay.data.local.SecureStorageManager
import com.gswift.relay.data.model.AiProvidersResponse
import com.gswift.relay.data.model.AppPresets
import com.gswift.relay.data.model.ChatAiRequest
import com.gswift.relay.data.model.ChatAiResponse
import com.gswift.relay.data.model.ChatHistoryItem
import com.gswift.relay.data.model.ChatMessage
import com.gswift.relay.data.model.DomainLookupResponse
import com.gswift.relay.data.model.EmailTemplate
import com.gswift.relay.data.model.HealthResponse
import com.gswift.relay.data.model.KeyRotationResetResponse
import com.gswift.relay.data.model.KeyRotationStatusResponse
import com.gswift.relay.data.model.LastSentEmail
import com.gswift.relay.data.model.SendEmailRequest
import com.gswift.relay.data.model.SendEmailResponse
import com.gswift.relay.data.model.SmtpConfig
import com.gswift.relay.data.model.VerifySmtpRequest
import com.gswift.relay.data.model.VerifySmtpResponse
import com.gswift.relay.data.remote.GSwiftApiService
import com.gswift.relay.data.util.BankDraftEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class GSwiftRepository(
    private val api: GSwiftApiService,
    private val storage: SecureStorageManager
) {
    private val gson = Gson()

    fun loadSmtpConfig(): SmtpConfig = storage.loadSmtpConfig()
    fun saveSmtpConfig(config: SmtpConfig) = storage.saveSmtpConfig(config)

    fun loadServerBaseUrl(): String = storage.loadServerBaseUrl()
    fun saveServerBaseUrl(url: String) = storage.saveServerBaseUrl(url)

    fun loadLastSentEmail(): LastSentEmail? = storage.loadLastSentEmail()
    fun saveLastSentEmail(item: LastSentEmail) = storage.saveLastSentEmail(item)

    fun loadTemplates(): List<EmailTemplate> = storage.loadTemplates()
    fun saveTemplates(list: List<EmailTemplate>) = storage.saveTemplates(list)

    fun loadChatMessages(): List<ChatMessage> = storage.loadChatMessages()
    fun saveChatMessages(messages: List<ChatMessage>) = storage.saveChatMessages(messages)
    fun clearChatMessages() = storage.clearChatMessages()

    fun loadCustomCancelLink(): String = storage.loadCustomCancelLink()
    fun saveCustomCancelLink(link: String) = storage.saveCustomCancelLink(link)

    fun loadCustomDataRules(): String = storage.loadCustomDataRules()
    fun saveCustomDataRules(rules: String) = storage.saveCustomDataRules(rules)

    suspend fun checkHealth(): Result<HealthResponse> = safeCall {
        api.getHealth()
    }

    suspend fun fetchAiProviders(): Result<AiProvidersResponse> = safeCall {
        api.getAiProviders()
    }

    suspend fun fetchKeyRotationStatus(): Result<KeyRotationStatusResponse> = safeCall {
        api.getKeyRotationStatus()
    }

    suspend fun resetKeyRotation(): Result<KeyRotationResetResponse> = safeCall {
        api.resetKeyRotation()
    }

    suspend fun lookupDomain(domain: String): Result<DomainLookupResponse> = safeCall {
        api.lookupDomain(domain.trim())
    }

    suspend fun verifySmtp(config: SmtpConfig): Result<VerifySmtpResponse> = safeCall {
        val cleaned = sanitizeSmtpConfig(config)
        api.verifySmtp(VerifySmtpRequest(smtpConfig = cleaned))
    }

    private fun sanitizeSmtpConfig(config: SmtpConfig): SmtpConfig {
        val cleanUser = config.username.trim()
        val cleanPass = config.password.replace(Regex("[\\s\\u00A0\\u200B-\\u200D\\uFEFF]+"), "").trim()
        return config.copy(
            host = config.host.trim().ifBlank { "smtp.gmail.com" },
            port = config.port.trim().ifBlank { "587" },
            username = cleanUser,
            password = cleanPass,
            senderEmail = config.senderEmail.ifBlank { cleanUser }.trim()
        )
    }

    suspend fun sendEmail(
        to: String,
        subject: String,
        htmlOrText: String,
        config: SmtpConfig,
        customCancelLink: String = ""
    ): Result<SendEmailResponse> = withContext(Dispatchers.IO) {
        try {
            val cleanedCfg = sanitizeSmtpConfig(config)
            val nowMillis = System.currentTimeMillis()
            val dt = BankDraftEngine.buildWibDateTime(nowMillis)
            val bankKey = AppPresets.detectBankKey("$subject $htmlOrText")
            val uniqueRef = BankDraftEngine.generateUniqueBankReference(bankKey, nowMillis)

            val refreshedBody = BankDraftEngine.synchronizeDynamicFieldsInHtml(
                html = htmlOrText,
                newReference = uniqueRef,
                nowMillis = nowMillis,
                refreshRef = true
            ).let { body ->
                if (customCancelLink.isNotBlank()) {
                    body.replace(Regex("""\[(?:LINK_PEMBATALAN|CANCEL_LINK|LINK)]""", RegexOption.IGNORE_CASE), customCancelLink.trim())
                } else {
                    body
                }
            }

            val activeSubject = BankDraftEngine.synchronizeSubject(subject.trim(), uniqueRef, nowMillis)

            val isHtml = Regex("<[a-z][\\s\\S]*>", RegexOption.IGNORE_CASE).containsMatchIn(refreshedBody)
            val wrappedHtml = if (isHtml) {
                if (refreshedBody.contains("<html", true) || refreshedBody.contains("<!DOCTYPE", true) || refreshedBody.contains("<table", true)) {
                    refreshedBody
                } else {
                    """<div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; font-size: 14px; line-height: 1.6; color: #1e293b; max-width: 600px; margin: 0 auto;">$refreshedBody</div>"""
                }
            } else {
                """<div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; font-size: 14px; line-height: 1.6; color: #334155; max-width: 600px; margin: 0 auto;">${refreshedBody.replace("\n", "<br>")}</div>"""
            }

            val plainText = refreshedBody
                .replace(Regex("<style[^>]*>[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), "")
                .replace(Regex("<script[^>]*>[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
                .replace(Regex("<[^>]*>"), " ")
                .replace("&nbsp;", " ")
                .replace(Regex("\\s+"), " ")
                .trim()
                .ifEmpty { activeSubject }

            val response = api.sendEmail(
                SendEmailRequest(
                    to = to.trim(),
                    subject = activeSubject,
                    text = plainText,
                    html = wrappedHtml,
                    smtpConfig = if (cleanedCfg.username.isNotBlank()) cleanedCfg else null,
                    clientDateTime = dt,
                    clientRef = uniqueRef,
                    clientTimezone = "Asia/Jakarta"
                )
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                val errBody = response.errorBody()?.string()
                val msg = extractErrorMessage(errBody, response.code())
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Gagal terhubung ke server relay."))
        }
    }

    suspend fun sendAiChat(
        message: String,
        history: List<ChatMessage>,
        model: String?,
        bankKey: String?,
        scenario: String?,
        cancelLink: String,
        customDataRules: String
    ): Result<ChatAiResponse> = safeCall {
        val mappedHistory = history
            .filterNot { it.id == "welcome" || it.isError }
            .takeLast(12)
            .map { ChatHistoryItem(role = it.role, text = it.text) }

        val locale = Locale.forLanguageTag("id-ID")
        val tzFmt = SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", locale)
        tzFmt.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        val clientTimeStr = "${tzFmt.format(Date())} WIB"

        api.chatAi(
            ChatAiRequest(
                message = message,
                history = mappedHistory,
                clientTime = clientTimeStr,
                cancelLink = cancelLink.ifBlank { null },
                customDataRules = customDataRules.ifBlank { null },
                model = model,
                bankKey = bankKey,
                scenario = scenario
            )
        )
    }

    private fun extractErrorMessage(rawError: String?, httpCode: Int): String {
        if (rawError.isNullOrBlank()) return "HTTP $httpCode error"
        return runCatching {
            val obj = gson.fromJson(rawError, JsonObject::class.java)
            when {
                obj.has("error") && !obj.get("error").isJsonNull -> obj.get("error").asString
                obj.has("message") && !obj.get("message").isJsonNull -> obj.get("message").asString
                else -> rawError
            }
        }.getOrDefault(rawError)
    }

    private suspend inline fun <T> safeCall(
        crossinline block: suspend () -> Response<T>
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            val response = block()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                val rawError = response.errorBody()?.string()
                Result.failure(Exception(extractErrorMessage(rawError, response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Kesalahan jaringan."))
        }
    }
}
