package com.gswift.relay.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gswift.relay.data.model.LogType
import com.gswift.relay.data.model.SmtpConnectionState
import com.gswift.relay.ui.viewmodel.MainUiState

@Composable
fun TerminalScreen(
    uiState: MainUiState,
    onScanSmtp: () -> Unit,
    onClearLogs: () -> Unit,
    onGoToAccounts: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var showDiagnostics by remember { mutableStateOf(false) }
    val diag = uiState.smtpDiagnostic

    val diagDotColor = when (diag.severity) {
        "healthy" -> Color(0xFF34D399)
        "warning" -> Color(0xFFFBBF24)
        "error" -> Color(0xFFFB7185)
        else -> Color(0xFF94A3B8)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Full Dark Mac-style Console Window matching Screenshot 4 (Terminal Tab)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .shadow(10.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF070B14))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(22.dp))
        ) {
            // Top Window Title Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C1220))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                        Box(Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                        Box(Modifier.size(9.dp).clip(CircleShape).background(Color(0xFF10B981)))
                    }

                    Text(
                        text = "relay.log",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(42.dp)
                    )

                    // ● LIVE Badge
                    Surface(
                        color = Color(0xFF064E3B).copy(alpha = 0.6f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Color(0xFF059669).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF34D399))
                            )
                            Text(
                                text = "LIVE",
                                color = Color(0xFF34D399),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Diagnostik Dropdown Toggle Pill
                    Surface(
                        color = if (showDiagnostics) Color(0xFF1E293B) else Color(0xFF141C2B),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (showDiagnostics) diagDotColor.copy(alpha = 0.6f) else Color(0xFF334155)),
                        modifier = Modifier.clickable { showDiagnostics = !showDiagnostics }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(diagDotColor)
                            )
                            Icon(
                                imageVector = if (showDiagnostics) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "Diagnostik SMTP",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Right Window Toolbar Actions: Pindai, Copy, Clear
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TerminalActionButton(
                        onClick = onScanSmtp,
                        icon = {
                            Icon(
                                Icons.Rounded.Refresh,
                                contentDescription = "Pindai Koneksi SMTP",
                                tint = Color(0xFFCBD5E1),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    )
                    TerminalActionButton(
                        onClick = {
                            val text = uiState.logs.joinToString("\n") { "[${it.timestamp}] ${it.message}" }
                            clipboard.setText(AnnotatedString(text))
                        },
                        icon = {
                            Icon(
                                Icons.Rounded.ContentCopy,
                                contentDescription = "Salin Log",
                                tint = Color(0xFFCBD5E1),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    )
                    TerminalActionButton(
                        onClick = onClearLogs,
                        icon = {
                            Icon(
                                Icons.Rounded.DeleteOutline,
                                contentDescription = "Bersihkan Log",
                                tint = Color(0xFFCBD5E1),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    )
                }
            }

            // Expandable SMTP Diagnostic Tray (1:1 App.tsx Diagnostik Console)
            AnimatedVisibility(visible = showDiagnostics) {
                Surface(
                    color = Color(0xFF0F172A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 310.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = diagDotColor.copy(alpha = 0.16f),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, diagDotColor.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = diag.badge,
                                    color = diagDotColor,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = diag.category,
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = diag.title,
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = diag.rootCause,
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )

                        diag.rawError?.takeIf { it.isNotBlank() }?.let { raw ->
                            Surface(
                                color = Color(0xFF070B14),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Text(
                                    text = raw,
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        diag.steps.forEach { step ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Surface(
                                    color = Color(0xFF1E293B),
                                    shape = CircleShape,
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${step.num}",
                                            color = Color(0xFF38BDF8),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = step.title,
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = step.desc,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.5.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onGoToAccounts,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF334155)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                            ) {
                                Icon(Icons.Rounded.Settings, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("Konfigurasi Akun", color = Color(0xFFE2E8F0), fontSize = 11.sp)
                            }
                            Button(
                                onClick = onScanSmtp,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                            ) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("Pindai Sekarang", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF1E293B))

            // Monospace Log Stream
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.logs) { log ->
                    val (glyph, msgColor) = when (log.type) {
                        LogType.INFO -> "•" to Color(0xFFE2E8F0)
                        LogType.SUCCESS -> "✓" to Color(0xFF34D399)
                        LogType.WARNING -> "⚠" to Color(0xFFFBBF24)
                        LogType.ERROR -> "✕" to Color(0xFFFB7185)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "[${log.timestamp}]",
                            color = Color(0xFF64748B),
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = glyph,
                            color = msgColor,
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = log.message,
                            color = msgColor,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF1E293B))

            // Bottom Footer Bar matching Screenshot 4
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C1220))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
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
                            .background(Color(0xFF38BDF8))
                    )
                    Text(
                        text = "Log: ${uiState.logs.size} entri",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                val smtpLabel = when {
                    uiState.smtpConfig.username.isBlank() -> "Belum terkonfigurasi"
                    uiState.smtpState == SmtpConnectionState.CONNECTED -> "${uiState.smtpConfig.host}:${uiState.smtpConfig.port}"
                    uiState.smtpState == SmtpConnectionState.CHECKING -> "Memeriksa..."
                    else -> "Terputus (${uiState.smtpConfig.host})"
                }
                val smtpLabelColor = when (uiState.smtpState) {
                    SmtpConnectionState.CONNECTED -> Color(0xFF34D399)
                    SmtpConnectionState.DISCONNECTED -> Color(0xFFFB7185)
                    else -> Color(0xFF94A3B8)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "SMTP:",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = smtpLabel,
                        color = smtpLabelColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun TerminalActionButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(9.dp),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier
            .size(30.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            icon()
        }
    }
}
