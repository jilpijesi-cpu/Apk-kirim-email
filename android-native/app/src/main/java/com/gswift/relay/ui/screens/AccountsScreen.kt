package com.gswift.relay.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gswift.relay.data.model.AppPresets
import com.gswift.relay.data.model.SmtpConfig
import com.gswift.relay.data.model.SmtpConnectionState
import com.gswift.relay.data.util.BankDraftEngine
import com.gswift.relay.ui.theme.EmeraldOnline
import com.gswift.relay.ui.theme.NavyDark
import com.gswift.relay.ui.theme.NavyPrimary
import com.gswift.relay.ui.theme.RoseError
import com.gswift.relay.ui.viewmodel.MainUiState

@Composable
fun AccountsScreen(
    uiState: MainUiState,
    onUpdateConfig: (SmtpConfig) -> Unit,
    onSaveSettings: (SmtpConfig) -> Unit,
    onTestAndSendSample: (SmtpConfig) -> Unit,
    onVerifyOnly: () -> Unit,
    onAutoDetectDomain: (String) -> Unit,
    onToggleAutoDetect: (Boolean) -> Unit,
    onUpdateServerUrl: (String) -> Unit,
    onGoToSend: () -> Unit
) {
    val cfg = uiState.smtpConfig
    val uriHandler = LocalUriHandler.current
    var showPassword by remember { mutableStateOf(false) }
    var formExpanded by remember { mutableStateOf(true) }
    var serverAdvancedExpanded by remember { mutableStateOf(false) }
    var showError535Guide by remember { mutableStateOf(false) }
    var serverUrlDraft by remember(uiState.serverBaseUrl) { mutableStateOf(uiState.serverBaseUrl) }

    val passValidation = remember(cfg.password, cfg.username) {
        BankDraftEngine.validateAppPassword(cfg.password, cfg.username)
    }

    val statusColor = when (uiState.smtpState) {
        SmtpConnectionState.CONNECTED -> EmeraldOnline
        SmtpConnectionState.DISCONNECTED -> RoseError
        SmtpConnectionState.CHECKING -> Color(0xFFD97706)
        SmtpConnectionState.UNCONFIGURED -> Color(0xFF94A3B8)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Row matching Screenshot 5 (Pengaturan SMTP)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, RoundedCornerShape(15.dp))
                        .clip(RoundedCornerShape(15.dp))
                        .background(Brush.verticalGradient(listOf(NavyPrimary, NavyDark))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column {
                    Text(
                        text = "Pengaturan SMTP",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Akun pengirim email keluar",
                        fontSize = 12.5.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            if (uiState.smtpState == SmtpConnectionState.CONNECTED) {
                Button(
                    onClick = onGoToSend,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Mulai Kirim", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
        }

        // SMTP Status Card matching Screenshot 5
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(statusColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (uiState.smtpState) {
                            SmtpConnectionState.CONNECTED -> "SMTP SIAP DIGUNAKAN"
                            SmtpConnectionState.DISCONNECTED -> "KONEKSI SMTP TERPUTUS"
                            SmtpConnectionState.CHECKING -> "MEMVERIFIKASI KONEKSI..."
                            SmtpConnectionState.UNCONFIGURED -> "SMTP BELUM DIKONFIGURASI"
                        },
                        fontWeight = FontWeight.Black,
                        fontSize = 12.5.sp,
                        letterSpacing = 0.3.sp,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (uiState.smtpState == SmtpConnectionState.UNCONFIGURED) {
                            "Masukkan data akun di bawah"
                        } else {
                            uiState.smtpStatusMessage
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .size(width = 46.dp, height = 34.dp)
                        .clickable(enabled = !uiState.isVerifyingSmtp) {
                            onVerifyOnly()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (uiState.isVerifyingSmtp) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = NavyPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Verifikasi Ulang",
                                tint = Color(0xFF1E293B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Active Connected Summary Card with "Kirim Email Uji Coba"
        if (uiState.smtpState == SmtpConnectionState.CONNECTED && cfg.username.isNotBlank()) {
            Surface(
                color = Color(0xFFECFDF5),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFF6EE7B7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AKUN TERHUBUNG AKTIF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF047857)
                        )
                        Text(
                            text = cfg.username,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF065F46),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${cfg.host}:${cfg.port} (${cfg.connectionType})",
                            fontSize = 11.sp,
                            color = Color(0xFF047857)
                        )
                    }
                    OutlinedButton(
                        onClick = { onTestAndSendSample(cfg) },
                        enabled = !uiState.isVerifyingSmtp,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, EmeraldOnline),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Rounded.Send, contentDescription = null, tint = EmeraldOnline, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Kirim Uji Coba", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = EmeraldOnline)
                    }
                }
            }
        }

        // Collapsible Form Card matching Screenshot 5 (FORMULIR AKUN SMTP)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(22.dp))
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { formExpanded = !formExpanded }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "FORMULIR AKUN SMTP",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.4.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Icon(
                        imageVector = if (formExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFF64748B)
                    )
                }

                if (formExpanded) {
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Nama Pengirim (Display Name)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            OutlinedTextField(
                                value = cfg.fromName,
                                onValueChange = { onUpdateConfig(cfg.copy(fromName = it)) },
                                placeholder = { Text("Contoh: CS Resmi BCA / Shopee", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NavyPrimary,
                                    unfocusedBorderColor = Color(0xFFCBD5E1)
                                )
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Username / Alamat Email",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            OutlinedTextField(
                                value = cfg.username,
                                onValueChange = { onUpdateConfig(cfg.copy(username = it, senderEmail = it)) },
                                placeholder = { Text("akun@gmail.com", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NavyPrimary,
                                    unfocusedBorderColor = Color(0xFFCBD5E1)
                                )
                            )

                            uiState.detectedLookupInfo?.let { info ->
                                Surface(
                                    color = Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Deteksi Domain: ${info.name}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NavyPrimary
                                            )
                                            Text(
                                                text = "${info.host}:${info.port} (${info.connectionType})",
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color(0xFF334155)
                                            )
                                        }
                                        TextButton(
                                            onClick = {
                                                onUpdateConfig(
                                                    cfg.copy(
                                                        host = info.host,
                                                        port = info.port,
                                                        connectionType = info.connectionType,
                                                        dailyLimit = info.dailyLimit
                                                    )
                                                )
                                            }
                                        ) {
                                            Text("Terapkan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "App Password / Password SMTP",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                if (cfg.password.isNotBlank() && passValidation.isGmailOrYahoo) {
                                    Surface(
                                        color = if (passValidation.isExact16) Color(0xFFECFDF5) else Color(0xFFFFFBEB),
                                        shape = CircleShape,
                                        border = BorderStroke(
                                            1.dp,
                                            if (passValidation.isExact16) Color(0xFF6EE7B7) else Color(0xFFFCD34D)
                                        )
                                    ) {
                                        Text(
                                            text = if (passValidation.isExact16) "16 Digit Valid" else "${passValidation.length}/16 Karakter",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (passValidation.isExact16) EmeraldOnline else Color(0xFF92400E),
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = cfg.password,
                                onValueChange = { onUpdateConfig(cfg.copy(password = it)) },
                                placeholder = { Text("16 karakter Sandi Aplikasi Gmail", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                            contentDescription = "Tampilkan sandi"
                                        )
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NavyPrimary,
                                    unfocusedBorderColor = Color(0xFFCBD5E1)
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (passValidation.hasSpaces) {
                                    TextButton(
                                        onClick = { onUpdateConfig(cfg.copy(password = passValidation.clean)) },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Hapus Spasi (${passValidation.length} digit)", fontSize = 11.sp, color = NavyPrimary)
                                    }
                                } else {
                                    Spacer(Modifier.width(1.dp))
                                }
                                TextButton(
                                    onClick = { showError535Guide = !showError535Guide },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Rounded.HelpOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Solusi Error 535", fontSize = 11.sp)
                                }
                            }

                            AnimatedVisibility(visible = showError535Guide) {
                                Surface(
                                    color = Color(0xFFFFFBEB),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Wajib Gunakan App Password (16 Digit)",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E)
                                        )
                                        Text(
                                            text = "Gmail dan Yahoo menolak sandi login biasa. Aktifkan Verifikasi 2 Langkah lalu buat App Password 16 digit tanpa spasi.",
                                            fontSize = 10.5.sp,
                                            lineHeight = 15.sp,
                                            color = Color(0xFF78350F)
                                        )
                                        OutlinedButton(
                                            onClick = {
                                                runCatching { uriHandler.openUri("https://myaccount.google.com/apppasswords") }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Buka Google App Passwords", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Expandable Server Host & Port + Provider Presets + 7-Layer Detection
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { serverAdvancedExpanded = !serverAdvancedExpanded },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Server Host & Port (${cfg.host.ifBlank { "Otomatis" }}:${cfg.port})",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF475569)
                                    )
                                    Icon(
                                        imageVector = if (serverAdvancedExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                if (serverAdvancedExpanded) {
                                    // Provider Quick Presets
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        AppPresets.providerPresets.forEach { preset ->
                                            FilterChip(
                                                selected = cfg.host.equals(preset.host, true),
                                                onClick = {
                                                    onUpdateConfig(
                                                        cfg.copy(
                                                            host = preset.host,
                                                            port = preset.port,
                                                            connectionType = preset.connectionType,
                                                            dailyLimit = preset.dailyLimit
                                                        )
                                                    )
                                                },
                                                label = { Text(preset.name.substringBefore(" "), fontSize = 10.5.sp) }
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { onAutoDetectDomain(cfg.username) },
                                        enabled = !uiState.isLookingUpDomain,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        if (uiState.isLookingUpDomain) {
                                            CircularProgressIndicator(modifier = Modifier.size(15.dp), strokeWidth = 2.dp)
                                        } else {
                                            Icon(Icons.Rounded.AutoFixHigh, contentDescription = null, modifier = Modifier.size(15.dp))
                                        }
                                        Spacer(Modifier.width(6.dp))
                                        Text("Deteksi Otomatis Host & Port (7-Layer)", fontSize = 11.5.sp)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = cfg.host,
                                            onValueChange = { onUpdateConfig(cfg.copy(host = it)) },
                                            label = { Text("Host SMTP") },
                                            singleLine = true,
                                            modifier = Modifier.weight(2f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        OutlinedTextField(
                                            value = cfg.port,
                                            onValueChange = { onUpdateConfig(cfg.copy(port = it)) },
                                            label = { Text("Port") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf("STARTTLS", "SSL_TLS").forEach { mode ->
                                            FilterChip(
                                                selected = cfg.connectionType == mode,
                                                onClick = {
                                                    val nextPort = if (mode == "SSL_TLS") "465" else "587"
                                                    onUpdateConfig(cfg.copy(connectionType = mode, port = nextPort))
                                                },
                                                label = { Text(mode, fontSize = 11.sp) }
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Auto-Detect Domain saat mengetik",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF475569)
                                        )
                                        Switch(
                                            checked = uiState.autoDetectSmtp,
                                            onCheckedChange = onToggleAutoDetect
                                        )
                                    }
                                }
                            }
                        }

                        // Action Buttons matching Screenshot 5 ("Uji Koneksi" + "Simpan Setelan")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onTestAndSendSample(cfg) },
                                enabled = cfg.username.isNotBlank() && cfg.password.isNotBlank() && !uiState.isVerifyingSmtp,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Icon(
                                    Icons.Rounded.VerifiedUser,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Uji Koneksi", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }

                            Button(
                                onClick = { onSaveSettings(cfg) },
                                enabled = !uiState.isVerifyingSmtp,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                            ) {
                                if (uiState.isVerifyingSmtp) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.Rounded.CheckCircleOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                Spacer(Modifier.width(6.dp))
                                Text("Simpan Setelan", fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // Backend Relay API Server Card (crucial so standalone APK can point to deployed server instead of ais-dev cookie URL)
        val isDevCookieUrl = uiState.serverBaseUrl.contains("ais-dev-", ignoreCase = true)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(22.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Dns, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                        Text(
                            text = "KONEKSI SERVER RELAY API",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Surface(
                        color = if (uiState.backendOnline) Color(0xFFECFDF5) else Color(0xFFFFF1F2),
                        shape = CircleShape
                    ) {
                        Text(
                            text = if (uiState.backendOnline) "Server Online" else "Offline",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.backendOnline) EmeraldOnline else RoseError,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                if (isDevCookieUrl && !uiState.backendOnline) {
                    Surface(
                        color = Color(0xFFFFFBEB),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                            Text(
                                text = "URL dev (ais-dev) membutuhkan cookie browser AI Studio. Masukkan URL server publik (Cloud Run / VPS / Render) agar AI & SMTP Relay aktif di APK.",
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = serverUrlDraft,
                    onValueChange = { serverUrlDraft = it },
                    label = { Text("URL Backend Server Relay") },
                    placeholder = { Text("https://relay-anda.run.app/") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { onUpdateServerUrl(serverUrlDraft) },
                    enabled = serverUrlDraft.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Simpan & Tes Server", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
