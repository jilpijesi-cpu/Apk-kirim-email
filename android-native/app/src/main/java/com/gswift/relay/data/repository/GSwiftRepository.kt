package com.gswift.relay.data.repository

import com.google.gson.Gson
import com.gswift.relay.data.local.SecureStorageManager
import com.gswift.relay.data.model.AiProvidersResponse
import com.gswift.relay.data.model.AppPresets
import com.gswift.relay.data.model.ChatAiRequest
import com.gswift.relay.data.model.ChatAiResponse
import com.gswift.relay.data.model.ChatHistoryItem
import com.gswift.relay.data.model.ChatMessage
import com.gswift.relay.data.model.ClientDateTime
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.random.Random

class GSwiftRepository(
    private val api: GSwiftApiService,
    private val storage: SecureStorageManager
) {
    private val gson = Gson()

    fun loadSmtpConfig(): SmtpConfig = storage.loadSmtpConfig()
    fun saveSmtpConfig(config: SmtpConfig) = storage.saveSmtpConfig(config)

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
            username = cleanUser,
            password = cleanPass,
            senderEmail = config.senderEmail.ifBlank { cleanUser }.trim()
        )
    }

    private fun buildClientDateTime(): ClientDateTime {
        val tz = TimeZone.getTimeZone("Asia/Jakarta")
        val cal = Calendar.getInstance(tz)
        val monthsShort = arrayOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
        val monthsFull = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
        val day = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        val mIdx = cal.get(Calendar.MONTH)
        val monthNum = (mIdx + 1).toString().padStart(2, '0')
        val year = cal.get(Calendar.YEAR)
        val hours = cal.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
        val minutes = cal.get(Calendar.MINUTE).toString().padStart(2, '0')
        val seconds = cal.get(Calendar.SECOND).toString().padStart(2, '0')
        val monthShort = monthsShort[mIdx]
        val monthFull = monthsFull[mIdx]
        return ClientDateTime(
            day = day,
            monthShort = monthShort,
            monthFull = monthFull,
            year = year,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            dateShort = "$day $monthShort $year",
            dateFull = "$day $monthFull $year",
            timeShort = "$hours:$minutes",
            timeFull = "$hours:$minutes:$seconds",
            dateTimeShort = "$day/$monthNum/$year $hours:$minutes"
        )
    }

    suspend fun sendEmail(
        to: String,
        subject: String,
        htmlOrText: String,
        config: SmtpConfig
    ): Result<SendEmailResponse> = withContext(Dispatchers.IO) {
        try {
            val cleanedCfg = sanitizeSmtpConfig(config)
            val isHtml = Regex("<[a-z][\\s\\S]*>", RegexOption.IGNORE_CASE).containsMatchIn(htmlOrText)
            val wrappedHtml = if (isHtml) {
                if (htmlOrText.contains("<html", true) || htmlOrText.contains("<!DOCTYPE", true) || htmlOrText.contains("<table", true)) {
                    htmlOrText
                } else {
                    """<div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; font-size: 14px; line-height: 1.6; color: #1e293b; max-width: 600px; margin: 0 auto;">$htmlOrText</div>"""
                }
            } else {
                """<div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; font-size: 14px; line-height: 1.6; color: #334155; max-width: 600px; margin: 0 auto;">${htmlOrText.replace("\n", "<br>")}</div>"""
            }

            val plainText = htmlOrText
                .replace(Regex("<style[^>]*>[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), "")
                .replace(Regex("<script[^>]*>[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
                .replace(Regex("<[^>]*>"), " ")
                .replace("&nbsp;", " ")
                .replace(Regex("\\s+"), " ")
                .trim()
                .ifEmpty { subject.trim() }

            val bankKey = AppPresets.detectBankKey("$subject $htmlOrText")
            val bankPrefix = AppPresets.banks.find { it.key == bankKey }?.refPrefix ?: "BCA"
            val uniqueRef = "$bankPrefix-${Random.nextLong(10000000000L, 99999999999L)}"

            val response = api.sendEmail(
                SendEmailRequest(
                    to = to.trim(),
                    subject = subject.trim(),
                    text = plainText,
                    html = wrappedHtml,
                    smtpConfig = cleanedCfg,
                    clientDateTime = buildClientDateTime(),
                    clientRef = uniqueRef,
                    clientTimezone = "Asia/Jakarta"
                )
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                val errBody = response.errorBody()?.string()
                val parsed = runCatching {
                    gson.fromJson(errBody, SendEmailResponse::class.java)
                }.getOrNull()
                Result.failure(
                    Exception(parsed?.error ?: "HTTP ${response.code()}: Gagal mengirim email.")
                )
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
        val mappedHistory = history.takeLast(12).map {
            ChatHistoryItem(role = it.role, text = it.text)
        }
        val tzFmt = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
        tzFmt.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        api.chatAi(
            ChatAiRequest(
                message = message,
                history = mappedHistory,
                cancelLink = cancelLink.ifBlank { null },
                customDataRules = customDataRules.ifBlank { null },
                model = model,
                bankKey = bankKey,
                scenario = scenario
            )
        )
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
                Result.failure(
                    Exception(
                        if (!rawError.isNullOrBlank()) rawError
                        else "HTTP ${response.code()} error"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Kesalahan jaringan."))
        }
    }
}
