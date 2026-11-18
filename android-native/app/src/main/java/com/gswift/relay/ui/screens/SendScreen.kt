package com.gswift.relay.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import com.gswift.relay.data.model.SmtpConnectionState
import com.gswift.relay.ui.theme.ClaudeTerracotta
import com.gswift.relay.ui.theme.EmeraldOnline
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
    onDismissBanner: () -> Unit,
    onGoToAccounts: () -> Unit,
    onGoToAi: () -> Unit,
    extractEmails: (String) -> List<String>
) {
    var showPreviewDialog by remember { mutableStateOf(false) }
    val validEmails = remember(uiState.recipientInput) { extractEmails(uiState.recipientInput) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (uiState.smtpState == SmtpConnectionState.UNCONFIGURED) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Akun SMTP Belum Diatur",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = "Tambahkan email dan App Password 16 digit untuk mengirim.",
                            fontSize = 12.sp,
                            color = Color(0xFFB45309)
                        )
                    }
                    TextButton(onClick = onGoToAccounts) {
                        Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Atur", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        uiState.deliveryBanner?.let { banner ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (banner.isSuccess) Color(0xFFECFDF5) else Color(0xFFFFF1F2)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (banner.isSuccess) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = if (banner.isSuccess) EmeraldOnline else RoseError
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = banner.title,
                            fontWeight = FontWeight.Bold,
                            color = if (banner.isSuccess) Color(0xFF065F46) else Color(0xFF9F1239),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onDismissBanner) {
                            Text("Tutup")
                        }
                    }
                    Text(
                        text = banner.detail,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    banner.tip?.let { tipText ->
                        Surface(
                            color = Color.White.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Solusi: $tipText",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF881337),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kirim Email Cepat",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row {
                        IconButton(onClick = { showPreviewDialog = true }) {
                            Icon(Icons.Rounded.Visibility, contentDescription = "Pratinjau", tint = NavyPrimary)
                        }
                        IconButton(onClick = onClearClick) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Bersihkan", tint = Color(0xFF64748B))
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.recipientInput,
                    onValueChange = { onUpdateFields(it, null, null) },
                    label = { Text("Kepada (Email Penerima)") },
                    placeholder = { Text("contoh@gmail.com, lain@domain.com") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = false,
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
                                label = { Text(email, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.subjectInput,
                    onValueChange = { onUpdateFields(null, it, null) },
                    label = { Text("Subjek Email") },
                    placeholder = { Text("[Resmi] Konfirmasi Transaksi...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.messageInput,
                    onValueChange = { onUpdateFields(null, null, it) },
                    label = { Text("Konten Pesan / HTML Draf") },
                    placeholder = { Text("Tulis isi email atau buat lewat tab AI Chat...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    shape = RoundedCornerShape(14.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = if (uiState.messageInput.contains("<")) FontFamily.Monospace else FontFamily.Default,
                        fontSize = 13.sp
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onGoToAi,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = ClaudeTerracotta,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Buat dengan AI", color = ClaudeTerracotta, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onSendClick,
                        enabled = !uiState.isSendingEmail,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        if (uiState.isSendingEmail) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Rounded.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(if (uiState.isSendingEmail) "Mengirim..." else "Kirim")
                    }
                }
            }
        }
    }

    if (showPreviewDialog) {
        val plainPreview = remember(uiState.messageInput) {
            HtmlCompat.fromHtml(uiState.messageInput, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
        }
        AlertDialog(
            onDismissRequest = { showPreviewDialog = false },
            title = { Text("Pratinjau Native Konten") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Subjek: ${uiState.subjectInput.ifBlank { "(Belum diisi)" }}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = plainPreview.ifBlank { "(Konten pesan masih kosong)" },
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPreviewDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}
