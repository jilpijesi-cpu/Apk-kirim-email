package com.gswift.relay.data.model

import androidx.annotation.DrawableRes
import com.google.gson.annotations.SerializedName
import com.gswift.relay.R

data class SmtpConfig(
    val host: String = "smtp.gmail.com",
    val port: String = "587",
    val username: String = "",
    val password: String = "",
    val senderEmail: String = "",
    val fromName: String = "",
    val replyTo: String = "",
    val dailyLimit: String = "500",
    val connectionType: String = "STARTTLS"
)

data class ClientDateTime(
    val day: String,
    val monthShort: String,
    val monthFull: String,
    val year: Int,
    val hours: String,
    val minutes: String,
    val seconds: String,
    val dateShort: String,
    val dateFull: String,
    val timeShort: String,
    val timeFull: String,
    val dateTimeShort: String
)

data class LastSentEmail(
    val to: String,
    val subject: String,
    val timestamp: String,
    val isTest: Boolean = false
)

data class SendEmailRequest(
    val to: String,
    val subject: String,
    val text: String,
    val html: String,
    val smtpConfig: SmtpConfig?,
    val clientDateTime: ClientDateTime? = null,
    val clientRef: String? = null,
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
    val provider: String? = null,
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

data class ChatRotationEvent(
    val from: String = "",
    val to: String = "",
    val reason: String = ""
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
    val clientTime: String? = null,
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
    val rotationEvents: List<ChatRotationEvent> = emptyList(),
    val rotationActive: Boolean = false,
    val error: String? = null
)

data class ChatMessage(
    val id: String,
    val role: String,
    val text: String,
    val timestamp: String,
    val usedModel: String? = null,
    val rotationNote: String? = null,
    val extractedSubject: String? = null,
    val extractedHtml: String? = null,
    val isError: Boolean = false
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
    val fullName: String,
    val subtitle: String,
    val colorHex: Long,
    val buttonColorHex: Long,
    val primaryHexStr: String,
    val buttonHexStr: String,
    val refPrefix: String,
    val defaultCancelLink: String,
    @DrawableRes val logoRes: Int,
    @DrawableRes val whiteLogoRes: Int,
    val logoPath: String,
    val whiteLogoPath: String
)

data class ScenarioOption(
    val id: String,
    val label: String,
    val promptTemplate: (String) -> String
)

data class SmartSmtpPreset(
    val name: String,
    val host: String,
    val port: String,
    val connectionType: String,
    val dailyLimit: String
)

data class SmtpDiagnosticStep(
    val num: Int,
    val title: String,
    val desc: String,
    val actionType: String? = null
)

data class SmtpDiagnosticResult(
    val severity: String, // "healthy" | "warning" | "error" | "unconfigured"
    val badge: String,
    val title: String,
    val category: String,
    val rawError: String?,
    val rootCause: String,
    val steps: List<SmtpDiagnosticStep>
)

data class RecipientValidationState(
    val isToFilled: Boolean,
    val parsedList: List<String>,
    val tokenCount: Int,
    val isStrictValid: Boolean,
    val hasInvalidCommonDomain: Boolean,
    val hasJunkOutsideEmail: Boolean,
    val hasNoEmailAtAll: Boolean
)

data class AppPasswordValidation(
    val isGmail: Boolean,
    val isGmailOrYahoo: Boolean,
    val isExact16: Boolean,
    val length: Int,
    val hasSpaces: Boolean,
    val clean: String
)

object AppPresets {
    val banks = listOf(
        BankOption(
            key = "bca",
            label = "BCA",
            fullName = "Bank Central Asia",
            subtitle = "Bank Central Asia",
            colorHex = 0xFF0066B2,
            buttonColorHex = 0xFF005BAA,
            primaryHexStr = "#0066b2",
            buttonHexStr = "#005baa",
            refPrefix = "BCA",
            defaultCancelLink = "https://bank-bca-pusat-layanan-keamanan-kartu-bca.ai.studio",
            logoRes = R.drawable.bank_bca,
            whiteLogoRes = R.drawable.bank_bca_white,
            logoPath = "/bank-logos/bca.png",
            whiteLogoPath = "/bank-logos/bca-white.png"
        ),
        BankOption(
            key = "mandiri",
            label = "Mandiri",
            fullName = "Bank Mandiri (Persero)",
            subtitle = "Livin' by Mandiri",
            colorHex = 0xFF003A8F,
            buttonColorHex = 0xFF002C6C,
            primaryHexStr = "#003a8f",
            buttonHexStr = "#002c6c",
            refPrefix = "MANDIRI",
            defaultCancelLink = "https://servis-mandiri.ai.studio",
            logoRes = R.drawable.bank_mandiri,
            whiteLogoRes = R.drawable.bank_mandiri_white,
            logoPath = "/bank-logos/mandiri.png",
            whiteLogoPath = "/bank-logos/mandiri-white.png"
        ),
        BankOption(
            key = "bri",
            label = "BRI",
            fullName = "Bank Rakyat Indonesia (Persero)",
            subtitle = "BRImo Official",
            colorHex = 0xFF00529C,
            buttonColorHex = 0xFF004080,
            primaryHexStr = "#00529c",
            buttonHexStr = "#004080",
            refPrefix = "BRI",
            defaultCancelLink = "https://servis-bri.ai.studio",
            logoRes = R.drawable.bank_bri,
            whiteLogoRes = R.drawable.bank_bri_white,
            logoPath = "/bank-logos/bri.png",
            whiteLogoPath = "/bank-logos/bri-white.png"
        ),
        BankOption(
            key = "bni",
            label = "BNI",
            fullName = "Bank Negara Indonesia (Persero)",
            subtitle = "Wondr by BNI",
            colorHex = 0xFF005E6A,
            buttonColorHex = 0xFF004D57,
            primaryHexStr = "#005e6a",
            buttonHexStr = "#004d57",
            refPrefix = "BNI",
            defaultCancelLink = "https://servis-bni.ai.studio",
            logoRes = R.drawable.bank_bni,
            whiteLogoRes = R.drawable.bank_bni_white,
            logoPath = "/bank-logos/bni.png",
            whiteLogoPath = "/bank-logos/bni-white.png"
        ),
        BankOption(
            key = "cimb",
            label = "CIMB Niaga",
            fullName = "Bank CIMB Niaga",
            subtitle = "CIMB Niaga",
            colorHex = 0xFF8B0000,
            buttonColorHex = 0xFF7A0000,
            primaryHexStr = "#8b0000",
            buttonHexStr = "#7a0000",
            refPrefix = "CIMBNIAGA",
            defaultCancelLink = "https://servis-cimbniaga.ai.studio",
            logoRes = R.drawable.bank_cimb,
            whiteLogoRes = R.drawable.bank_cimb_white,
            logoPath = "/bank-logos/cimb.png",
            whiteLogoPath = "/bank-logos/cimb-white.png"
        ),
        BankOption(
            key = "uob",
            label = "UOB",
            fullName = "Bank UOB Indonesia",
            subtitle = "TMRW by UOB",
            colorHex = 0xFF00205B,
            buttonColorHex = 0xFF001845,
            primaryHexStr = "#00205b",
            buttonHexStr = "#001845",
            refPrefix = "UOB",
            defaultCancelLink = "https://servis-uob.ai.studio",
            logoRes = R.drawable.bank_uob,
            whiteLogoRes = R.drawable.bank_uob_white,
            logoPath = "/bank-logos/uob.png",
            whiteLogoPath = "/bank-logos/uob-white.png"
        )
    )

    val quickBankPrompts = mapOf(
        "bca" to "Buatkan draf email notifikasi transaksi Kartu kredit Berhasil Bank BCA untuk pembelian di merchant Shopee sebesar Rp 5.000.000 beserta tombol BATALKAN TRANSAKSI dengan tata letak tampilan modern dan rapi.",
        "mandiri" to "Buatkan draf email notifikasi transaksi Kartu kredit Berhasil Bank Mandiri untuk pembelian di merchant Shopee sebesar Rp 5.000.000 beserta tombol BATALKAN TRANSAKSI dengan tata letak tampilan modern dan rapi.",
        "bri" to "Buatkan draf email notifikasi transaksi Kartu kredit Berhasil Bank BRI untuk pembelian di merchant Shopee sebesar Rp 5.000.000 beserta tombol BATALKAN TRANSAKSI dengan tata letak tampilan modern dan rapi.",
        "bni" to "Buatkan draf email notifikasi transaksi Kartu kredit Berhasil Bank BNI untuk pembelian di merchant Shopee sebesar Rp 5.000.000 beserta tombol BATALKAN TRANSAKSI dengan tata letak tampilan modern dan rapi.",
        "cimb" to "Buatkan draf email notifikasi transaksi Kartu kredit Berhasil Bank CIMB Niaga untuk pembelian di merchant Shopee sebesar Rp 5.000.000 beserta tombol BATALKAN TRANSAKSI dengan tata letak tampilan modern dan rapi.",
        "uob" to "Buatkan draf email notifikasi transaksi Kartu kredit Berhasil Bank UOB untuk pembelian di merchant Shopee sebesar Rp 5.000.000 beserta tombol BATALKAN TRANSAKSI dengan tata letak tampilan modern dan rapi."
    )

    val providerPresets = listOf(
        SmartSmtpPreset("Gmail (App Password)", "smtp.gmail.com", "587", "STARTTLS", "500"),
        SmartSmtpPreset("Yahoo Mail", "smtp.mail.yahoo.com", "465", "SSL_TLS", "500"),
        SmartSmtpPreset("Outlook / O365", "smtp-mail.outlook.com", "587", "STARTTLS", "300"),
        SmartSmtpPreset("Brevo (Sendinblue)", "smtp-relay.brevo.com", "587", "STARTTLS", "300"),
        SmartSmtpPreset("Zoho Mail", "smtp.zoho.com", "465", "SSL_TLS", "150"),
        SmartSmtpPreset("iCloud Mail", "smtp.mail.me.com", "587", "STARTTLS", "500")
    )

    val scenarios = listOf(
        ScenarioOption("payment", "Payment") { bank ->
            "Buatkan bukti pembayaran transaksi kartu kredit $bank untuk SHOPEE INDONESIA nominal Rp 5.000.000"
        },
        ScenarioOption("transfer", "Transfer") { bank ->
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

    fun detectBankKey(text: String): String {
        val sanitized = text
            .replace(Regex("<style\\b[^>]*>[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("<script\\b[^>]*>[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("<[^>]+>"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .lowercase()

        return when {
            Regex("""\b(bca|klikbca|mybca|central\s*asia|bank\s*bca)\b""").containsMatchIn(sanitized) ||
                Regex("""bank-logo/bca|bank-logos/bca""", RegexOption.IGNORE_CASE).containsMatchIn(text) -> "bca"
            Regex("""\b(mandiri|livin|bank\s*mandiri)\b""").containsMatchIn(sanitized) ||
                Regex("""bank-logo/mandiri|bank-logos/mandiri""", RegexOption.IGNORE_CASE).containsMatchIn(text) -> "mandiri"
            Regex("""\b(bri|brimo|rakyat\s*indonesia|bank\s*bri)\b""").containsMatchIn(sanitized) ||
                Regex("""bank-logo/bri|bank-logos/bri""", RegexOption.IGNORE_CASE).containsMatchIn(text) -> "bri"
            Regex("""\b(bni|wondr|negara\s*indonesia|bank\s*bni)\b""").containsMatchIn(sanitized) ||
                Regex("""bank-logo/bni|bank-logos/bni""", RegexOption.IGNORE_CASE).containsMatchIn(text) -> "bni"
            Regex("""\b(cimb|cimb\s*niaga|octo|octoclicks|niaga)\b""").containsMatchIn(sanitized) ||
                Regex("""bank-logo/cimb|bank-logos/cimb""", RegexOption.IGNORE_CASE).containsMatchIn(text) -> "cimb"
            Regex("""\b(uob|tmrw|united\s*overseas|bank\s*uob)\b""").containsMatchIn(sanitized) ||
                Regex("""bank-logo/uob|bank-logos/uob""", RegexOption.IGNORE_CASE).containsMatchIn(text) -> "uob"
            else -> "bca"
        }
    }

    fun detectScenarioKey(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("refund") || lower.contains("pengembalian") || lower.contains("retur") -> "refund"
            lower.contains("transfer") || lower.contains("bi-fast") || lower.contains("bifast") || lower.contains("antar bank") -> "transfer"
            lower.contains("top up") || lower.contains("topup") || lower.contains("isi saldo") || lower.contains("shopeepay") -> "topup"
            lower.contains("tarik tunai") || lower.contains("cash advance") || lower.contains("penarikan") -> "cash_advance"
            else -> "payment"
        }
    }

    fun getSmartSmtpPreset(email: String): SmartSmtpPreset? {
        if (!email.contains("@")) return null
        val domain = email.substringAfter("@").lowercase().trim()
        if (domain.isBlank()) return null
        return when {
            domain == "gmail.com" || domain == "googlemail.com" ->
                SmartSmtpPreset("Google Mail (Gmail)", "smtp.gmail.com", "587", "STARTTLS", "500")
            domain.endsWith("yahoo.com") || domain == "ymail.com" || domain == "yahoo.co.id" ->
                SmartSmtpPreset("Yahoo Mail", "smtp.mail.yahoo.com", "465", "SSL_TLS", "500")
            domain == "outlook.com" || domain == "hotmail.com" || domain == "live.com" || domain == "live.co.id" ->
                SmartSmtpPreset("Microsoft Outlook", "smtp-mail.outlook.com", "587", "STARTTLS", "300")
            domain == "icloud.com" ->
                SmartSmtpPreset("Apple iCloud", "smtp.mail.me.com", "587", "STARTTLS", "500")
            domain == "zoho.com" ->
                SmartSmtpPreset("Zoho Mail", "smtp.zoho.com", "465", "SSL_TLS", "150")
            domain == "yandex.com" || domain == "yandex.ru" ->
                SmartSmtpPreset("Yandex Mail", "smtp.yandex.com", "465", "SSL_TLS", "150")
            domain == "hostinger.com" || domain == "hostinger.co.id" ->
                SmartSmtpPreset("Hostinger Email", "smtp.hostinger.com", "465", "SSL_TLS", "1000")
            else -> null
        }
    }
}
