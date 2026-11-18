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
import com.gswift.relay.data.model.SmtpDiagnosticResult
import com.gswift.relay.data.model.SmtpLookupInfo
import com.gswift.relay.data.network.ConnectivityObserver
import com.gswift.relay.data.repository.GSwiftRepository
import com.gswift.relay.data.util.BankDraftEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

data class PasteFeedback(
    val isError: Boolean,
    val message: String,
    val details: String
)

data class MainUiState(
    val isOnline: Boolean = true,
    val backendOnline: Boolean = false,
    val serverBaseUrl: String = "",
    val smtpConfig: SmtpConfig = SmtpConfig(),
    val smtpState: SmtpConnectionState = SmtpConnectionState.UNCONFIGURED,
    val smtpStatusMessage: String = "Belum dikonfigurasi",
    val isVerifyingSmtp: Boolean = false,
    val isLookingUpDomain: Boolean = false,
    val detectedLookupInfo: SmtpLookupInfo? = null,
    val autoDetectSmtp: Boolean = true,
    val recipientInput: String = "",
    val subjectInput: String = "",
    val messageInput: String = "",
    val pasteFeedback: PasteFeedback? = null,
    val lastSentEmail: LastSentEmail? = null,
    val isSendingEmail: Boolean = false,
    val sendErrorInline: String? = null,
    val deliveryBanner: DeliveryBanner? = null,
    val aiMessages: List<ChatMessage> = emptyList(),
    val aiProviders: List<AiProviderInfo> = emptyList(),
    val selectedAiModel: String? = "Claude Mythos",
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
) {
    val smtpDiagnostic: SmtpDiagnosticResult
        get() = BankDraftEngine.computeSmtpDiagnostic(
            smtpConfig = smtpConfig,
            smtpState = smtpState,
            smtpStatusMessage = smtpStatusMessage,
            logs = logs
        )
}

class GSwiftViewModel(
    private val repository: GSwiftRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var domainLookupJob: Job? = null

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

    fun addLog(type: LogType, message: String) {
        val entry = LogEntry(timestamp = nowTime(), type = type, message = message)
        _uiState.update { state ->
            state.copy(logs = (listOf(entry) + state.logs).take(150))
        }
    }

    private fun loadInitialData() {
        val savedSmtp = repository.loadSmtpConfig()
        val savedTemplates = repository.loadTemplates()
        val savedLastSent = repository.loadLastSentEmail()
        val savedChat = repository.loadChatMessages().filterNot { it.id == "welcome" }
        val cancelLink = repository.loadCustomCancelLink()
        val dataRules = repository.loadCustomDataRules()
        val baseUrl = repository.loadServerBaseUrl()

        _uiState.update {
            it.copy(
                serverBaseUrl = baseUrl,
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
                customDataRules = dataRules,
                logs = listOf(LogEntry(nowTime(), LogType.INFO, "G-Swift Relay active. System ready."))
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
                addLog(LogType.SUCCESS, "Koneksi Relay terjalin dengan server (${it.environment}).")
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

    fun updateServerBaseUrl(url: String, testNow: Boolean = true) {
        repository.saveServerBaseUrl(url)
        val saved = repository.loadServerBaseUrl()
        _uiState.update {
            it.copy(
                serverBaseUrl = saved,
                snackbarMessage = "URL Server Relay diperbarui: $saved"
            )
        }
        addLog(LogType.INFO, "Server Relay API diubah ke: $saved")
        if (testNow) {
            checkHealthAndProviders()
        }
    }

    fun updateMailFields(to: String? = null, subject: String? = null, message: String? = null) {
        _uiState.update {
            val nextTo = to ?: it.recipientInput
            it.copy(
                recipientInput = nextTo,
                subjectInput = subject ?: it.subjectInput,
                messageInput = message ?: it.messageInput,
                sendErrorInline = null,
                pasteFeedback = if (to != null && to.isBlank()) null else it.pasteFeedback
            )
        }
    }

    fun handleRecipientPasteOrClean(pastedText: String) {
        val extracted = BankDraftEngine.extractEmailAddresses(pastedText)
        if (extracted.isNotEmpty()) {
            val cleaned = extracted.joinToString(", ")
            _uiState.update {
                it.copy(
                    recipientInput = cleaned,
                    pasteFeedback = PasteFeedback(
                        isError = false,
                        message = "${extracted.size} Alamat Email Berhasil Disaring & Diterapkan!",
                        details = cleaned
                    )
                )
            }
            addLog(LogType.INFO, "Auto-Filter: ${extracted.size} email disaring ($cleaned)")
        } else {
            _uiState.update {
                it.copy(
                    pasteFeedback = PasteFeedback(
                        isError = true,
                        message = "Teks Tidak Mengandung Format Email Valid!",
                        details = "Pastikan teks memuat alamat seperti nama@domain.com."
                    )
                )
            }
            addLog(LogType.WARNING, "Peringatan Tempel: Tidak ditemukan format email valid.")
        }
    }

    fun cleanCurrentRecipientInput() {
        val extracted = BankDraftEngine.extractEmailAddresses(_uiState.value.recipientInput)
        if (extracted.isNotEmpty()) {
            val cleaned = extracted.joinToString(", ")
            _uiState.update {
                it.copy(
                    recipientInput = cleaned,
                    pasteFeedback = PasteFeedback(
                        isError = false,
                        message = "Alamat email berhasil dibersihkan dari teks tambahan!",
                        details = cleaned
                    )
                )
            }
            addLog(LogType.INFO, "Auto-filter: ${extracted.size} email dibersihkan ($cleaned)")
        }
    }

    fun dismissPasteFeedback() {
        _uiState.update { it.copy(pasteFeedback = null) }
    }

    fun dismissSendErrorInline() {
        _uiState.update { it.copy(sendErrorInline = null) }
    }

    fun syncSendDraftTime() {
        val state = _uiState.value
        if (state.messageInput.isBlank() && state.subjectInput.isBlank()) return
        val nowMillis = System.currentTimeMillis()
        val updatedMsg = if (state.messageInput.isNotBlank()) {
            BankDraftEngine.synchronizeDateTimeInHtml(state.messageInput, nowMillis)
        } else state.messageInput
        val dt = BankDraftEngine.buildWibDateTime(nowMillis)
        val updatedSubj = state.subjectInput
            .replace(Regex("""\[(?:TANGGAL_TRANSAKSI|TGL_TRANSAKSI|TANGGAL_WAKTU|TGL_WAKTU|DATETIME|TANGGAL_DAN_WAKTU)]""", RegexOption.IGNORE_CASE), dt.dateTimeShort)
            .replace(Regex("""\[(?:TANGGAL|TGL|DATE)]""", RegexOption.IGNORE_CASE), dt.dateShort)
            .replace(Regex("""\[(?:WAKTU_TRANSAKSI|JAM_TRANSAKSI|WAKTU|JAM|TIME)]""", RegexOption.IGNORE_CASE), dt.timeShort)

        if (updatedMsg != state.messageInput || updatedSubj != state.subjectInput) {
            _uiState.update { it.copy(messageInput = updatedMsg, subjectInput = updatedSubj) }
        }
    }

    fun applyBankToSendDraft(bankKey: String, resetBlank: Boolean = false) {
        val state = _uiState.value
        val result = BankDraftEngine.applyFreshBankTemplate(
            html = if (resetBlank) "" else state.messageInput,
            targetBankKey = bankKey,
            customCancelLink = state.customCancelLink,
            forceNewBlank = resetBlank
        )
        val updatedSubj = state.subjectInput.ifBlank {
            "Konfirmasi Transaksi Resmi Bank ${result.appliedBank.label} (Ref: ${result.newReference})"
        }.let { subj ->
            var s = subj
            AppPresets.banks.filter { it.key != result.appliedBank.key }.forEach { other ->
                s = s.replace(Regex("\\b${Regex.escape(other.label)}\\b"), result.appliedBank.label)
            }
            BankDraftEngine.synchronizeSubject(s, result.newReference)
        }
        _uiState.update {
            it.copy(
                selectedBankKey = result.appliedBank.key,
                messageInput = result.html,
                subjectInput = updatedSubj,
                snackbarMessage = "Identitas Bank ${result.appliedBank.label} diterapkan (Ref: ${result.newReference})"
            )
        }
        addLog(LogType.SUCCESS, "Smart Bank Template: ${result.appliedBank.fullName} (Ref baru: ${result.newReference})")
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
            it.copy(
                recipientInput = "",
                subjectInput = "",
                messageInput = "",
                sendErrorInline = null,
                pasteFeedback = null,
                deliveryBanner = null
            )
        }
        addLog(LogType.INFO, "Formulir pengiriman email dibersihkan.")
    }

    fun dismissDeliveryBanner() {
        _uiState.update { it.copy(deliveryBanner = null) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun extractCleanEmails(raw: String): List<String> = BankDraftEngine.extractEmailAddresses(raw)

    fun sendEmail() {
        val state = _uiState.value
        if (!state.isOnline) {
            _uiState.update { it.copy(sendErrorInline = "Tidak ada koneksi internet.", snackbarMessage = "Tidak ada koneksi internet.") }
            return
        }
        val cleanEmails = extractCleanEmails(state.recipientInput)
        if (cleanEmails.isEmpty()) {
            val msg = "Silakan masukkan minimal satu alamat email penerima yang valid."
            _uiState.update { it.copy(sendErrorInline = msg) }
            addLog(LogType.WARNING, msg)
            return
        }
        if (state.subjectInput.isBlank()) {
            val msg = "Silakan masukkan subjek email."
            _uiState.update { it.copy(sendErrorInline = msg) }
            addLog(LogType.WARNING, msg)
            return
        }
        if (state.messageInput.isBlank()) {
            val msg = "Silakan lengkapi pesan draf email sebelum mengirim."
            _uiState.update { it.copy(sendErrorInline = msg) }
            addLog(LogType.WARNING, msg)
            return
        }
        if (state.smtpConfig.username.isBlank() || state.smtpConfig.password.isBlank()) {
            val msg = "Konfigurasi akun pengirim SMTP di tab Akun terlebih dahulu."
            _uiState.update { it.copy(sendErrorInline = msg) }
            addLog(LogType.WARNING, msg)
            return
        }

        val passCheck = BankDraftEngine.validateAppPassword(state.smtpConfig.password, state.smtpConfig.username)
        if (passCheck.isGmailOrYahoo && !passCheck.isExact16) {
            val errMsg = "Pengiriman dibatalkan: App Password Gmail/Yahoo harus tepat 16 karakter (saat ini ${passCheck.length} karakter). Perbarui di tab Akun."
            _uiState.update { it.copy(sendErrorInline = errMsg) }
            addLog(LogType.ERROR, errMsg)
            return
        }

        viewModelScope.launch {
            val startMs = System.currentTimeMillis()
            val dt = BankDraftEngine.buildWibDateTime(startMs)
            _uiState.update { it.copy(isSendingEmail = true, deliveryBanner = null, sendErrorInline = null) }
            val joinedRecipients = cleanEmails.joinToString(", ")
            addLog(LogType.INFO, "Memulai pengiriman paket ke $joinedRecipients (${state.smtpConfig.host}:${state.smtpConfig.port}) | ${dt.dateTimeShort}...")

            repository.sendEmail(
                to = joinedRecipients,
                subject = state.subjectInput,
                htmlOrText = state.messageInput,
                config = state.smtpConfig,
                customCancelLink = state.customCancelLink
            ).onSuccess { resp ->
                val elapsed = System.currentTimeMillis() - startMs
                val lastItem = LastSentEmail(
                    to = joinedRecipients,
                    subject = state.subjectInput,
                    timestamp = nowShortTime()
                )
                repository.saveLastSentEmail(lastItem)
                addLog(LogType.SUCCESS, "Relay success. MessageID: ${resp.messageId ?: "-"} (${elapsed}ms)")
                addLog(LogType.SUCCESS, "Target sistem berhasil diakses. [$joinedRecipients]")
                _uiState.update {
                    it.copy(
                        isSendingEmail = false,
                        smtpState = SmtpConnectionState.CONNECTED,
                        lastSentEmail = lastItem,
                        recipientInput = "",
                        subjectInput = "",
                        messageInput = "",
                        deliveryBanner = DeliveryBanner(
                            isSuccess = true,
                            title = "TARGET SISTEM BERHASIL DI RETASS",
                            subtitle = "Pengiriman Email Sukses • Message Dispatched",
                            detail = "Penerima: $joinedRecipients\nMessage ID: ${resp.messageId ?: "Terkonfirmasi"}\nWaktu Proses: ${elapsed}ms",
                            elapsedMs = elapsed
                        )
                    )
                }
            }.onFailure { err ->
                val elapsed = System.currentTimeMillis() - startMs
                val raw = err.message ?: "Gagal mengirim email"
                val tip = when {
                    raw.contains("cookie_check", true) || raw.contains("ais-dev", true) ->
                        "Server dev AI Studio meminta autentikasi browser. Buka tab Akun -> Koneksi Server Relay API, lalu masukkan URL server publik Anda."
                    raw.contains("535") || raw.contains("App Password", true) || raw.contains("BadCredentials", true) ->
                        "Server SMTP menolak sandi. Untuk Gmail/Yahoo, aktifkan Verifikasi 2 Langkah lalu buat 16 karakter Sandi Aplikasi tanpa spasi di myaccount.google.com/apppasswords."
                    raw.contains("timeout", true) || raw.contains("ECONNREFUSED", true) ->
                        "Port SMTP terblokir atau tidak merespons. Gunakan Port 587 (STARTTLS) atau Port 465 (SSL/TLS)."
                    else -> null
                }
                addLog(LogType.ERROR, "Relay failed: $raw")
                _uiState.update {
                    it.copy(
                        isSendingEmail = false,
                        smtpState = SmtpConnectionState.DISCONNECTED,
                        smtpStatusMessage = raw,
                        sendErrorInline = raw,
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
        val cleanTarget = extractCleanEmails(targetEmail).firstOrNull() ?: targetEmail.trim()
        if (cleanTarget.isBlank()) return
        if (state.smtpConfig.username.isBlank() || state.smtpConfig.password.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "Atur akun SMTP di tab Akun sebelum kirim tes.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingEmail = true) }
            repository.sendEmail(
                to = cleanTarget,
                subject = template.subject,
                htmlOrText = template.message,
                config = state.smtpConfig,
                customCancelLink = state.customCancelLink
            ).onSuccess {
                addLog(LogType.SUCCESS, "[UJICOBA SUKSES] Email '${template.name}' terkirim ke $cleanTarget.")
                _uiState.update {
                    it.copy(isSendingEmail = false, snackbarMessage = "Email tes terkirim ke $cleanTarget")
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
                snackbarMessage = "Aturan & Tautan AI tersimpan."
            )
        }
        addLog(LogType.INFO, "Pengaturan AI diperbarui.")
    }

    fun sendAiPrompt(promptText: String) {
        val cleanPrompt = promptText.trim()
        if (cleanPrompt.isEmpty()) return
        val state = _uiState.value
        if (!state.isOnline) {
            _uiState.update { it.copy(snackbarMessage = "AI membutuhkan koneksi internet.") }
            return
        }

        val detectedBank = AppPresets.detectBankKey(cleanPrompt)
        val detectedScenario = AppPresets.detectScenarioKey(cleanPrompt)

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = "user",
            text = cleanPrompt,
            timestamp = nowTime()
        )
        val cleanHistoryBefore = state.aiMessages.filterNot { it.id == "welcome" || it.isError }
        val updatedHistory = cleanHistoryBefore + userMsg
        _uiState.update {
            it.copy(
                aiMessages = updatedHistory,
                isAiThinking = true,
                selectedBankKey = detectedBank,
                selectedScenario = detectedScenario
            )
        }
        repository.saveChatMessages(updatedHistory)

        viewModelScope.launch {
            repository.sendAiChat(
                message = cleanPrompt,
                history = cleanHistoryBefore,
                model = state.selectedAiModel ?: "Claude Mythos",
                bankKey = detectedBank,
                scenario = detectedScenario,
                cancelLink = state.customCancelLink,
                customDataRules = state.customDataRules
            ).onSuccess { resp ->
                val rawText = resp.text ?: resp.error ?: "Tidak ada respons dari model AI."
                if (resp.text == null && resp.error != null) {
                    val errMsg = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        role = "model",
                        text = resp.error,
                        timestamp = nowTime(),
                        isError = true
                    )
                    val nextMessages = _uiState.value.aiMessages + errMsg
                    _uiState.update { it.copy(aiMessages = nextMessages, isAiThinking = false) }
                    return@onSuccess
                }

                val parsed = BankDraftEngine.parseMessageContent(rawText)
                val syncedHtml = if (parsed.html.isNotBlank()) {
                    val ref = BankDraftEngine.generateUniqueBankReference(detectedBank)
                    BankDraftEngine.synchronizeDynamicFieldsInHtml(parsed.html, ref, refreshRef = true)
                } else null

                val rotationNote = resp.rotationEvents
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString(", ") { "${it.from} → ${it.to}" }

                if (resp.rotationActive || resp.rotationEvents.isNotEmpty()) {
                    addLog(LogType.WARNING, "Rotasi Otomatis AI aktif (${rotationNote ?: resp.usedModel ?: "Auto"}). ")
                } else {
                    addLog(LogType.SUCCESS, "AI selesai merancang draf (${resp.usedModel ?: "Claude Mythos"}).")
                }

                val aiMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = "model",
                    text = parsed.text.ifBlank { if (!syncedHtml.isNullOrBlank()) "Draf email HTML siap digunakan." else rawText },
                    timestamp = nowTime(),
                    usedModel = resp.usedModel ?: "Claude Mythos",
                    rotationNote = rotationNote,
                    extractedSubject = parsed.subject.ifBlank { null },
                    extractedHtml = syncedHtml
                )
                val nextMessages = _uiState.value.aiMessages + aiMsg
                repository.saveChatMessages(nextMessages)

                // Auto-sync generated draft & subject directly into Send form (1:1 App.tsx behavior)
                _uiState.update { cur ->
                    cur.copy(
                        aiMessages = nextMessages,
                        isAiThinking = false,
                        subjectInput = aiMsg.extractedSubject ?: cur.subjectInput,
                        messageInput = syncedHtml ?: cur.messageInput
                    )
                }
                if (!syncedHtml.isNullOrBlank()) {
                    addLog(LogType.SUCCESS, "Sinkronisasi Otomatis: Draf AI & Subjek disinkronkan ke tab Kirim.")
                }
                refreshKeyRotationStatus()
            }.onFailure { err ->
                addLog(LogType.ERROR, "Chat AI gagal: ${err.message}")
                val errMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = "model",
                    text = err.message ?: "Tidak dapat menghubungkan ke server G-Swift.",
                    timestamp = nowTime(),
                    isError = true
                )
                val nextMessages = _uiState.value.aiMessages + errMsg
                _uiState.update {
                    it.copy(aiMessages = nextMessages, isAiThinking = false)
                }
            }
        }
    }

    fun randomizeAiMessageRef(messageId: String) {
        val currentList = _uiState.value.aiMessages.toMutableList()
        val idx = currentList.indexOfFirst { it.id == messageId }
        if (idx < 0) return
        val msg = currentList[idx]
        val html = msg.extractedHtml ?: return
        val bankKey = AppPresets.detectBankKey("${msg.extractedSubject.orEmpty()} $html")
        val newRef = BankDraftEngine.generateUniqueBankReference(bankKey)
        val refreshedHtml = BankDraftEngine.synchronizeDynamicFieldsInHtml(html, newRef, refreshRef = true)
        val refreshedSubject = msg.extractedSubject?.let { BankDraftEngine.synchronizeSubject(it, newRef) }
        currentList[idx] = msg.copy(extractedHtml = refreshedHtml, extractedSubject = refreshedSubject)
        repository.saveChatMessages(currentList)
        _uiState.update {
            it.copy(
                aiMessages = currentList,
                messageInput = refreshedHtml,
                subjectInput = refreshedSubject ?: it.subjectInput,
                snackbarMessage = "No. Ref diperbarui: $newRef"
            )
        }
        addLog(LogType.INFO, "Acak Ref: No. Referensi baru $newRef diterapkan.")
    }

    fun clearAiChat() {
        repository.clearChatMessages()
        _uiState.update { it.copy(aiMessages = emptyList()) }
        addLog(LogType.INFO, "Riwayat percakapan AI dibersihkan.")
    }

    fun applyAiDraftToSend(subject: String?, htmlOrBody: String) {
        val bankKey = AppPresets.detectBankKey("${subject.orEmpty()} $htmlOrBody")
        val newRef = BankDraftEngine.generateUniqueBankReference(bankKey)
        val syncedHtml = BankDraftEngine.synchronizeDynamicFieldsInHtml(htmlOrBody, newRef, refreshRef = true)
        val syncedSubj = subject?.let { BankDraftEngine.synchronizeSubject(it, newRef) }
        _uiState.update {
            it.copy(
                subjectInput = syncedSubj ?: it.subjectInput,
                messageInput = syncedHtml,
                snackbarMessage = "Draf AI & Ref baru ($newRef) siap dikirim."
            )
        }
        addLog(LogType.INFO, "Draf AI diterapkan ke tab Kirim (Ref: $newRef).")
    }

    fun saveAiDraftAsTemplate(subject: String?, htmlContent: String) {
        val cleanSubject = subject?.trim().orEmpty().ifBlank { "Draf Template Email AI" }
        val name = if (cleanSubject.length > 38) "${cleanSubject.take(38)}..." else cleanSubject
        saveTemplate(
            name = name,
            category = "AI Generated",
            subject = cleanSubject,
            message = htmlContent,
            editingId = null
        )
    }

    fun saveTemplate(name: String, category: String, subject: String, message: String, editingId: String? = null) {
        val list = _uiState.value.templates.toMutableList()
        if (editingId != null) {
            val idx = list.indexOfFirst { it.id == editingId }
            if (idx >= 0) {
                list[idx] = EmailTemplate(editingId, name, category.ifBlank { "General" }, subject, message)
            }
        } else {
            list.add(0, EmailTemplate(UUID.randomUUID().toString(), name, category.ifBlank { "General" }, subject, message))
        }
        repository.saveTemplates(list)
        _uiState.update {
            it.copy(templates = list, snackbarMessage = "Template '$name' tersimpan.")
        }
        addLog(LogType.SUCCESS, "Template '$name' disimpan.")
    }

    fun deleteTemplate(id: String) {
        val list = _uiState.value.templates.filterNot { it.id == id }
        repository.saveTemplates(list)
        _uiState.update { it.copy(templates = list, snackbarMessage = "Template dihapus.") }
        addLog(LogType.WARNING, "Template dihapus.")
    }

    fun deleteAllTemplates() {
        repository.saveTemplates(emptyList())
        _uiState.update { it.copy(templates = emptyList(), snackbarMessage = "Semua template dihapus.") }
        addLog(LogType.WARNING, "Semua template berhasil dihapus.")
    }

    fun applyTemplateToSend(template: EmailTemplate) {
        val nowMillis = System.currentTimeMillis()
        val bankKey = AppPresets.detectBankKey("${template.subject} ${template.message}")
        val newRef = BankDraftEngine.generateUniqueBankReference(bankKey, nowMillis)
        val dt = BankDraftEngine.buildWibDateTime(nowMillis)
        val syncedMsg = BankDraftEngine.synchronizeDynamicFieldsInHtml(template.message, newRef, nowMillis, refreshRef = true)
        val syncedSubj = BankDraftEngine.synchronizeSubject(template.subject, newRef, nowMillis)

        _uiState.update {
            it.copy(
                subjectInput = syncedSubj,
                messageInput = syncedMsg,
                snackbarMessage = "Template '${template.name}' diterapkan (Ref: $newRef)."
            )
        }
        addLog(LogType.INFO, "Template '${template.name}' diterapkan | Ref: $newRef | Waktu: ${dt.dateTimeShort}")
    }

    fun toggleAutoDetectSmtp(enabled: Boolean) {
        _uiState.update { it.copy(autoDetectSmtp = enabled) }
    }

    fun updateSmtpConfig(config: SmtpConfig) {
        val prevUser = _uiState.value.smtpConfig.username
        var merged = config
        if (config.username != prevUser) {
            val preset = AppPresets.getSmartSmtpPreset(config.username)
            if (preset != null) {
                merged = config.copy(
                    host = preset.host,
                    port = preset.port,
                    connectionType = preset.connectionType,
                    dailyLimit = preset.dailyLimit
                )
            } else if (config.username.contains("@") && _uiState.value.autoDetectSmtp) {
                val domain = config.username.substringAfter("@").trim()
                if (domain.contains(".")) {
                    scheduleDomainAutoLookup(domain)
                }
            }
        }
        _uiState.update { it.copy(smtpConfig = merged) }
    }

    private fun scheduleDomainAutoLookup(domain: String) {
        domainLookupJob?.cancel()
        domainLookupJob = viewModelScope.launch {
            delay(850)
            autoDetectSmtpByEmail(domain, silentIfFail = true)
        }
    }

    fun saveSmtpSettings(config: SmtpConfig, onNavigateToSend: () -> Unit) {
        val preset = AppPresets.getSmartSmtpPreset(config.username)
        val normalized = config.copy(
            host = config.host.trim().ifBlank { preset?.host ?: "smtp.gmail.com" },
            port = config.port.trim().ifBlank { preset?.port ?: "587" },
            username = config.username.trim(),
            senderEmail = config.senderEmail.ifBlank { config.username }.trim(),
            password = config.password.replace(Regex("[\\s\\u00A0\\u200B-\\u200D\\uFEFF]+"), "").trim()
        )
        repository.saveSmtpConfig(normalized)
        _uiState.update {
            it.copy(
                smtpConfig = normalized,
                snackbarMessage = "Konfigurasi SMTP disimpan."
            )
        }
        addLog(LogType.SUCCESS, "Konfigurasi SMTP disimpan dan diperbarui.")
        verifySmtpConnection(normalized, silent = false)
        onNavigateToSend()
    }

    fun testSmtpAndSendSample(config: SmtpConfig) {
        if (config.username.isBlank() || config.password.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "Lengkapi username dan App Password sebelum uji koneksi.") }
            return
        }
        val preset = AppPresets.getSmartSmtpPreset(config.username)
        val cleaned = config.copy(
            host = config.host.trim().ifBlank { preset?.host ?: "smtp.gmail.com" },
            port = config.port.trim().ifBlank { preset?.port ?: "587" },
            username = config.username.trim(),
            senderEmail = config.senderEmail.ifBlank { config.username }.trim(),
            password = config.password.replace(Regex("[\\s\\u00A0\\u200B-\\u200D\\uFEFF]+"), "").trim()
        )
        repository.saveSmtpConfig(cleaned)
        _uiState.update { it.copy(smtpConfig = cleaned, isVerifyingSmtp = true, smtpState = SmtpConnectionState.CHECKING) }

        viewModelScope.launch {
            addLog(LogType.INFO, "Memverifikasi koneksi SMTP (${cleaned.host}:${cleaned.port})...")
            repository.verifySmtp(cleaned).onSuccess { verifyResp ->
                if (!verifyResp.connected) {
                    val err = verifyResp.error ?: "Gagal terhubung ke server SMTP."
                    addLog(LogType.ERROR, "[UJICOBA GAGAL] $err")
                    _uiState.update {
                        it.copy(
                            isVerifyingSmtp = false,
                            smtpState = SmtpConnectionState.DISCONNECTED,
                            smtpStatusMessage = err,
                            snackbarMessage = "Verifikasi gagal: $err"
                        )
                    }
                    return@onSuccess
                }

                // Connected -> send actual sample test email just like App.tsx testSMTPConnection()
                val targetEmail = cleaned.senderEmail.ifBlank { cleaned.username }
                val dt = BankDraftEngine.buildWibDateTime()
                val testHtml = """
                    <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; text-align: center; padding: 36px 20px; background: #f0f7ff; border-radius: 20px; border: 1px solid #d0e4ff; max-width: 520px; margin: 0 auto;">
                      <h2 style="color: #002d54; font-size: 19px; font-weight: 800; margin: 0 0 8px 0;">Ujicoba Koneksi Relay Berhasil!</h2>
                      <p style="color: #475569; font-size: 13px; margin: 0 0 18px 0; line-height: 1.5;">Pengaturan SMTP Anda valid dan server pengiriman telah berhasil merelay pesan ini ke inbox Anda.</p>
                      <div style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 12px; font-size: 11px; color: #64748b; text-align: left; line-height: 1.6;">
                        <div><b>Server Relay:</b> ${cleaned.host}:${cleaned.port}</div>
                        <div><b>Akun Pengirim:</b> ${cleaned.username}</div>
                        <div><b>Waktu Pengiriman:</b> ${dt.dateTimeShort} WIB</div>
                      </div>
                    </div>
                """.trimIndent()

                addLog(LogType.SUCCESS, "SMTP terverifikasi! Mengirim email uji coba ke $targetEmail...")
                repository.sendEmail(
                    to = targetEmail,
                    subject = "Test Connection - G-Swift Relay Console",
                    htmlOrText = testHtml,
                    config = cleaned
                ).onSuccess {
                    addLog(LogType.SUCCESS, "[UJICOBA SUKSES] Email tes terkirim ke $targetEmail.")
                    _uiState.update {
                        it.copy(
                            isVerifyingSmtp = false,
                            smtpState = SmtpConnectionState.CONNECTED,
                            smtpStatusMessage = "Terhubung & Email tes terkirim ke $targetEmail",
                            snackbarMessage = "Koneksi valid! Email uji coba dikirim ke $targetEmail"
                        )
                    }
                }.onFailure { sendErr ->
                    val err = sendErr.message ?: "Gagal mengirim email uji coba."
                    addLog(LogType.ERROR, "[UJICOBA GAGAL] $err")
                    _uiState.update {
                        it.copy(
                            isVerifyingSmtp = false,
                            smtpState = SmtpConnectionState.DISCONNECTED,
                            smtpStatusMessage = err,
                            snackbarMessage = "Gagal kirim tes: $err"
                        )
                    }
                }
            }.onFailure { err ->
                val msg = err.message ?: "Gagal terhubung ke server."
                addLog(LogType.ERROR, "[UJICOBA GAGAL] $msg")
                _uiState.update {
                    it.copy(
                        isVerifyingSmtp = false,
                        smtpState = SmtpConnectionState.DISCONNECTED,
                        smtpStatusMessage = msg,
                        snackbarMessage = msg
                    )
                }
            }
        }
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

        val passCheck = BankDraftEngine.validateAppPassword(config.password, config.username)
        if (passCheck.isGmailOrYahoo && !passCheck.isExact16) {
            val msg = "Format App Password belum memenuhi syarat (${passCheck.length}/16 karakter). Harap gunakan 16 karakter tanpa spasi."
            _uiState.update {
                it.copy(
                    isVerifyingSmtp = false,
                    smtpState = SmtpConnectionState.DISCONNECTED,
                    smtpStatusMessage = msg,
                    snackbarMessage = if (!silent) msg else null
                )
            }
            if (!silent) addLog(LogType.ERROR, msg)
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
                    addLog(LogType.SUCCESS, "SMTP terhubung: ${resp.host ?: config.host}:${resp.port ?: config.port} (${resp.username ?: config.username})")
                    _uiState.update {
                        it.copy(
                            isVerifyingSmtp = false,
                            smtpState = SmtpConnectionState.CONNECTED,
                            smtpStatusMessage = resp.message ?: "Terhubung ke ${resp.host ?: config.host}",
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

    fun autoDetectSmtpByEmail(emailOrDomain: String, silentIfFail: Boolean = false) {
        val domain = if (emailOrDomain.contains("@")) {
            emailOrDomain.substringAfter("@").trim()
        } else {
            emailOrDomain.trim()
        }
        if (domain.isBlank()) {
            if (!silentIfFail) {
                _uiState.update { it.copy(snackbarMessage = "Masukkan email atau domain terlebih dahulu.") }
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLookingUpDomain = true) }
            addLog(LogType.INFO, "Mendeteksi konfigurasi MX/SMTP untuk domain $domain...")
            repository.lookupDomain(domain).onSuccess { resp ->
                val info = resp.smtp
                if (resp.success && info != null) {
                    val updated = if (_uiState.value.autoDetectSmtp || !silentIfFail) {
                        _uiState.value.smtpConfig.copy(
                            host = info.host,
                            port = info.port,
                            connectionType = info.connectionType,
                            dailyLimit = info.dailyLimit
                        )
                    } else _uiState.value.smtpConfig

                    if (_uiState.value.autoDetectSmtp || !silentIfFail) {
                        repository.saveSmtpConfig(updated)
                    }
                    addLog(LogType.SUCCESS, "SMTP Cerdas (${resp.layer ?: "7-Layer"}): ${info.host}:${info.port}")
                    _uiState.update {
                        it.copy(
                            isLookingUpDomain = false,
                            detectedLookupInfo = info,
                            smtpConfig = updated,
                            snackbarMessage = if (!silentIfFail) "Terdeteksi ${info.name}: ${info.host}:${info.port}" else null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLookingUpDomain = false,
                            snackbarMessage = if (!silentIfFail) (resp.error ?: "Domain tidak ditemukan.") else null
                        )
                    }
                }
            }.onFailure { err ->
                if (!silentIfFail) {
                    addLog(LogType.ERROR, "Deteksi domain gagal: ${err.message}")
                }
                _uiState.update {
                    it.copy(
                        isLookingUpDomain = false,
                        snackbarMessage = if (!silentIfFail) "Gagal: ${err.message}" else null
                    )
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
