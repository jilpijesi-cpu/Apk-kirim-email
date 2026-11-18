package com.gswift.relay.data.model

import com.google.gson.annotations.SerializedName

data class SmtpConfig(
    val host: String = "smtp.gmail.com",
    val port: String = "465",
    val username: String = "",
    val password: String = "",
    val senderEmail: String = "",
    val fromName: String = "",
    val replyTo: String = "",
    val dailyLimit: String = "200",
    val connectionType: String = "SSL_TLS"
)

data class SendEmailRequest(
    val to: String,
    val subject: String,
    val text: String,
    val html: String,
    val smtpConfig: SmtpConfig,
    val clientTimezone: String = "Asia/Jakarta"
)

data class SendEmailResponse(
    val success: Boolean = false,
    val messageId: String? = null,
    val response: String? = null,
    val error: String? = null,
    val code: String? = null,
    val isAuthError: Boolean = false
)

data class VerifySmtpRequest(
    val smtpConfig: SmtpConfig
)

data class VerifySmtpResponse(
    val success: Boolean = false,
    val connected: Boolean = false,
    val host: String? = null,
    val port: Int? = null,
    val username: String? = null,
    val message: String? = null,
    val error: String? = null
)

data class DomainLookupResponse(
    val success: Boolean = false,
    val layer: String? = null,
    val smtp: SmtpLookupInfo? = null,
    val logs: List<String> = emptyList(),
    val error: String? = null
)

data class SmtpLookupInfo(
    val host: String = "",
    val port: String = "465",
    val connectionType: String = "SSL_TLS",
    val dailyLimit: String = "500",
    val name: String = ""
)

data class HealthResponse(
    val status: String = "",
    @SerializedName("smtp_configured")
    val smtpConfigured: Boolean = false,
    val environment: String = ""
)

data class AiProvidersResponse(
    val activeProviders: List<AiProviderInfo> = emptyList(),
    val deprecatedOrInactiveProviders: List<InactiveProviderInfo> = emptyList()
)

data class AiProviderInfo(
    val id: String = "",
    val name: String = "",
    val status: String = "",
    val models: List<String> = emptyList(),
    val keyMasked: String? = null
)

data class InactiveProviderInfo(
    val id: String = "",
    val status: String = "",
    val reason: String = ""
)

data class KeyRotationStatusResponse(
    @SerializedName("activeKeysCount")
    val activeKeys: Int = 0,
    val totalKeys: Int = 0,
    val keys: List<KeyEntryStatus> = emptyList(),
    val recentRotations: List<RotationEvent> = emptyList()
)

data class KeyEntryStatus(
    val id: String = "",
    val provider: String = "",
    val maskedKey: String = "",
    val priority: Int = 10,
    val consecutiveErrors: Int = 0,
    @SerializedName("cooldownRemainingSeconds")
    val cooldownRemainingSec: Int = 0,
    val totalSuccess: Int = 0,
    val totalErrors: Int = 0,
    val lastErrorReason: String? = null,
    val status: String = "active"
)

data class RotationEvent(
    val timestamp: String = "",
    val fromProvider: String = "",
    val toProvider: String = "",
    val reason: String = "",
    val fromKeyMasked: String? = null
)

data class KeyRotationResetResponse(
    val success: Boolean = false,
    val message: String = "",
    val status: KeyRotationStatusResponse? = null
)

data class ChatHistoryItem(
    val role: String,
    val text: String
)

data class ChatAiRequest(
    val message: String,
    val history: List<ChatHistoryItem> = emptyList(),
    val cancelLink: String? = null,
    val customDataRules: String? = null,
    val model: String? = null,
    val bankKey: String? = null,
    val scenario: String? = null
)

data class ChatAiResponse(
    val text: String? = null,
    val usedModel: String? = null,
    val requestedModel: String? = null,
    val rotationEvents: List<RotationEvent> = emptyList(),
    val rotationActive: Boolean = false,
    val error: String? = null
)

data class ChatMessage(
    val id: String,
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: String,
    val usedModel: String? = null,
    val extractedSubject: String? = null,
    val extractedHtml: String? = null
)

data class EmailTemplate(
    val id: String,
    val name: String,
    val category: String,
    val subject: String,
    val message: String
)

data class LogEntry(
    val timestamp: String,
    val type: LogType,
    val message: String
)

enum class LogType {
    INFO, SUCCESS, WARNING, ERROR
}

enum class SmtpConnectionState {
    UNCONFIGURED, CHECKING, CONNECTED, DISCONNECTED
}

data class BankOption(
    val key: String,
    val label: String,
    val colorHex: Long
)

data class ScenarioOption(
    val id: String,
    val label: String,
    val promptTemplate: (String) -> String
)

object AppPresets {
    val banks = listOf(
        BankOption("bca", "BCA", 0xFF005BAA),
        BankOption("mandiri", "Mandiri", 0xFF003A8F),
        BankOption("bri", "BRI", 0xFF00529C),
        BankOption("bni", "BNI", 0xFF005E6A),
        BankOption("cimb", "CIMB Niaga", 0xFF8B0000),
        BankOption("uob", "UOB", 0xFF00205B)
    )

    val scenarios = listOf(
        ScenarioOption("payment", "Pembayaran") { bank ->
            "Buatkan bukti pembayaran transaksi kartu kredit $bank untuk SHOPEE INDONESIA nominal Rp 5.000.000"
        },
        ScenarioOption("transfer", "Transfer BI-Fast") { bank ->
            "Buatkan bukti transfer dana BI-Fast $bank ke SHOPEE INDONESIA nominal Rp 5.000.000"
        },
        ScenarioOption("refund", "Refund") { bank ->
            "Buatkan bukti pengembalian dana (refund) dari SHOPEE INDONESIA ke kartu kredit $bank nominal Rp 5.000.000"
        },
        ScenarioOption("topup", "Top Up") { bank ->
            "Buatkan bukti isi saldo (Top Up) ShopeePay melalui $bank nominal Rp 5.000.000"
        },
        ScenarioOption("cash_advance", "Tarik Tunai") { bank ->
            "Buatkan bukti tarik tunai kartu kredit $bank di ATM nominal Rp 5.000.000"
        }
    )
}
