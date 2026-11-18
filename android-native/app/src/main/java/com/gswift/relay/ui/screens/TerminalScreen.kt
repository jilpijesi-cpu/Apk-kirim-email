package com.gswift.relay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gswift.relay.data.model.LogType
import com.gswift.relay.ui.theme.EmeraldOnline
import com.gswift.relay.ui.theme.GoldAccent
import com.gswift.relay.ui.theme.NavyPrimary
import com.gswift.relay.ui.theme.RoseError
import com.gswift.relay.ui.viewmodel.MainUiState

@Composable
fun TerminalScreen(
    uiState: MainUiState,
    onRefreshKeys: () -> Unit,
    onResetKeys: () -> Unit,
    onClearLogs: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // API Key Rotation Status Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rotasi API Key AI Server",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row {
                        IconButton(onClick = onRefreshKeys) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Refresh", tint = NavyPrimary)
                        }
                        IconButton(onClick = onResetKeys) {
                            Icon(Icons.Rounded.RestartAlt, contentDescription = "Reset Cooldown", tint = GoldAccent)
                        }
                    }
                }

                val rot = uiState.keyRotationStatus
                if (rot != null) {
                    val cooldownCount = rot.keys.count { it.status != "active" }
                    Text(
                        text = "Aktif: ${rot.activeKeys} / ${rot.totalKeys} Key • Cooldown: $cooldownCount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    rot.keys.take(4).forEach { key ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${key.provider.uppercase()} (${key.maskedKey})",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = key.status.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (key.status == "active") EmeraldOnline else GoldAccent
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Memuat status rotasi API Key dari server...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Console Log Card
        Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "KONSOL LOG REAL-TIME",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    OutlinedButton(onClick = onClearLogs) {
                        Icon(
                            Icons.Rounded.DeleteSweep,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Clear", color = Color.White, fontSize = 11.sp)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF020617), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(uiState.logs) { log ->
                        val color = when (log.type) {
                            LogType.INFO -> Color(0xFF38BDF8)
                            LogType.SUCCESS -> Color(0xFF34D399)
                            LogType.WARNING -> Color(0xFFFBBF24)
                            LogType.ERROR -> Color(0xFFFB7185)
                        }
                        Text(
                            text = "[${log.timestamp}] ${log.message}",
                            color = color,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
