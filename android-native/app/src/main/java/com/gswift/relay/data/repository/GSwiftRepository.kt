package com.gswift.relay.data.repository

import com.google.gson.Gson
import com.gswift.relay.data.local.SecureStorageManager
import com.gswift.relay.data.model.AiProvidersResponse
import com.gswift.relay.data.model.ChatAiRequest
import com.gswift.relay.data.model.ChatAiResponse
import com.gswift.relay.data.model.ChatHistoryItem
import com.gswift.relay.data.model.ChatMessage
import com.gswift.relay.data.model.DomainLookupResponse
import com.gswift.relay.data.model.EmailTemplate
import com.gswift.relay.data.model.HealthResponse
import com.gswift.relay.data.model.KeyRotationResetResponse
import com.gswift.relay.data.model.KeyRotationStatusResponse
import com.gswift.relay.data.model.SendEmailRequest
import com.gswift.relay.data.model.SendEmailResponse
import com.gswift.relay.data.model.SmtpConfig
import com.gswift.relay.data.model.VerifySmtpRequest
import com.gswift.relay.data.model.VerifySmtpResponse
import com.gswift.relay.data.remote.GSwiftApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class GSwiftRepository(
    private val api: GSwiftApiService,
    private val storage: SecureStorageManager
) {
    private val gson = Gson()

    fun loadSmtpConfig(): SmtpConfig = storage.loadSmtpConfig()
    fun saveSmtpConfig(config: SmtpConfig) = storage.saveSmtpConfig(config)

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
        api.verifySmtp(VerifySmtpRequest(smtpConfig = config))
    }

    suspend fun sendEmail(
        to: String,
        subject: String,
        htmlOrText: String,
        config: SmtpConfig
    ): Result<SendEmailResponse> = withContext(Dispatchers.IO) {
        try {
            val plainText = htmlOrText
                .replace(Regex("<[^>]*>"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
                .ifEmpty { "G-Swift Relay Message" }

            val response = api.sendEmail(
                SendEmailRequest(
                    to = to.trim(),
                    subject = subject.trim(),
                    text = plainText,
                    html = htmlOrText,
                    smtpConfig = config
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
