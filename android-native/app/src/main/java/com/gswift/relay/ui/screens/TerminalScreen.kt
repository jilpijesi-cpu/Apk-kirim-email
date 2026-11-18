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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.gswift.relay.ui.theme.EmeraldOnline
import com.gswift.relay.ui.theme.GoldAccent
import com.gswift.relay.ui.viewmodel.MainUiState

@Composable
fun TerminalScreen(
    uiState: MainUiState,
    onRefreshKeys: () -> Unit,
    onResetKeys: () -> Unit,
    onClearLogs: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var showKeysMenu by remember { mutableStateOf(false) }

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
                .background(Color(0xFF0B1120))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(22.dp))
        ) {
            // Top Window Title Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111827))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // macOS Window Dots
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                        Box(Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                        Box(Modifier.size(9.dp).clip(CircleShape).background(Color(0xFF10B981)))
                    }

                    Text(
                        text = "relay",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(34.dp)
                    )

                    // ● LIVE Badge
                    Surface(
                        color = Color(0xFF064E3B).copy(alpha = 0.6f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Color(0xFF059669).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
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
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Key Rotation Dropdown Pill
                    Box {
                        val rot = uiState.keyRotationStatus
                        val dotColor = if ((rot?.activeKeys ?: 0) > 0) Color(0xFF34D399) else Color(0xFF94A3B8)
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.clickable { showKeysMenu = true }
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
                                        .background(dotColor)
                                )
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = "Status API Key",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showKeysMenu,
                            onDismissRequest = { showKeysMenu = false }
                        ) {
                            if (rot != null) {
                                val cooldownCount = rot.keys.count { it.status != "active" }
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Aktif: ${rot.activeKeys}/${rot.totalKeys} • Cooldown: $cooldownCount",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    },
                                    onClick = {}
                                )
                                rot.keys.take(6).forEach { key ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${key.provider.uppercase()} (${key.maskedKey}) - ${key.status.uppercase()}",
                                                fontSize = 11.5.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = if (key.status == "active") EmeraldOnline else GoldAccent
                                            )
                                        },
                                        onClick = {}
                                    )
                                }
                                HorizontalDivider()
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Rounded.RestartAlt, contentDescription = null) },
                                    text = { Text("Reset Cooldown Semua Key", fontSize = 12.sp) },
                                    onClick = {
                                        onResetKeys()
                                        showKeysMenu = false
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Memuat status key dari server...", fontSize = 12.sp) },
                                    onClick = {
                                        onRefreshKeys()
                                        showKeysMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Right Window Toolbar Actions (Refresh, Copy, Clear)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TerminalActionButton(
                        onClick = onRefreshKeys,
                        icon = {
                            Icon(
                                Icons.Rounded.Refresh,
                                contentDescription = "Refresh",
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
                    val msgColor = when (log.type) {
                        LogType.INFO -> Color(0xFFE2E8F0)
                        LogType.SUCCESS -> Color(0xFF34D399)
                        LogType.WARNING -> Color(0xFFFBBF24)
                        LogType.ERROR -> Color(0xFFFB7185)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "[${log.timestamp}]",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp,
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
                    .background(Color(0xFF0F172A))
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

                val smtpLabel = when (uiState.smtpState) {
                    SmtpConnectionState.CONNECTED -> "Terhubung"
                    SmtpConnectionState.DISCONNECTED -> "Terputus"
                    SmtpConnectionState.CHECKING -> "Memeriksa..."
                    SmtpConnectionState.UNCONFIGURED -> "Belum terkonfigurasi"
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
                        fontFamily = FontFamily.Monospace
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
