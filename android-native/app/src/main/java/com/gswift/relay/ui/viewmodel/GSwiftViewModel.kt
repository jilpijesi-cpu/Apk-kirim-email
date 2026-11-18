package com.gswift.relay.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gswift.relay.data.model.AiProviderInfo
import com.gswift.relay.data.model.AppPresets
import com.gswift.relay.data.model.ChatMessage
import com.gswift.relay.data.model.EmailTemplate
import com.gswift.relay.data.model.KeyRotationStatusResponse
import com.gswift.relay.data.model.LastSentEmail
import com.gswift.relay.data.model.LogEntry
import com.gswift.relay.data.model.LogType
import com.gswift.relay.data.model.SmtpConfig
import com.gswift.relay.data.model.SmtpConnectionState
import com.gswift.relay.data.network.ConnectivityObserver
import com.gswift.relay.data.repository.GSwiftRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

data class DeliveryBanner(
    val isSuccess: Boolean,
    val title: String,
    val subtitle: String,
    val detail: String,
    val tip: String? = null,
    val elapsedMs: Long = 0L
)

data class MainUiState(
    val isOnline: Boolean = true,
    val backendOnline: Boolean = false,
    val smtpConfig: SmtpConfig = SmtpConfig(),
    val smtpState: SmtpConnectionState = SmtpConnectionState.UNCONFIGURED,
    val smtpStatusMessage: String = "Belum dikonfigurasi",
    val isVerifyingSmtp: Boolean = false,
    val isLookingUpDomain: Boolean = false,
    val recipientInput: String = "",
    val subjectInput: String = "",
    val messageInput: String = "",
    val lastSentEmail: LastSentEmail? = null,
    val isSendingEmail: Boolean = false,
    val deliveryBanner: DeliveryBanner? = null,
    val aiMessages: List<ChatMessage> = emptyList(),
    val aiProviders: List<AiProviderInfo> = emptyList(),
    val selectedAiModel: String? = null,
    val selectedBankKey: String = "bca",
    val selectedScenario: String = "payment",
    val customCancelLink: String = "",
    val customDataRules: String = "",
    val isAiThinking: Boolean = false,
    val templates: List<EmailTemplate> = emptyList(),
    val logs: List<LogEntry> = emptyList(),
    val keyRotationStatus: KeyRotationStatusResponse? = null,
    val isRefreshingKeys: Boolean = false,
    val snackbarMessage: String? = null
)

class GSwiftViewModel(
    private val repository: GSwiftRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        observeConnectivity()
        checkHealthAndProviders()
    }

    private fun nowTime(): String {
        val fmt = SimpleDateFormat("HH:mm:ss", Locale.forLanguageTag("id-ID"))
        fmt.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        return fmt.format(Date())
    }

    private fun nowShortTime(): String {
        val fmt = SimpleDateFormat("HH:mm", Locale.forLanguageTag("id-ID"))
        fmt.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        return fmt.format(Date())
    }

    private fun addLog(type: LogType, message: String) {
        val entry = LogEntry(timestamp = nowTime(), type = type, message = message)
        _uiState.update { state ->
            state.copy(logs = (listOf(entry) + state.logs).take(150))
        }
    }

    private fun loadInitialData() {
        val savedSmtp = repository.loadSmtpConfig()
        val savedTemplates = repository.loadTemplates()
        val savedLastSent = repository.loadLastSentEmail()
        val savedChat = repository.loadChatMessages().ifEmpty {
            listOf(
                ChatMessage(
                    id = "welcome",
                    role = "model",
                    text = "Halo! Saya Asisten AI Draf Email. Tuliskan pesan, topik, atau konsep email apa pun yang ingin Anda buat — saya akan merancang draf email HTML yang kreatif, unik, dan responsif untuk Anda!",
                    timestamp = nowTime()
                )
            )
        }
        val cancelLink = repository.loadCustomCancelLink()
        val dataRules = repository.loadCustomDataRules()

        _uiState.update {
            it.copy(
                smtpConfig = savedSmtp,
                smtpState = if (savedSmtp.username.isNotBlank() && savedSmtp.password.isNotBlank()) {
                    SmtpConnectionState.CHECKING
                } else {
                    SmtpConnectionState.UNCONFIGURED
                },
                lastSentEmail = savedLastSent,
                templates = savedTemplates,
                aiMessages = savedChat,
                customCancelLink = cancelLink,
                customDataRules = dataRules
            )
        }

        if (savedSmtp.username.isNotBlank() && savedSmtp.password.isNotBlank()) {
            verifySmtpConnection(savedSmtp, silent = true)
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityObserver.observeIsOnline().collect { online ->
                _uiState.update { it.copy(isOnline = online) }
                if (!online) {
                    addLog(LogType.WARNING, "Perangkat sedang offline. Menggunakan penyimpanan lokal.")
                }
            }
        }
    }

    fun checkHealthAndProviders() {
        viewModelScope.launch {
            repository.checkHealth().onSuccess {
                _uiState.update { state -> state.copy(backendOnline = true) }
                addLog(LogType.SUCCESS, "Terhubung ke backend G-Swift Relay (${it.environment}).")
            }.onFailure { err ->
                _uiState.update { state -> state.copy(backendOnline = false) }
                addLog(LogType.ERROR, "Backend tidak terjangkau: ${err.message}")
            }

            repository.fetchAiProviders().onSuccess { resp ->
                val defaultModel = resp.activeProviders.firstOrNull()?.models?.firstOrNull()
                _uiState.update { state ->
                    state.copy(
                        aiProviders = resp.activeProviders,
                        selectedAiModel = state.selectedAiModel ?: defaultModel
                    )
                }
            }

            refreshKeyRotationStatus()
        }
    }

    fun updateMailFields(to: String? = null, subject: String? = null, message: String? = null) {
        _uiState.update {
            it.copy(
                recipientInput = to ?: it.recipientInput,
                subjectInput = subject ?: it.subjectInput,
                messageInput = message ?: it.messageInput
            )
        }
    }

    fun applyLastSentEmail() {
        val last = _uiState.value.lastSentEmail ?: return
        _uiState.update {
            it.copy(
                recipientInput = last.to,
                subjectInput = last.subject.ifBlank { it.subjectInput },
                snackbarMessage = "Memuat kembali: ${last.to}"
            )
        }
        addLog(LogType.INFO, "Memuat kembali email terakhir: ${last.to}")
    }

    fun clearMailForm() {
        _uiState.update {
            it.copy(recipientInput = "", subjectInput = "", messageInput = "", deliveryBanner = null)
        }
        addLog(LogType.INFO, "Formulir pengiriman email dibersihkan.")
    }

    fun dismissDeliveryBanner() {
        _uiState.update { it.copy(deliveryBanner = null) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    /**
     * 1:1 Auto-Filter extraction matching App.tsx extractEmailAddresses()
     */
    fun extractCleanEmails(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        val sanitized = raw
            .replace(Regex("mailto:\\s*", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("%40", RegexOption.IGNORE_CASE), "@")
            .replace(Regex("\\[([^\\]]+)]\\(([^)]+)\\)"), "$1 $2")

        val regex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,63}")
        val list = mutableListOf<String>()
        regex.findAll(sanitized).forEach { match ->
            val clean = match.value.trim().lowercase()
            val domain = clean.substringAfter("@", "")
            if (domain.isNotEmpty() && !domain.startsWith("-") && !domain.endsWith("-") && !domain.contains("..")) {
                if (!list.contains(clean)) {
                    list.add(clean)
                }
            }
        }
        return list
    }

    fun sendEmail() {
        val state = _uiState.value
        if (!state.isOnline) {
            _uiState.update { it.copy(snackbarMessage = "Tidak ada koneksi internet.") }
            return
        }
        val cleanEmails = extractCleanEmails(state.recipientInput)
        if (cleanEmails.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "Masukkan minimal satu alamat email penerima yang valid.") }
            return
        }
        if (state.subjectInput.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "Silakan masukkan subjek email.") }
            return
        }
        if (state.messageInput.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "Silakan lengkapi pesan draf email sebelum mengirim.") }
            return
        }
        if (state.smtpConfig.username.isBlank() || state.smtpConfig.password.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "Konfigurasi akun pengirim SMTP di tab Akun terlebih dahulu.") }
            return
        }

        viewModelScope.launch {
            val startMs = System.currentTimeMillis()
            _uiState.update { it.copy(isSendingEmail = true, deliveryBanner = null) }
            val joinedRecipients = cleanEmails.joinToString(", ")
            addLog(LogType.INFO, "Memulai pengiriman paket ke $joinedRecipients...")

            repository.sendEmail(
                to = joinedRecipients,
                subject = state.subjectInput,
                htmlOrText = state.messageInput,
                config = state.smtpConfig
            ).onSuccess { resp ->
                val elapsed = System.currentTimeMillis() - startMs
                val lastItem = LastSentEmail(
                    to = joinedRecipients,
                    subject = state.subjectInput,
                    timestamp = nowShortTime()
                )
                repository.saveLastSentEmail(lastItem)
                addLog(LogType.SUCCESS, "Email terkirim dalam ${elapsed}ms! ID: ${resp.messageId ?: "-"}")
                _uiState.update {
                    it.copy(
                        isSendingEmail = false,
                        smtpState = SmtpConnectionState.CONNECTED,
                        lastSentEmail = lastItem,
                        deliveryBanner = DeliveryBanner(
                            isSuccess = true,
                            title = "Pengiriman Berhasil",
                            subtitle = "Exploit Success • Message Dispatched",
                            detail = "Penerima: $joinedRecipients\nMessage ID: ${resp.messageId ?: "Terkonfirmasi"}\nWaktu Proses: ${elapsed}ms",
                            elapsedMs = elapsed
                        )
                    )
                }
            }.onFailure { err ->
                val elapsed = System.currentTimeMillis() - startMs
                val raw = err.message ?: "Gagal mengirim email"
                val tip = when {
                    raw.contains("535") || raw.contains("App Password", ignoreCase = true) || raw.contains("BadCredentials", ignoreCase = true) ->
                        "Server SMTP menolak sandi. Untuk Gmail/Yahoo, aktifkan Verifikasi 2 Langkah lalu buat 16 karakter Sandi Aplikasi tanpa spasi di myaccount.google.com/apppasswords."
                    raw.contains("timeout", ignoreCase = true) || raw.contains("ECONNREFUSED", ignoreCase = true) ->
                        "Port SMTP terblokir atau tidak merespons. Gunakan Port 587 (STARTTLS) atau Port 465 (SSL/TLS)."
                    else -> null
                }
                addLog(LogType.ERROR, "Relay failed: $raw")
                _uiState.update {
                    it.copy(
                        isSendingEmail = false,
                        smtpState = SmtpConnectionState.DISCONNECTED,
                        deliveryBanner = DeliveryBanner(
                            isSuccess = false,
                            title = "Pengiriman Gagal",
                            subtitle = "Exploit Failed • Delivery Interrupted",
                            detail = raw,
                            tip = tip,
                            elapsedMs = elapsed
                        )
                    )
                }
            }
        }
    }

    fun sendTestEmail(targetEmail: String, template: EmailTemplate) {
        val state = _uiState.value
        if (targetEmail.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingEmail = true) }
            repository.sendEmail(
                to = targetEmail,
                subject = template.subject,
                htmlOrText = template.message,
                config = state.smtpConfig
            ).onSuccess {
                addLog(LogType.SUCCESS, "[UJICOBA] Email '${template.name}' terkirim ke $targetEmail.")
                _uiState.update {
                    it.copy(isSendingEmail = false, snackbarMessage = "Email tes terkirim ke $targetEmail")
                }
            }.onFailure { err ->
                addLog(LogType.ERROR, "[UJICOBA GAGAL] ${err.message}")
                _uiState.update {
                    it.copy(isSendingEmail = false, snackbarMessage = "Gagal: ${err.message}")
                }
            }
        }
    }

    fun selectBank(bankKey: String) {
        _uiState.update { it.copy(selectedBankKey = bankKey) }
    }

    fun selectScenario(scenarioId: String) {
        _uiState.update { it.copy(selectedScenario = scenarioId) }
    }

    fun selectAiModel(model: String) {
        _uiState.update { it.copy(selectedAiModel = model) }
    }

    fun updateAiCustomSettings(cancelLink: String, dataRules: String) {
        repository.saveCustomCancelLink(cancelLink)
        repository.saveCustomDataRules(dataRules)
        _uiState.update {
            it.copy(
                customCancelLink = cancelLink,
                customDataRules = dataRules,
                snackbarMessage = "Aturan AI tersimpan."
            )
        }
    }

    fun sendAiPrompt(promptText: String) {
        val cleanPrompt = promptText.trim()
        if (cleanPrompt.isEmpty()) return
        val state = _uiState.value
        if (!state.isOnline) {
            _uiState.update { it.copy(snackbarMessage = "AI membutuhkan koneksi internet.") }
            return
        }

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = "user",
            text = cleanPrompt,
            timestamp = nowTime()
        )
        val updatedHistory = state.aiMessages + userMsg
        _uiState.update { it.copy(aiMessages = updatedHistory, isAiThinking = true) }
        repository.saveChatMessages(updatedHistory)

        viewModelScope.launch {
            repository.sendAiChat(
                message = cleanPrompt,
                history = state.aiMessages,
                model = state.selectedAiModel,
                bankKey = state.selectedBankKey,
                scenario = state.selectedScenario,
                cancelLink = state.customCancelLink,
                customDataRules = state.customDataRules
            ).onSuccess { resp ->
                val rawText = resp.text ?: "Tidak ada respons dari model AI."
                val extractedSubject = extractSubjectFromAi(rawText)
                val extractedHtml = extractHtmlFromAi(rawText)

                if (resp.rotationActive) {
                    addLog(LogType.WARNING, "Rotasi API Key AI aktif (${resp.usedModel ?: "Auto"}).")
                } else {
                    addLog(LogType.SUCCESS, "AI selesai merancang draf (${resp.usedModel ?: "Model"}).")
                }

                val aiMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = "model",
                    text = rawText,
                    timestamp = nowTime(),
                    usedModel = resp.usedModel,
                    extractedSubject = extractedSubject,
                    extractedHtml = extractedHtml
                )
                val nextMessages = _uiState.value.aiMessages + aiMsg
                repository.saveChatMessages(nextMessages)
                _uiState.update {
                    it.copy(aiMessages = nextMessages, isAiThinking = false)
                }
                refreshKeyRotationStatus()
            }.onFailure { err ->
                addLog(LogType.ERROR, "Chat AI gagal: ${err.message}")
                val errMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = "model",
                    text = "Maaf, permintaan gagal diproses: ${err.message}",
                    timestamp = nowTime()
                )
                val nextMessages = _uiState.value.aiMessages + errMsg
                _uiState.update {
                    it.copy(aiMessages = nextMessages, isAiThinking = false)
                }
            }
        }
    }

    fun clearAiChat() {
        repository.clearChatMessages()
        _uiState.update {
            it.copy(
                aiMessages = listOf(
                    ChatMessage(
                        id = "welcome",
                        role = "model",
                        text = "Riwayat percakapan dibersihkan. Siap merancang draf email baru!",
                        timestamp = nowTime()
                    )
                )
            )
        }
    }

    fun applyAiDraftToSend(subject: String?, htmlOrBody: String) {
        _uiState.update {
            it.copy(
                subjectInput = subject ?: it.subjectInput,
                messageInput = htmlOrBody,
                snackbarMessage = "Draf AI diterapkan ke tab Kirim."
            )
        }
        addLog(LogType.INFO, "Draf AI diterapkan ke formulir Kirim.")
    }

    private fun extractSubjectFromAi(raw: String): String? {
        val patterns = listOf(
            Regex("""Subjek Rekomendasi[:*`\s]+`([^`]+)`""", RegexOption.IGNORE_CASE),
            Regex("""Subjek[:*\s]+([^\n]+)""", RegexOption.IGNORE_CASE)
        )
        for (p in patterns) {
            val match = p.find(raw)
            if (match != null) {
                return match.groupValues[1].trim().trim('`', '"', '*')
            }
        }
        return null
    }

    private fun extractHtmlFromAi(raw: String): String? {
        val codeBlock = Regex("""```html\s*([\s\S]*?)```""", RegexOption.IGNORE_CASE).find(raw)
        if (codeBlock != null) return codeBlock.groupValues[1].trim()
        if (raw.contains("<!DOCTYPE html", ignoreCase = true) || raw.contains("<table", ignoreCase = true)) {
            return raw
        }
        return null
    }

    fun saveTemplate(name: String, category: String, subject: String, message: String, editingId: String? = null) {
        val list = _uiState.value.templates.toMutableList()
        if (editingId != null) {
            val idx = list.indexOfFirst { it.id == editingId }
            if (idx >= 0) {
                list[idx] = EmailTemplate(editingId, name, category, subject, message)
            }
        } else {
            list.add(0, EmailTemplate(UUID.randomUUID().toString(), name, category, subject, message))
        }
        repository.saveTemplates(list)
        _uiState.update {
            it.copy(templates = list, snackbarMessage = "Template '$name' tersimpan.")
        }
        addLog(LogType.SUCCESS, "Template '$name' disimpan secara lokal.")
    }

    fun deleteTemplate(id: String) {
        val list = _uiState.value.templates.filterNot { it.id == id }
        repository.saveTemplates(list)
        _uiState.update { it.copy(templates = list, snackbarMessage = "Template dihapus.") }
    }

    fun applyTemplateToSend(template: EmailTemplate) {
        _uiState.update {
            it.copy(
                subjectInput = template.subject,
                messageInput = template.message,
                snackbarMessage = "Template '${template.name}' diterapkan."
            )
        }
    }

    fun updateSmtpConfig(config: SmtpConfig) {
        val preset = AppPresets.getSmartSmtpPreset(config.username)
        val merged = if (preset != null && config.host.isBlank()) {
            config.copy(
                host = preset.host,
                port = preset.port,
                connectionType = preset.connectionType,
                dailyLimit = preset.dailyLimit
            )
        } else {
            config
        }
        _uiState.update { it.copy(smtpConfig = merged) }
    }

    fun saveAndVerifySmtp(config: SmtpConfig) {
        val preset = AppPresets.getSmartSmtpPreset(config.username)
        val normalized = config.copy(
            host = config.host.ifBlank { preset?.host ?: "smtp.gmail.com" },
            port = config.port.ifBlank { preset?.port ?: "587" },
            username = config.username.trim(),
            senderEmail = config.senderEmail.ifBlank { config.username.trim() },
            password = config.password.replace(Regex("[\\s\\u00A0]+"), "").trim()
        )
        repository.saveSmtpConfig(normalized)
        _uiState.update { it.copy(smtpConfig = normalized) }
        verifySmtpConnection(normalized, silent = false)
    }

    fun verifySmtpConnection(config: SmtpConfig = _uiState.value.smtpConfig, silent: Boolean = false) {
        if (config.username.isBlank() || config.password.isBlank()) {
            _uiState.update {
                it.copy(
                    smtpState = SmtpConnectionState.UNCONFIGURED,
                    smtpStatusMessage = "Username atau Password SMTP kosong"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isVerifyingSmtp = true,
                    smtpState = SmtpConnectionState.CHECKING,
                    smtpStatusMessage = "Memverifikasi ${config.host}..."
                )
            }
            repository.verifySmtp(config).onSuccess { resp ->
                if (resp.connected) {
                    addLog(LogType.SUCCESS, "SMTP terhubung: ${resp.host}:${resp.port} (${resp.username})")
                    _uiState.update {
                        it.copy(
                            isVerifyingSmtp = false,
                            smtpState = SmtpConnectionState.CONNECTED,
                            smtpStatusMessage = resp.message ?: "Terhubung ke ${resp.host}",
                            snackbarMessage = if (!silent) "Koneksi SMTP berhasil diverifikasi!" else null
                        )
                    }
                } else {
                    val err = resp.error ?: "Koneksi SMTP ditolak."
                    addLog(LogType.ERROR, "Verifikasi SMTP gagal: $err")
                    _uiState.update {
                        it.copy(
                            isVerifyingSmtp = false,
                            smtpState = SmtpConnectionState.DISCONNECTED,
                            smtpStatusMessage = err,
                            snackbarMessage = if (!silent) err else null
                        )
                    }
                }
            }.onFailure { err ->
                addLog(LogType.ERROR, "Verifikasi SMTP error: ${err.message}")
                _uiState.update {
                    it.copy(
                        isVerifyingSmtp = false,
                        smtpState = SmtpConnectionState.DISCONNECTED,
                        smtpStatusMessage = err.message ?: "Gagal terhubung",
                        snackbarMessage = if (!silent) err.message else null
                    )
                }
            }
        }
    }

    fun autoDetectSmtpByEmail(emailOrDomain: String) {
        val domain = if (emailOrDomain.contains("@")) {
            emailOrDomain.substringAfter("@").trim()
        } else {
            emailOrDomain.trim()
        }
        if (domain.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "Masukkan email atau domain terlebih dahulu.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLookingUpDomain = true) }
            addLog(LogType.INFO, "Mendeteksi konfigurasi MX/SMTP untuk domain $domain...")
            repository.lookupDomain(domain).onSuccess { resp ->
                val info = resp.smtp
                if (resp.success && info != null) {
                    val updated = _uiState.value.smtpConfig.copy(
                        host = info.host,
                        port = info.port,
                        connectionType = info.connectionType,
                        dailyLimit = info.dailyLimit
                    )
                    repository.saveSmtpConfig(updated)
                    addLog(LogType.SUCCESS, "Deteksi domain berhasil (${resp.layer}): ${info.host}:${info.port}")
                    _uiState.update {
                        it.copy(
                            isLookingUpDomain = false,
                            smtpConfig = updated,
                            snackbarMessage = "Terdeteksi ${info.name}: ${info.host}:${info.port}"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLookingUpDomain = false,
                            snackbarMessage = resp.error ?: "Domain tidak ditemukan."
                        )
                    }
                }
            }.onFailure { err ->
                addLog(LogType.ERROR, "Deteksi domain gagal: ${err.message}")
                _uiState.update {
                    it.copy(isLookingUpDomain = false, snackbarMessage = "Gagal: ${err.message}")
                }
            }
        }
    }

    fun refreshKeyRotationStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingKeys = true) }
            repository.fetchKeyRotationStatus().onSuccess { status ->
                _uiState.update { it.copy(keyRotationStatus = status, isRefreshingKeys = false) }
            }.onFailure {
                _uiState.update { it.copy(isRefreshingKeys = false) }
            }
        }
    }

    fun resetKeyRotationCooldowns() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingKeys = true) }
            repository.resetKeyRotation().onSuccess { resp ->
                addLog(LogType.INFO, resp.message)
                _uiState.update {
                    it.copy(
                        keyRotationStatus = resp.status ?: it.keyRotationStatus,
                        isRefreshingKeys = false,
                        snackbarMessage = "Cooldown seluruh API Key direset."
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(isRefreshingKeys = false, snackbarMessage = "Reset gagal: ${err.message}")
                }
            }
        }
    }

    fun clearLogs() {
        _uiState.update { it.copy(logs = emptyList()) }
    }

    class Factory(
        private val repository: GSwiftRepository,
        private val connectivityObserver: ConnectivityObserver
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GSwiftViewModel(repository, connectivityObserver) as T
        }
    }
}
