package com.gswift.relay.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gswift.relay.data.model.SmtpConfig
import com.gswift.relay.data.model.SmtpConnectionState
import com.gswift.relay.ui.theme.EmeraldOnline
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Status Banner
        val statusColor = when (uiState.smtpState) {
            SmtpConnectionState.CONNECTED -> EmeraldOnline
            SmtpConnectionState.DISCONNECTED -> RoseError
            SmtpConnectionState.CHECKING -> Color(0xFFD97706)
            SmtpConnectionState.UNCONFIGURED -> Color(0xFF64748B)
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Rounded.Shield, contentDescription = null, tint = statusColor)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (uiState.smtpState) {
                            SmtpConnectionState.CONNECTED -> "SMTP Terhubung & Terverifikasi"
                            SmtpConnectionState.DISCONNECTED -> "Koneksi SMTP Terputus"
                            SmtpConnectionState.CHECKING -> "Sedang Memverifikasi..."
                            SmtpConnectionState.UNCONFIGURED -> "SMTP Belum Dikonfigurasi"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = statusColor
                    )
                    Text(
                        text = uiState.smtpStatusMessage,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Konfigurasi Akun Pengirim (SMTP)",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Kredensial disimpan terenkripsi lokal menggunakan Android EncryptedSharedPreferences (AES-256).",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = cfg.fromName,
                    onValueChange = { onUpdateConfig(cfg.copy(fromName = it)) },
                    label = { Text("Nama Pengirim (Display Name)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = cfg.username,
                    onValueChange = { onUpdateConfig(cfg.copy(username = it, senderEmail = it)) },
                    label = { Text("Email / Username SMTP") },
                    placeholder = { Text("akun@gmail.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedButton(
                    onClick = { onAutoDetectDomain(cfg.username) },
                    enabled = !uiState.isLookingUpDomain,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLookingUpDomain) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Rounded.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text("Deteksi Otomatis Host & Port (7-Layer)")
                }

                OutlinedTextField(
                    value = cfg.password,
                    onValueChange = { onUpdateConfig(cfg.copy(password = it)) },
                    label = { Text("App Password (16 Karakter untuk Gmail)") },
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
                    shape = RoundedCornerShape(12.dp)
                )

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

                Button(
                    onClick = { onSaveAndVerify(cfg) },
                    enabled = !uiState.isVerifyingSmtp,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    if (uiState.isVerifyingSmtp) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(if (uiState.isVerifyingSmtp) "Memverifikasi..." else "Simpan & Verifikasi SMTP")
                }
            }
        }
    }
}
