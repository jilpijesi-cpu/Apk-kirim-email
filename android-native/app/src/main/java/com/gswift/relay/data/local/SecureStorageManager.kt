package com.gswift.relay.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gswift.relay.BuildConfig
import com.gswift.relay.data.model.ChatMessage
import com.gswift.relay.data.model.EmailTemplate
import com.gswift.relay.data.model.LastSentEmail
import com.gswift.relay.data.model.SmtpConfig

class SecureStorageManager(context: Context) {

    private val gson = Gson()

    private val securePrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "gswift_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val appPrefs: SharedPreferences =
        context.getSharedPreferences("gswift_app_prefs", Context.MODE_PRIVATE)

    fun saveSmtpConfig(config: SmtpConfig) {
        securePrefs.edit()
            .putString(KEY_SMTP_CONFIG, gson.toJson(config))
            .apply()
    }

    fun loadSmtpConfig(): SmtpConfig {
        val raw = securePrefs.getString(KEY_SMTP_CONFIG, null) ?: return SmtpConfig()
        return runCatching { gson.fromJson(raw, SmtpConfig::class.java) }
            .getOrDefault(SmtpConfig())
    }

    fun saveServerBaseUrl(url: String) {
        val cleaned = normalizeUrl(url)
        appPrefs.edit().putString(KEY_SERVER_BASE_URL, cleaned).apply()
    }

    fun loadServerBaseUrl(): String {
        val saved = appPrefs.getString(KEY_SERVER_BASE_URL, null)
        return if (!saved.isNullOrBlank()) normalizeUrl(saved) else normalizeUrl(BuildConfig.BASE_URL)
    }

    private fun normalizeUrl(raw: String): String {
        var trimmed = raw.trim()
        if (trimmed.isEmpty()) return BuildConfig.BASE_URL
        if (!trimmed.startsWith("http://", true) && !trimmed.startsWith("https://", true)) {
            trimmed = "https://$trimmed"
        }
        if (!trimmed.endsWith("/")) {
            trimmed = "$trimmed/"
        }
        return trimmed
    }

    fun saveLastSentEmail(item: LastSentEmail) {
        appPrefs.edit().putString(KEY_LAST_SENT, gson.toJson(item)).apply()
    }

    fun loadLastSentEmail(): LastSentEmail? {
        val raw = appPrefs.getString(KEY_LAST_SENT, null) ?: return null
        return runCatching { gson.fromJson(raw, LastSentEmail::class.java) }.getOrNull()
    }

    fun saveTemplates(templates: List<EmailTemplate>) {
        appPrefs.edit()
            .putString(KEY_TEMPLATES, gson.toJson(templates))
            .apply()
    }

    fun loadTemplates(): List<EmailTemplate> {
        val raw = appPrefs.getString(KEY_TEMPLATES, null) ?: return emptyList()
        val type = object : TypeToken<List<EmailTemplate>>() {}.type
        return runCatching { gson.fromJson<List<EmailTemplate>>(raw, type) }
            .getOrDefault(emptyList())
    }

    fun saveChatMessages(messages: List<ChatMessage>) {
        appPrefs.edit()
            .putString(KEY_CHAT_MESSAGES, gson.toJson(messages))
            .apply()
    }

    fun loadChatMessages(): List<ChatMessage> {
        val raw = appPrefs.getString(KEY_CHAT_MESSAGES, null) ?: return emptyList()
        val type = object : TypeToken<List<ChatMessage>>() {}.type
        return runCatching { gson.fromJson<List<ChatMessage>>(raw, type) }
            .getOrDefault(emptyList())
    }

    fun clearChatMessages() {
        appPrefs.edit().remove(KEY_CHAT_MESSAGES).apply()
    }

    fun saveCustomCancelLink(link: String) {
        appPrefs.edit().putString(KEY_CANCEL_LINK, link).apply()
    }

    fun loadCustomCancelLink(): String = appPrefs.getString(KEY_CANCEL_LINK, "") ?: ""

    fun saveCustomDataRules(rules: String) {
        appPrefs.edit().putString(KEY_DATA_RULES, rules).apply()
    }

    fun loadCustomDataRules(): String = appPrefs.getString(KEY_DATA_RULES, "") ?: ""

    companion object {
        private const val KEY_SMTP_CONFIG = "smtp_config"
        private const val KEY_SERVER_BASE_URL = "server_base_url"
        private const val KEY_LAST_SENT = "last_sent_email"
        private const val KEY_TEMPLATES = "email_templates"
        private const val KEY_CHAT_MESSAGES = "chat_messages"
        private const val KEY_CANCEL_LINK = "custom_cancel_link"
        private const val KEY_DATA_RULES = "custom_data_rules"
    }
}
