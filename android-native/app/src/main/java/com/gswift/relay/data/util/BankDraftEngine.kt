package com.gswift.relay.data.util

import androidx.core.text.HtmlCompat
import com.gswift.relay.data.model.AppPasswordValidation
import com.gswift.relay.data.model.AppPresets
import com.gswift.relay.data.model.BankOption
import com.gswift.relay.data.model.ClientDateTime
import com.gswift.relay.data.model.LogEntry
import com.gswift.relay.data.model.LogType
import com.gswift.relay.data.model.RecipientValidationState
import com.gswift.relay.data.model.SmtpConfig
import com.gswift.relay.data.model.SmtpConnectionState
import com.gswift.relay.data.model.SmtpDiagnosticResult
import com.gswift.relay.data.model.SmtpDiagnosticStep
import java.util.Calendar
import java.util.TimeZone
import kotlin.random.Random

data class ParsedAiMessage(
    val text: String,
    val html: String,
    val subject: String
)

data class FreshBankTemplateResult(
    val html: String,
    val newReference: String,
    val previousBankKey: String,
    val appliedBank: BankOption
)

data class ParsedReceiptPreview(
    val bank: BankOption,
    val statusBadge: String,
    val heading: String,
    val introText: String,
    val rows: List<Pair<String, String>>,
    val ctaLabel: String?,
    val ctaUrl: String?,
    val footerText: String?,
    val fallbackText: String
)

object BankDraftEngine {

    private val STALE_BANK_COLORS = listOf(
        "#0066b2", "#005baa", "#004080", "#00529c",
        "#003a8f", "#002c6c", "#012a66",
        "#005e6a", "#004d57", "#024a54",
        "#8b0000", "#7a0000", "#990000", "#b91c1c",
        "#00205b", "#001845", "#001233"
    )

    private val COMMON_DOMAINS = setOf(
        "gmail.com", "yahoo.com", "yahoo.co.id", "outlook.com", "hotmail.com",
        "icloud.com", "live.com", "msn.com", "qq.com", "protonmail.com",
        "zoho.com", "gmx.com", "mail.com", "yandex.com", "me.com"
    )

    private val COMMON_TLDS = listOf(
        ".com", ".id", ".co.id", ".net", ".org", ".edu", ".ac.id",
        ".go.id", ".mil.id", ".or.id", ".info", ".biz", ".me",
        ".io", ".dev", ".web.id", ".my.id", ".xyz"
    )

    fun buildWibDateTime(nowMillis: Long = System.currentTimeMillis()): ClientDateTime {
        val tz = TimeZone.getTimeZone("Asia/Jakarta")
        val cal = Calendar.getInstance(tz).apply { timeInMillis = nowMillis }
        val monthsShort = arrayOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
        val monthsFull = arrayOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
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

    fun generateUniqueBankReference(bankKey: String, nowMillis: Long = System.currentTimeMillis()): String {
        val bank = AppPresets.banks.find { it.key == bankKey } ?: AppPresets.banks.first()
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta")).apply {
            timeInMillis = nowMillis
        }
        val year = cal.get(Calendar.YEAR)
        val month = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val day = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        val randNum = Random.nextInt(100000, 999999)
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val suffix = (1..4).map { chars[Random.nextInt(chars.length)] }.joinToString("")
        val cleanName = bank.label.uppercase().replace(Regex("[^A-Z0-9]"), "")
        return "$cleanName$year$month$day$randNum$suffix"
    }

    fun extractEmailAddresses(raw: String): List<String> {
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

    fun validateRecipients(raw: String): RecipientValidationState {
        val rawVal = raw.trim()
        val isToFilled = rawVal.isNotEmpty()
        val parsedList = extractEmailAddresses(rawVal)
        val tokens = rawVal.split(Regex("[,;\\s\\n\\r\\t]+")).filter { it.isNotBlank() }
        val strictEmailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,63}$")
        val isStrictValid = tokens.isNotEmpty() && tokens.all { strictEmailRegex.matches(it) }

        val invalidDomainTok = tokens.find { tok ->
            val parts = tok.split("@")
            if (parts.size != 2) return@find false
            val domain = parts[1].lowercase()
            if (COMMON_DOMAINS.contains(domain)) return@find false
            if (COMMON_TLDS.any { domain.endsWith(it) }) return@find false
            true
        }

        val hasInvalidCommonDomain = invalidDomainTok != null
        val hasJunkOutsideEmail = isToFilled && !isStrictValid && parsedList.isNotEmpty()
        val hasNoEmailAtAll = isToFilled && parsedList.isEmpty()

        return RecipientValidationState(
            isToFilled = isToFilled,
            parsedList = parsedList,
            tokenCount = tokens.size,
            isStrictValid = isStrictValid,
            hasInvalidCommonDomain = hasInvalidCommonDomain,
            hasJunkOutsideEmail = hasJunkOutsideEmail,
            hasNoEmailAtAll = hasNoEmailAtAll
        )
    }

    fun validateAppPassword(password: String, username: String): AppPasswordValidation {
        val lowerUser = username.trim().lowercase()
        val isGmail = lowerUser.endsWith("@gmail.com") || lowerUser.endsWith("@googlemail.com")
        val isYahoo = lowerUser.endsWith("@yahoo.com") || lowerUser.endsWith("@ymail.com") || lowerUser.endsWith("@yahoo.co.id")
        val isIcloud = lowerUser.endsWith("@icloud.com")
        val hasSpaces = Regex("[\\s\\u00A0]+").containsMatchIn(password)
        val clean = password.replace(Regex("[\\s\\u00A0\\u200B-\\u200D\\uFEFF]+"), "").trim()
        return AppPasswordValidation(
            isGmail = isGmail,
            isGmailOrYahoo = isGmail || isYahoo || isIcloud,
            isExact16 = clean.length == 16,
            length = clean.length,
            hasSpaces = hasSpaces,
            clean = clean
        )
    }

    fun parseMessageContent(rawText: String): ParsedAiMessage {
        if (rawText.isBlank()) return ParsedAiMessage("", "", "")

        var extractedSubject = ""
        val subjectPatterns = listOf(
            Regex("""Subjek Rekomendasi[:*`\s]+`([^`]+)`""", RegexOption.IGNORE_CASE),
            Regex("""Subjek(?:\s*Email)?[:*\s]+([^\n]+)""", RegexOption.IGNORE_CASE)
        )
        for (p in subjectPatterns) {
            val match = p.find(rawText)
            if (match != null) {
                extractedSubject = match.groupValues[1].trim().trim('`', '"', '*')
                break
            }
        }

        val htmlBlockMatch = Regex("""```html\s*([\s\S]*?)```""", RegexOption.IGNORE_CASE).find(rawText)
        val genericBlockMatch = if (htmlBlockMatch == null) {
            Regex("""```\s*(<!DOCTYPE[\s\S]*?|<html[\s\S]*?|<table[\s\S]*?)```""", RegexOption.IGNORE_CASE).find(rawText)
        } else null

        val extractedHtml = when {
            htmlBlockMatch != null -> htmlBlockMatch.groupValues[1].trim()
            genericBlockMatch != null -> genericBlockMatch.groupValues[1].trim()
            rawText.contains("<!DOCTYPE html", ignoreCase = true) ||
                (rawText.contains("<table", ignoreCase = true) && rawText.contains("</table>", ignoreCase = true)) -> {
                val startIdx = rawText.indexOf("<!DOCTYPE", ignoreCase = true)
                    .takeIf { it >= 0 }
                    ?: rawText.indexOf("<html", ignoreCase = true).takeIf { it >= 0 }
                    ?: rawText.indexOf("<table", ignoreCase = true).coerceAtLeast(0)
                rawText.substring(startIdx).trim()
            }
            else -> ""
        }

        var cleanText = rawText
            .replace(Regex("""```html\s*[\s\S]*?```""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""```\s*(?:<!DOCTYPE|<html|<table)[\s\S]*?```""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""Subjek Rekomendasi[:*`\s]+`[^`]+`""", RegexOption.IGNORE_CASE), "")
            .trim()

        if (extractedHtml.isNotEmpty() && cleanText.contains("<!DOCTYPE", ignoreCase = true)) {
            val idx = cleanText.indexOf("<!DOCTYPE", ignoreCase = true)
            cleanText = cleanText.substring(0, idx).trim()
        }

        if (extractedSubject.isBlank() && extractedHtml.isNotEmpty()) {
            val titleMatch = Regex("""<title[^>]*>([^<]+)</title>""", RegexOption.IGNORE_CASE).find(extractedHtml)
            if (titleMatch != null) {
                extractedSubject = titleMatch.groupValues[1].trim()
            }
        }

        return ParsedAiMessage(text = cleanText, html = extractedHtml, subject = extractedSubject)
    }

    fun synchronizeDateTimeInHtml(html: String, nowMillis: Long = System.currentTimeMillis()): String {
        if (html.isBlank()) return html
        val dt = buildWibDateTime(nowMillis)
        var res = html
            .replace(Regex("""\[(?:TANGGAL_TRANSAKSI|TGL_TRANSAKSI|TANGGAL_WAKTU|TGL_WAKTU|DATETIME|TANGGAL_DAN_WAKTU)]""", RegexOption.IGNORE_CASE), "${dt.dateTimeShort} WIB")
            .replace(Regex("""\{\{\s*(?:tanggal_transaksi|tgl_transaksi|tanggal_waktu|tgl_waktu|datetime|tanggal_dan_waktu)\s*}}""", RegexOption.IGNORE_CASE), "${dt.dateTimeShort} WIB")
            .replace(Regex("""\[(?:TANGGAL|TGL|DATE)]""", RegexOption.IGNORE_CASE), dt.dateShort)
            .replace(Regex("""\{\{\s*(?:tanggal|tgl|date)\s*}}""", RegexOption.IGNORE_CASE), dt.dateShort)
            .replace(Regex("""\[(?:WAKTU_TRANSAKSI|JAM_TRANSAKSI|WAKTU|JAM|TIME)]""", RegexOption.IGNORE_CASE), "${dt.timeShort} WIB")
            .replace(Regex("""\{\{\s*(?:waktu_transaksi|jam_transaksi|waktu|jam|time)\s*}}""", RegexOption.IGNORE_CASE), "${dt.timeShort} WIB")

        // Also update existing date/time row values in transaction receipt tables
        val dateCellRegex = Regex(
            """(<td\b[^>]*>(?:[^<]*?(?:Tanggal\s*(?:&amp;|&)?\s*Waktu|Tanggal\s*Transaksi|Waktu\s*Transaksi|Tanggal)[^<]*?)</td>\s*<td\b[^>]*>)([\s\S]*?)(</td>)""",
            RegexOption.IGNORE_CASE
        )
        res = dateCellRegex.replace(res) { m ->
            val prefix = m.groupValues[1]
            val valContent = m.groupValues[2]
            val suffix = m.groupValues[3]
            if (valContent.contains("<")) {
                val replacedInner = valContent.replace(Regex(""">[^<]+<""")) { ">${dt.dateTimeShort} WIB<" }
                "$prefix$replacedInner$suffix"
            } else {
                "$prefix${dt.dateTimeShort} WIB$suffix"
            }
        }
        return res
    }

    fun synchronizeSubject(subject: String, ref: String, nowMillis: Long = System.currentTimeMillis()): String {
        if (subject.isBlank()) return subject
        val dt = buildWibDateTime(nowMillis)
        return subject
            .replace(Regex("""\[(?:NO_REFERENSI|NO_REF|REFERENCE_NO|NO_TRANSAKSI|REF_ID|KODE_REFERENSI|NOMOR_REFERENSI)]""", RegexOption.IGNORE_CASE), ref)
            .replace(Regex("""\{\{\s*(?:no_referensi|no_ref|reference_no|no_transaksi|ref_id|kode_referensi|nomor_referensi)\s*}}""", RegexOption.IGNORE_CASE), ref)
            .replace(Regex("""\[(?:TANGGAL_TRANSAKSI|TGL_TRANSAKSI|TANGGAL_WAKTU|TGL_WAKTU|DATETIME|TANGGAL_DAN_WAKTU)]""", RegexOption.IGNORE_CASE), dt.dateTimeShort)
            .replace(Regex("""\{\{\s*(?:tanggal_transaksi|tgl_transaksi|tanggal_waktu|tgl_waktu|datetime|tanggal_dan_waktu)\s*}}""", RegexOption.IGNORE_CASE), dt.dateTimeShort)
            .replace(Regex("""\[(?:TANGGAL|TGL|DATE)]""", RegexOption.IGNORE_CASE), dt.dateShort)
            .replace(Regex("""\{\{\s*(?:tanggal|tgl|date)\s*}}""", RegexOption.IGNORE_CASE), dt.dateShort)
            .replace(Regex("""\[(?:WAKTU_TRANSAKSI|JAM_TRANSAKSI|WAKTU|JAM|TIME)]""", RegexOption.IGNORE_CASE), dt.timeShort)
            .replace(Regex("""\{\{\s*(?:waktu_transaksi|jam_transaksi|waktu|jam|time)\s*}}""", RegexOption.IGNORE_CASE), dt.timeShort)
    }

    fun synchronizeDynamicFieldsInHtml(
        html: String,
        newReference: String,
        nowMillis: Long = System.currentTimeMillis(),
        refreshRef: Boolean = true
    ): String {
        if (html.isBlank()) return html
        var updated = synchronizeDateTimeInHtml(html, nowMillis)
        updated = updated
            .replace(Regex("""\[(?:NO_REFERENSI|NO_REF|REFERENCE_NO|NO_TRANSAKSI|REF_ID|KODE_REFERENSI|NOMOR_REFERENSI)]""", RegexOption.IGNORE_CASE), newReference)
            .replace(Regex("""\{\{\s*(?:no_referensi|no_ref|reference_no|no_transaksi|ref_id|kode_referensi|nomor_referensi)\s*}}""", RegexOption.IGNORE_CASE), newReference)

        if (refreshRef) {
            val refCellRegex = Regex(
                """(<(?:td|span|div|p)\b[^>]*>[^<]*(?:No(?:mor)?\.?\s*Referensi|Reference(?:\s*No\.?)?)[^<]*</(?:td|span|div|p)>\s*<(?:td|span|div|p)\b[^>]*>)([\s\S]*?)(</(?:td|span|div|p)>)""",
                RegexOption.IGNORE_CASE
            )
            updated = if (refCellRegex.containsMatchIn(updated)) {
                refCellRegex.replace(updated) { m ->
                    val prefix = m.groupValues[1]
                    val currentVal = m.groupValues[2]
                    val suffix = m.groupValues[3]
                    if (currentVal.contains("<")) {
                        val rep = currentVal.replace(Regex("[A-Za-z0-9\\-_]{6,28}"), newReference)
                        "$prefix$rep$suffix"
                    } else {
                        "$prefix$newReference$suffix"
                    }
                }
            } else {
                updated.replace(Regex("""\b(?:TRX|REF|CC|BCA|MANDIRI|BRI|BNI|CIMB|CIMBNIAGA|UOB|MDR)[A-Z0-9\-_]{6,26}\b"""), newReference)
            }
        }
        return updated
    }

    fun applyFreshBankTemplate(
        html: String,
        targetBankKey: String,
        customCancelLink: String = "",
        forceNewBlank: Boolean = false
    ): FreshBankTemplateResult {
        val targetBank = AppPresets.banks.find { it.key == targetBankKey } ?: AppPresets.banks.first()
        val prevBank = AppPresets.detectBankKey(html)
        val nowMillis = System.currentTimeMillis()
        val newRef = generateUniqueBankReference(targetBank.key, nowMillis)
        val dt = buildWibDateTime(nowMillis)
        val cancelLink = customCancelLink.trim().ifEmpty { targetBank.defaultCancelLink }

        if (forceNewBlank || html.isBlank()) {
            val freshHtml = """<!DOCTYPE html>
<html lang="id">
<head>
  <meta charset="UTF-8">
  <title>Konfirmasi Transaksi Resmi - ${targetBank.label}</title>
</head>
<body style="margin: 0; padding: 0; background-color: #f8fafc; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
  <table role="presentation" width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color: #f8fafc; padding: 30px 15px;">
    <tr>
      <td align="center">
        <table role="presentation" width="100%" border="0" cellspacing="0" cellpadding="0" style="max-width: 580px; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.08); border: 1px solid #e2e8f0;">
          <tr>
            <td style="background-color: ${targetBank.primaryHexStr}; padding: 24px; text-align: center;">
              <img src="${targetBank.whiteLogoPath}" alt="Logo Bank ${targetBank.label}" width="140" style="max-height: 48px; max-width: 150px; object-fit: contain; display: block; margin: 0 auto; filter: brightness(0) invert(1); -webkit-filter: brightness(0) invert(1);" />
            </td>
          </tr>
          <tr>
            <td style="padding: 28px 32px;">
              <span style="display: inline-block; background-color: #ecfdf5; color: #047857; font-size: 11px; font-weight: 800; text-transform: uppercase; padding: 4px 10px; border-radius: 12px; border: 1px solid #a7f3d0;">
                ✓ BERHASIL
              </span>
              <h2 style="margin: 12px 0 6px 0; font-size: 20px; font-weight: 800; color: #0f172a;">
                Konfirmasi Transaksi - ${targetBank.fullName}
              </h2>
              <p style="margin: 0 0 20px 0; font-size: 13.5px; color: #64748b; line-height: 1.5;">
                Transaksi Anda telah berhasil diotorisasi dengan aman oleh jaringan perbankan resmi.
              </p>
              <table role="presentation" width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color: #f8fafc; border-radius: 12px; border: 1px solid #e2e8f0; padding: 16px 20px; margin-bottom: 24px;">
                <tr>
                  <td style="padding: 6px 0; font-size: 12.5px; color: #64748b;">Status</td>
                  <td style="padding: 6px 0; font-size: 13px; font-weight: 800; color: #059669; text-align: right;">BERHASIL</td>
                </tr>
                <tr>
                  <td style="padding: 6px 0; font-size: 12.5px; color: #64748b;">Merchant / Penyedia</td>
                  <td style="padding: 6px 0; font-size: 13px; font-weight: 700; color: #1e293b; text-align: right;">SHOPEE INDONESIA</td>
                </tr>
                <tr>
                  <td style="padding: 6px 0; font-size: 12.5px; color: #64748b;">Metode Pembayaran</td>
                  <td style="padding: 6px 0; font-size: 13px; font-weight: 700; color: #1e293b; text-align: right;">Kartu Kredit VISA/MC</td>
                </tr>
                <tr>
                  <td style="padding: 6px 0; font-size: 12.5px; color: #64748b;">Nomor Referensi</td>
                  <td style="padding: 6px 0; font-size: 13px; font-family: monospace; font-weight: 700; color: ${targetBank.primaryHexStr}; text-align: right;">$newRef</td>
                </tr>
                <tr>
                  <td style="padding: 6px 0; font-size: 12.5px; color: #64748b;">Nominal</td>
                  <td style="padding: 6px 0; font-size: 16px; font-weight: 800; color: #0f172a; text-align: right;">Rp 5.000.000</td>
                </tr>
                <tr>
                  <td style="padding: 6px 0; font-size: 12.5px; color: #64748b;">Tanggal & Waktu</td>
                  <td style="padding: 6px 0; font-size: 12.5px; font-weight: 600; color: #334155; text-align: right;">${dt.dateTimeShort} WIB</td>
                </tr>
              </table>
              <div style="text-align: center; margin-top: 24px;">
                <a href="$cancelLink" target="_blank" rel="noopener noreferrer" style="display: inline-block; width: 100%; max-width: 300px; box-sizing: border-box; background-color: ${targetBank.buttonHexStr}; color: #ffffff; text-align: center; text-decoration: none; padding: 13px 24px; font-size: 14px; font-weight: 700; border-radius: 8px;">
                  Batalkan Transaksi ${targetBank.label}
                </a>
              </div>
            </td>
          </tr>
          <tr>
            <td style="background-color: #f1f5f9; padding: 16px; text-align: center; font-size: 11px; color: #94a3b8; border-top: 1px solid #e2e8f0;">
              © 2026 ${targetBank.fullName}. Seluruh hak cipta dilindungi.
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
</body>
</html>"""
            return FreshBankTemplateResult(freshHtml, newRef, prevBank, targetBank)
        }

        var updated = html
        // Purge residual bank hex colors
        for (stale in STALE_BANK_COLORS) {
            if (!stale.equals(targetBank.primaryHexStr, true) && !stale.equals(targetBank.buttonHexStr, true)) {
                updated = updated.replace(Regex(Regex.escape(stale), RegexOption.IGNORE_CASE), targetBank.primaryHexStr)
            }
        }

        // Purge previous bank names
        val otherBanks = AppPresets.banks.filter { it.key != targetBank.key }
        for (b in otherBanks) {
            updated = updated.replace(Regex(Regex.escape(b.fullName), RegexOption.IGNORE_CASE), targetBank.fullName)
            updated = updated.replace(Regex("\\b${Regex.escape(b.label)}\\b"), targetBank.label)
            updated = updated.replace(Regex(Regex.escape(b.logoPath), RegexOption.IGNORE_CASE), targetBank.logoPath)
            updated = updated.replace(Regex(Regex.escape(b.whiteLogoPath), RegexOption.IGNORE_CASE), targetBank.whiteLogoPath)
            updated = updated.replace(Regex(Regex.escape(b.defaultCancelLink), RegexOption.IGNORE_CASE), cancelLink)
        }

        // Update custom cancel link placeholder
        updated = updated.replace(Regex("""\[(?:LINK_PEMBATALAN|CANCEL_LINK|LINK)]""", RegexOption.IGNORE_CASE), cancelLink)

        // Update ref & timestamp
        updated = synchronizeDynamicFieldsInHtml(updated, newRef, nowMillis, refreshRef = true)

        return FreshBankTemplateResult(updated, newRef, prevBank, targetBank)
    }

    /**
     * Parses HTML email receipt into structured sections so Jetpack Compose can render
     * an authentic visual bank receipt card 100% natively without WebView.
     */
    fun parseReceiptPreview(html: String, subject: String = ""): ParsedReceiptPreview {
        val bankKey = AppPresets.detectBankKey("$subject $html")
        val bank = AppPresets.banks.find { it.key == bankKey } ?: AppPresets.banks.first()

        fun cleanHtmlFragment(frag: String): String {
            return HtmlCompat.fromHtml(frag, HtmlCompat.FROM_HTML_MODE_LEGACY)
                .toString()
                .replace('\u00A0', ' ')
                .replace(Regex("\\s+"), " ")
                .trim()
        }

        // Extract heading (h1/h2/h3)
        val headingMatch = Regex("""<h[1-3][^>]*>([\s\S]*?)</h[1-3]>""", RegexOption.IGNORE_CASE).find(html)
        val heading = headingMatch?.let { cleanHtmlFragment(it.groupValues[1]) }
            ?.ifBlank { null }
            ?: subject.ifBlank { "Konfirmasi Transaksi - ${bank.fullName}" }

        // Extract badge text (BERHASIL / MENUNGGU / etc.)
        val badgeMatch = Regex("""✓\s*[A-Z0-9\s]{3,24}""").find(cleanHtmlFragment(html))
        val statusBadge = badgeMatch?.value?.trim() ?: "✓ BERHASIL"

        // Extract key-value rows from <tr> with two <td> cells
        val rows = mutableListOf<Pair<String, String>>()
        val trRegex = Regex("""<tr\b[^>]*>([\s\S]*?)</tr>""", RegexOption.IGNORE_CASE)
        val tdRegex = Regex("""<t[dh]\b[^>]*>([\s\S]*?)</t[dh]>""", RegexOption.IGNORE_CASE)
        trRegex.findAll(html).forEach { tr ->
            val cells = tdRegex.findAll(tr.groupValues[1]).map { cleanHtmlFragment(it.groupValues[1]) }
                .filter { it.isNotBlank() }
                .toList()
            if (cells.size == 2 && cells[0].length <= 45 && cells[1].length <= 80 && !cells[0].contains("©")) {
                rows.add(cells[0] to cells[1])
            }
        }

        // Extract first meaningful intro paragraph (not inside footer)
        val paragraphs = Regex("""<p\b[^>]*>([\s\S]*?)</p>""", RegexOption.IGNORE_CASE)
            .findAll(html)
            .map { cleanHtmlFragment(it.groupValues[1]) }
            .filter { it.length > 15 && !it.startsWith("©") }
            .toList()
        val introText = paragraphs.firstOrNull() ?: ""

        // Extract CTA anchor
        val ctaMatch = Regex("""<a\b[^>]*href=["']([^"']+)["'][^>]*>([\s\S]*?)</a>""", RegexOption.IGNORE_CASE).find(html)
        val ctaUrl = ctaMatch?.groupValues?.get(1)?.trim()
        val ctaLabel = ctaMatch?.let { cleanHtmlFragment(it.groupValues[2]) }?.ifBlank { null }

        // Extract footer copyright
        val footerMatch = Regex("""©\s*20\d\d[^<\n]{5,100}""").find(cleanHtmlFragment(html))
        val footerText = footerMatch?.value?.trim()

        val fallbackText = HtmlCompat.fromHtml(
            html.replace(Regex("<style[^>]*>[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), ""),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        ).toString().trim()

        return ParsedReceiptPreview(
            bank = bank,
            statusBadge = statusBadge,
            heading = heading,
            introText = introText,
            rows = rows.distinctBy { it.first },
            ctaLabel = ctaLabel,
            ctaUrl = ctaUrl,
            footerText = footerText,
            fallbackText = fallbackText
        )
    }

    fun computeSmtpDiagnostic(
        smtpConfig: SmtpConfig,
        smtpState: SmtpConnectionState,
        smtpStatusMessage: String,
        logs: List<LogEntry>
    ): SmtpDiagnosticResult {
        val hasUser = smtpConfig.username.isNotBlank()
        val hasPass = smtpConfig.password.isNotBlank()
        val host = smtpConfig.host.trim().lowercase()
        val port = smtpConfig.port.trim().ifEmpty { "587" }
        val passCheck = validateAppPassword(smtpConfig.password, smtpConfig.username)

        val latestErrLog = logs.firstOrNull {
            it.type == LogType.ERROR || listOf("smtp", "eauth", "gagal", "failed", "refused", "timed out", "cookie_check")
                .any { kw -> it.message.contains(kw, ignoreCase = true) }
        }
        val latestErrMsg = latestErrLog?.message
            ?: (if (smtpState == SmtpConnectionState.DISCONNECTED) smtpStatusMessage else "")
        val lowerErr = latestErrMsg.lowercase()

        if (lowerErr.contains("cookie_check") || lowerErr.contains("ais-dev") || lowerErr.contains("halaman html")) {
            return SmtpDiagnosticResult(
                severity = "error",
                badge = "SERVER API AUTH",
                title = "URL Backend Dev Membutuhkan Cookie Browser",
                category = "Koneksi Backend Relay",
                rawError = latestErrMsg,
                rootCause = "Aplikasi Android mengakses URL dev AI Studio (ais-dev) tanpa cookie browser sehingga diblokir gateway.",
                steps = listOf(
                    SmtpDiagnosticStep(1, "Publish / Deploy Backend", "Deploy server Node.js Anda atau jalankan di server publik HTTPS.", "accounts"),
                    SmtpDiagnosticStep(2, "Ubah URL Server Relay di Tab Akun", "Buka Akun -> Koneksi Server Relay API, masukkan URL server publik lalu klik Tes Server.", "accounts")
                )
            )
        }

        if (!hasUser || !hasPass) {
            return SmtpDiagnosticResult(
                severity = "unconfigured",
                badge = "BELUM DIKONFIGURASI",
                title = "Kredensial Akun SMTP Belum Lengkap",
                category = "Konfigurasi Akun",
                rawError = latestErrMsg.ifBlank { "Username atau password SMTP masih kosong." },
                rootCause = "Aplikasi belum memiliki akun pengirim terverifikasi untuk merelay email keluar.",
                steps = listOf(
                    SmtpDiagnosticStep(1, "Buka Tab Akun SMTP", "Pindah ke tab Akun di bagian navigasi menu bawah.", "accounts"),
                    SmtpDiagnosticStep(2, "Pilih Provider atau Isi Host", "Masukkan email (Gmail/Yahoo/custom), sistem otomatis mendeteksi Host & Port."),
                    SmtpDiagnosticStep(3, "Gunakan Sandi Aplikasi (App Password)", "Untuk Gmail/Yahoo, gunakan 16 digit Sandi Aplikasi, bukan kata sandi biasa."),
                    SmtpDiagnosticStep(4, "Simpan & Verifikasi", "Klik 'Simpan Setelan' lalu 'Uji Koneksi'.", "accounts")
                )
            )
        }

        if (passCheck.isGmailOrYahoo && !passCheck.isExact16) {
            return SmtpDiagnosticResult(
                severity = "error",
                badge = "FORMAT PASSWORD SALAH",
                title = "Format App Password Tidak Sesuai (Wajib 16 Karakter)",
                category = "Kredensial Gmail/Yahoo",
                rawError = "Panjang saat ini: ${passCheck.length} karakter. Gmail/Yahoo mewajibkan tepat 16 karakter tanpa spasi.",
                rootCause = "Google dan Yahoo menolak sandi akun biasa dan mewajibkan Sandi Aplikasi 16 digit.",
                steps = listOf(
                    SmtpDiagnosticStep(1, "Aktifkan Verifikasi 2 Langkah (2FA)", "Pastikan 2-Step Verification pada akun Google Anda aktif."),
                    SmtpDiagnosticStep(2, "Buat Sandi Aplikasi di Google", "Buka myaccount.google.com/apppasswords lalu buat Sandi Aplikasi baru."),
                    SmtpDiagnosticStep(3, "Perbarui di Tab Akun", "Masukkan 16 karakter ke kolom Password SMTP tanpa spasi.", "accounts")
                )
            )
        }

        if (lowerErr.contains("535") || lowerErr.contains("eauth") || lowerErr.contains("invalid login") || lowerErr.contains("badcredentials")) {
            return SmtpDiagnosticResult(
                severity = "error",
                badge = "AUTH GAGAL (535)",
                title = "Autentikasi Ditolak oleh Server SMTP (Error 535)",
                category = "Kredensial & Sandi Aplikasi",
                rawError = latestErrMsg,
                rootCause = "Server SMTP menolak email atau kata sandi. Gmail/Yahoo memblokir password login biasa.",
                steps = listOf(
                    SmtpDiagnosticStep(1, "Gunakan App Password 16 Digit", "Sandi web browser tidak dapat dipakai untuk SMTP pihak ketiga.", "accounts"),
                    SmtpDiagnosticStep(2, "Pastikan Alamat Email Lengkap", "Username harus lengkap (saat ini: '${smtpConfig.username}')."),
                    SmtpDiagnosticStep(3, "Uji Ulang Koneksi", "Klik 'Pindai' setelah memperbarui sandi.", "test")
                )
            )
        }

        if (lowerErr.contains("econnrefused") || lowerErr.contains("etimedout") || lowerErr.contains("timeout") || lowerErr.contains("connection refused")) {
            val is465 = port == "465"
            return SmtpDiagnosticResult(
                severity = "error",
                badge = "KONEKSI TIMEOUT / DITOLAK",
                title = "Koneksi ke Server Relay Ditolak atau Waktu Habis",
                category = "Jaringan, Port & Firewall",
                rawError = latestErrMsg,
                rootCause = "Server ${host.ifBlank { "smtp" }}:$port tidak merespons atau port tidak sinkron dengan enkripsi.",
                steps = listOf(
                    SmtpDiagnosticStep(
                        1,
                        if (is465) "Sinkronisasi Port 465 (SSL/TLS)" else "Sinkronisasi Port 587 (STARTTLS)",
                        if (is465) "Gunakan Port 587 STARTTLS jika Port 465 terblokir." else "Gunakan Port 465 SSL_TLS jika Port 587 timeout.",
                        "accounts"
                    ),
                    SmtpDiagnosticStep(2, "Uji Ulang Koneksi", "Klik 'Pindai' untuk menguji ulang jalur TCP ke server relay.", "test")
                )
            )
        }

        if (smtpState == SmtpConnectionState.CONNECTED) {
            return SmtpDiagnosticResult(
                severity = "healthy",
                badge = "RELAY NORMAL",
                title = "Relay SMTP Terverifikasi & Siap Digunakan",
                category = "Koneksi Normal",
                rawError = null,
                rootCause = "Server relay terhubung aktif ke ${smtpConfig.host}:${smtpConfig.port}.",
                steps = listOf(
                    SmtpDiagnosticStep(1, "Kredensial Valid & Aktif", "Akun ${smtpConfig.username} terautentikasi siap mengirim email."),
                    SmtpDiagnosticStep(2, "Pemeriksaan Berkala", "Klik 'Pindai' kapan saja untuk mengecek status koneksi.", "test")
                )
            )
        }

        return SmtpDiagnosticResult(
            severity = if (smtpState == SmtpConnectionState.DISCONNECTED) "warning" else "healthy",
            badge = if (smtpState == SmtpConnectionState.DISCONNECTED) "TERPUTUS" else "SISTEM SIAP",
            title = if (smtpState == SmtpConnectionState.DISCONNECTED) "Koneksi SMTP Belum Terverifikasi" else "Diagnostik Siap Memantau Relay",
            category = "Status Relay",
            rawError = latestErrMsg.ifBlank { null },
            rootCause = smtpStatusMessage.ifBlank { "Sistem siap memantau dan memverifikasi jalur relay." },
            steps = listOf(
                SmtpDiagnosticStep(1, "Jalankan Tes Verifikasi", "Tekan 'Pindai' untuk memverifikasi status koneksi server.", "test"),
                SmtpDiagnosticStep(2, "Periksa Akun SMTP", "Pastikan host, port, dan App Password valid.", "accounts")
            )
        )
    }
}
