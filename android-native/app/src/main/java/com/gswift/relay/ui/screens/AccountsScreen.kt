package com.gswift.relay.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gswift.relay.data.model.SmtpConfig
import com.gswift.relay.data.model.SmtpConnectionState
import com.gswift.relay.ui.theme.EmeraldOnline
import com.gswift.relay.ui.theme.NavyDark
import com.gswift.relay.ui.theme.NavyPrimary
import com.gswift.relay.ui.theme.RoseError
import com.gswift.relay.ui.viewmodel.MainUiState

@Composable
fun AccountsScreen(
    uiState: MainUiState,
    onUpdateConfig: (SmtpConfig) -> Unit,
    onSaveAndVerify: (SmtpConfig) -> Unit,
    onAutoDetectDomain: (String) -> Unit
) {
    val cfg = uiState.smtpConfig
    var showPassword by remember { mutableStateOf(false) }
    var formExpanded by remember { mutableStateOf(true) }
    var serverAdvancedExpanded by remember { mutableStateOf(false) }

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
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(4.dp, RoundedCornerShape(15.dp))
                    .clip(RoundedCornerShape(15.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(NavyPrimary, NavyDark)
                        )
                    ),
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
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
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
                            onSaveAndVerify(cfg)
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
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "App Password / Password SMTP",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
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
                        }

                        // Expandable Server Host & Port + 7-Layer Detection
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
                                        text = "Server Host & Port (${cfg.host.ifBlank { "Otomatis" }})",
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
                                        Text("Deteksi Otomatis Host & Port (7-Layer)", fontSize = 12.sp)
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
                                }
                            }
                        }

                        // Action Buttons matching Screenshot 5 ("Uji Koneksi" + "Simpan Setelan")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onSaveAndVerify(cfg) },
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
                                onClick = { onSaveAndVerify(cfg) },
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
    }
}
