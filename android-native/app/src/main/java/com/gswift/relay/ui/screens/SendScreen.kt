package com.gswift.relay.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.Monitor
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.text.HtmlCompat
import com.gswift.relay.data.model.AppPresets
import com.gswift.relay.ui.theme.ClaudeStarburstLogo
import com.gswift.relay.ui.theme.EmeraldOnline
import com.gswift.relay.ui.theme.NavyDark
import com.gswift.relay.ui.theme.NavyPrimary
import com.gswift.relay.ui.theme.RoseError
import com.gswift.relay.ui.viewmodel.MainUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SendScreen(
    uiState: MainUiState,
    onUpdateFields: (to: String?, subject: String?, message: String?) -> Unit,
    onSendClick: () -> Unit,
    onClearClick: () -> Unit,
    onApplyLastSent: () -> Unit,
    onDismissBanner: () -> Unit,
    onGoToAccounts: () -> Unit,
    onGoToAi: () -> Unit,
    onGoToTemplates: () -> Unit,
    extractEmails: (String) -> List<String>
) {
    var previewMobileMode by remember { mutableStateOf(true) }
    var showRawHtml by remember { mutableStateOf(false) }
    var manualDraftMode by remember { mutableStateOf(false) }

    val validEmails = remember(uiState.recipientInput) { extractEmails(uiState.recipientInput) }
    val detectedBankKey = remember(uiState.subjectInput, uiState.messageInput) {
        AppPresets.detectBankKey("${uiState.subjectInput} ${uiState.messageInput}")
    }
    val detectedBank = remember(detectedBankKey) {
        AppPresets.banks.find { it.key == detectedBankKey } ?: AppPresets.banks.first()
    }
    val hasDraft = uiState.messageInput.isNotBlank() || manualDraftMode

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .padding(bottom = if (hasDraft) 84.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Main Card matching Screenshot 1 (Kirim Tab)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(22.dp))
            ) {
                Column {
                    // Upper Anti-Spam Security Header Banner
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDCFCE7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF16A34A))
                                    )
                                }
                                Text(
                                    text = "SISTEM ANTI-SPAM GMAIL \u2013 PENGIRIM",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp,
                                    color = Color(0xFF64748B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "AKTIF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = NavyPrimary,
                                maxLines = 1
                            )
                        }

                        // Sender Account Banner (Dashed prompt when empty, deep navy card when active)
                        if (uiState.smtpConfig.username.isNotBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(NavyPrimary, NavyDark)
                                        )
                                    )
                                    .clickable { onGoToAccounts() }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.Shield,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "AKUN PENGIRIM AKTIF",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFBFDBFE),
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = uiState.smtpConfig.username,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Surface(
                                    color = Color.White.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF4ADE80))
                                        )
                                        Text(
                                            text = "SECURE RELAY",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .dashedBorder(
                                        color = Color(0xFFCBD5E1),
                                        cornerRadius = 12.dp,
                                        strokeWidth = 1.dp,
                                        dashLength = 8f,
                                        gapLength = 6f
                                    )
                                    .clickable { onGoToAccounts() }
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Rounded.WarningAmber,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "BELUM ADA AKUN PENGIRIM",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontStyle = FontStyle.Italic,
                                    letterSpacing = 0.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Form Body
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Recipient Field Header with Auto-Filter Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "EMAIL PENERIMA",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.4.sp,
                                    color = NavyPrimary
                                )
                                Surface(
                                    color = Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(7.dp),
                                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                                ) {
                                    Text(
                                        text = "Auto-Filter Aktif",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyPrimary,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (uiState.recipientInput.isNotBlank() || uiState.subjectInput.isNotBlank() || uiState.messageInput.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        manualDraftMode = false
                                        onClearClick()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.DeleteOutline,
                                        contentDescription = "Bersihkan Form",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = uiState.recipientInput,
                            onValueChange = { onUpdateFields(it, null, null) },
                            placeholder = {
                                Text(
                                    "Tempel email atau teks kontak...",
                                    fontSize = 13.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (validEmails.isNotEmpty()) EmeraldOnline else NavyPrimary,
                                unfocusedBorderColor = if (validEmails.isNotEmpty()) EmeraldOnline else Color(0xFFCBD5E1),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            trailingIcon = {
                                if (uiState.recipientInput.isNotBlank()) {
                                    IconButton(onClick = { onUpdateFields("", null, null) }) {
                                        Icon(
                                            Icons.Rounded.Close,
                                            contentDescription = "Hapus",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            maxLines = 3
                        )

                        if (validEmails.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                validEmails.forEach { email ->
                                    AssistChip(
                                        onClick = {},
                                        leadingIcon = {
                                            Icon(
                                                Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                tint = EmeraldOnline,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        label = { Text(email, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        // Subject Field
                        Text(
                            text = "SUBJEK EMAIL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.4.sp,
                            color = NavyPrimary
                        )
                        OutlinedTextField(
                            value = uiState.subjectInput,
                            onValueChange = { onUpdateFields(null, it, null) },
                            placeholder = {
                                Text(
                                    "[Resmi] Pemberitahuan Transaksi...",
                                    fontSize = 13.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NavyPrimary,
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            singleLine = true,
                            trailingIcon = {
                                if (uiState.subjectInput.isNotBlank()) {
                                    IconButton(onClick = { onUpdateFields(null, "", null) }) {
                                        Icon(
                                            Icons.Rounded.Close,
                                            contentDescription = "Kosongkan",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        )

                        // Last Sent Email Quick-Load Pill / Empty Row (1:1 Screenshot)
                        val lastSent = uiState.lastSentEmail
                        if (lastSent != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                    .clickable { onApplyLastSent() }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD1FAE5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.DoneAll,
                                        contentDescription = null,
                                        tint = EmeraldOnline,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Text(
                                    text = lastSent.to,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = lastSent.timestamp,
                                    fontSize = 9.sp,
                                    color = Color(0xFF64748B)
                                )
                                Surface(
                                    color = NavyPrimary,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "GUNAKAN",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .dashedBorder(
                                        color = Color(0xFFCBD5E1),
                                        cornerRadius = 14.dp,
                                        strokeWidth = 1.dp,
                                        dashLength = 8f,
                                        gapLength = 6f
                                    )
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "TERAKHIR\nDIKIRIM:",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "Belum ada riwayat email yang dikirim",
                                    fontSize = 11.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Dashed Empty Draft State OR Live Preview/Editor matching Screenshot 1
                        if (!hasDraft) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0xFFFCFDFE))
                                    .dashedBorder(
                                        color = Color(0xFF1E293B),
                                        cornerRadius = 18.dp,
                                        strokeWidth = 1.6.dp,
                                        dashLength = 12f,
                                        gapLength = 10f
                                    )
                                    .padding(horizontal = 16.dp, vertical = 22.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFEEF2FF))
                                        .border(1.5.dp, Color(0xFF6366F1), RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MailOutline,
                                        contentDescription = null,
                                        tint = Color(0xFF4F46E5),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Text(
                                    text = "BELUM ADA DRAF EMAIL",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.4.sp,
                                    color = Color(0xFF0F172A),
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "Gunakan rekomendasi dari Claude Mythos atau pilih dari koleksi Templates untuk memuat draf email ke sini.",
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = Color(0xFF64748B),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )

                                Spacer(Modifier.height(2.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onGoToAi,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.2.dp, Color(0xFFF59E0B)),
                                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                                        ) {
                                            ClaudeStarburstLogo(
                                                modifier = Modifier.size(18.dp),
                                                animated = true
                                            )
                                            Text(
                                                text = "BUKA CLAUDE\nMYTHOS",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                lineHeight = 12.sp,
                                                color = Color(0xFF1E293B),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = onGoToTemplates,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Description,
                                                contentDescription = null,
                                                tint = Color(0xFF475569),
                                                modifier = Modifier.size(17.dp)
                                            )
                                            Text(
                                                text = "PILIH\nTEMPLATE",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                lineHeight = 12.sp,
                                                color = Color(0xFF1E293B),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        manualDraftMode = true
                                        showRawHtml = true
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.EditNote,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Atau tulis draf manual",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        } else {
                            // Live Pratinjau Card (when draft loaded or typing)
                            if (uiState.messageInput.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFF8FAFC))
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(7.dp)
                                                        .clip(CircleShape)
                                                        .background(EmeraldOnline)
                                                )
                                                Text(
                                                    text = "PRATINJAU (${detectedBank.label})",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFF334155)
                                                )
                                            }

                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFE2E8F0))
                                                    .padding(2.dp),
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (!showRawHtml && previewMobileMode) Color.White else Color.Transparent)
                                                        .clickable {
                                                            showRawHtml = false
                                                            previewMobileMode = true
                                                        }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    Icon(Icons.Rounded.PhoneAndroid, contentDescription = null, modifier = Modifier.size(12.dp))
                                                    Text("HP", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Row(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (!showRawHtml && !previewMobileMode) Color.White else Color.Transparent)
                                                        .clickable {
                                                            showRawHtml = false
                                                            previewMobileMode = false
                                                        }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    Icon(Icons.Rounded.Monitor, contentDescription = null, modifier = Modifier.size(12.dp))
                                                    Text("Desktop", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Row(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (showRawHtml) Color.White else Color.Transparent)
                                                        .clickable { showRawHtml = !showRawHtml }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text("Edit", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        if (!showRawHtml) {
                                            val plainPreview = remember(uiState.messageInput) {
                                                HtmlCompat.fromHtml(
                                                    uiState.messageInput,
                                                    HtmlCompat.FROM_HTML_MODE_LEGACY
                                                ).toString().trim()
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(if (previewMobileMode) Color(0xFFF1F5F9) else Color.White)
                                                    .padding(if (previewMobileMode) 12.dp else 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Surface(
                                                    color = Color.White,
                                                    shape = RoundedCornerShape(14.dp),
                                                    border = BorderStroke(
                                                        1.dp,
                                                        Color(detectedBank.colorHex).copy(alpha = 0.25f)
                                                    ),
                                                    modifier = Modifier.fillMaxWidth(if (previewMobileMode) 0.92f else 1f)
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(12.dp),
                                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(4.dp)
                                                                .clip(CircleShape)
                                                                .background(Color(detectedBank.colorHex))
                                                        )
                                                        Text(
                                                            text = uiState.subjectInput.ifBlank { "Draf Email ${detectedBank.label}" },
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = Color(0xFF0F172A)
                                                        )
                                                        Text(
                                                            text = plainPreview.ifBlank { uiState.messageInput },
                                                            fontSize = 12.sp,
                                                            color = Color(0xFF334155),
                                                            lineHeight = 18.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (showRawHtml || uiState.messageInput.isBlank()) {
                                OutlinedTextField(
                                    value = uiState.messageInput,
                                    onValueChange = { onUpdateFields(null, null, it) },
                                    label = { Text("Konten Pesan / HTML Draf") },
                                    placeholder = { Text("Tulis isi email atau gunakan AI untuk draf otomatis...", fontSize = 13.sp) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(165.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = if (uiState.messageInput.contains("<")) FontFamily.Monospace else FontFamily.Default,
                                        fontSize = 12.sp
                                    ),
                                    trailingIcon = {
                                        if (uiState.messageInput.isBlank() && manualDraftMode) {
                                            IconButton(onClick = { manualDraftMode = false }) {
                                                Icon(Icons.Rounded.Close, contentDescription = "Tutup Editor")
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Sticky Action Bar when draft is loaded/active (Gold Send Button)
        if (hasDraft) {
            Surface(
                color = Color.White.copy(alpha = 0.96f),
                shadowElevation = 10.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Column {
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onGoToAi,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            ClaudeStarburstLogo(modifier = Modifier.size(16.dp), animated = false)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "AI Draf",
                                color = Color(0xFF1E293B),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFFFFD700),
                                            Color(0xFFFFC000),
                                            Color(0xFFE6AC00)
                                        )
                                    )
                                )
                                .clickable(enabled = !uiState.isSendingEmail) { onSendClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (uiState.isSendingEmail) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.Rounded.Send,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = if (uiState.isSendingEmail) "MENGIRIM..." else "KIRIM SEKARANG",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.6.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Glowing Orb Delivery Modal
    uiState.deliveryBanner?.let { banner ->
        Dialog(onDismissRequest = onDismissBanner) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .clip(CircleShape)
                            .background(if (banner.isSuccess) Color(0xFFECFDF5) else Color(0xFFFFF1F2))
                            .border(
                                3.dp,
                                if (banner.isSuccess) EmeraldOnline else RoseError,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (banner.isSuccess) Icons.Rounded.DoneAll else Icons.Rounded.Close,
                            contentDescription = null,
                            tint = if (banner.isSuccess) EmeraldOnline else RoseError,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = banner.subtitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (banner.isSuccess) EmeraldOnline else RoseError,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = banner.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = banner.detail,
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center
                    )

                    banner.tip?.let { tip ->
                        Surface(
                            color = Color(0xFFFFF1F2),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "Solusi: $tip",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF9F1239),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!banner.isSuccess) {
                            OutlinedButton(
                                onClick = {
                                    onDismissBanner()
                                    onGoToAccounts()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Atur SMTP", fontSize = 12.sp)
                            }
                        }
                        Button(
                            onClick = onDismissBanner,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (banner.isSuccess) EmeraldOnline else NavyPrimary
                            )
                        ) {
                            Text("Tutup", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp,
    strokeWidth: Dp,
    dashLength: Float,
    gapLength: Float
): Modifier = drawBehind {
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength), 0f)
    )
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
        style = stroke
    )
}
