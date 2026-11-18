package com.gswift.relay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.gswift.relay.data.model.AppPresets
import com.gswift.relay.data.model.ChatMessage
import com.gswift.relay.ui.theme.ClaudeStarburstLogo
import com.gswift.relay.ui.theme.ClaudeTerracotta
import com.gswift.relay.ui.theme.ClaudeWarmBg
import com.gswift.relay.ui.theme.NavyPrimary
import com.gswift.relay.ui.viewmodel.MainUiState

@Composable
fun AiChatScreen(
    uiState: MainUiState,
    onBackToSend: () -> Unit,
    onSendPrompt: (String) -> Unit,
    onSelectBank: (String) -> Unit,
    onSelectScenario: (String) -> Unit,
    onSelectModel: (String) -> Unit,
    onSaveCustomSettings: (cancelLink: String, dataRules: String) -> Unit,
    onClearChat: () -> Unit,
    onApplyDraft: (subject: String?, htmlOrBody: String) -> Unit
) {
    var inputPrompt by remember { mutableStateOf("") }
    var showModelMenu by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val allModels = remember(uiState.aiProviders) {
        uiState.aiProviders.flatMap { it.models }
    }

    LaunchedEffect(uiState.aiMessages.size) {
        if (uiState.aiMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.aiMessages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClaudeWarmBg)
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Full-Screen Claude Mythos Header matching App.tsx AI tab
        Surface(color = Color.White, shadowElevation = 1.dp) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackToSend) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Kembali ke Kirim",
                                tint = Color(0xFF451A03)
                            )
                        }
                        ClaudeStarburstLogo(
                            modifier = Modifier.size(22.dp),
                            animated = uiState.isAiThinking
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Claude Mythos",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF451A03)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box {
                            OutlinedButton(
                                onClick = { showModelMenu = true },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = (uiState.selectedAiModel ?: "Auto").substringAfterLast("/").take(14),
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                            DropdownMenu(
                                expanded = showModelMenu,
                                onDismissRequest = { showModelMenu = false }
                            ) {
                                allModels.forEach { model ->
                                    DropdownMenuItem(
                                        text = { Text(model, fontSize = 13.sp) },
                                        onClick = {
                                            onSelectModel(model)
                                            showModelMenu = false
                                        }
                                    )
                                }
                            }
                        }
                        IconButton(onClick = { showRulesDialog = true }) {
                            Icon(Icons.Rounded.Tune, contentDescription = "Aturan AI")
                        }
                        IconButton(onClick = onClearChat) {
                            Icon(Icons.Rounded.DeleteSweep, contentDescription = "Reset Chat")
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AppPresets.banks.forEach { bank ->
                        FilterChip(
                            selected = uiState.selectedBankKey == bank.key,
                            onClick = { onSelectBank(bank.key) },
                            label = { Text(bank.label, fontSize = 11.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AppPresets.scenarios.forEach { sc ->
                        FilterChip(
                            selected = uiState.selectedScenario == sc.id,
                            onClick = {
                                onSelectScenario(sc.id)
                                val bankName = AppPresets.banks
                                    .find { it.key == uiState.selectedBankKey }?.label ?: "BCA"
                                inputPrompt = sc.promptTemplate(bankName)
                            },
                            label = { Text(sc.label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = Color(0xFFE7E5E4))

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.aiMessages, key = { it.id }) { msg ->
                ChatBubble(
                    message = msg,
                    onApplyDraft = onApplyDraft
                )
            }
            if (uiState.isAiThinking) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = ClaudeTerracotta,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Claude Mythos sedang merancang draf...",
                            fontSize = 12.sp,
                            color = ClaudeTerracotta,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Composer Row
        Surface(color = Color.White, shadowElevation = 4.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputPrompt,
                    onValueChange = { inputPrompt = it },
                    placeholder = { Text("Instruksi draf email...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    maxLines = 4
                )
                Button(
                    onClick = {
                        val toSend = inputPrompt
                        inputPrompt = ""
                        onSendPrompt(toSend)
                    },
                    enabled = inputPrompt.isNotBlank() && !uiState.isAiThinking,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ClaudeTerracotta)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Kirim Prompt")
                }
            }
        }
    }

    if (showRulesDialog) {
        var linkText by remember { mutableStateOf(uiState.customCancelLink) }
        var rulesText by remember { mutableStateOf(uiState.customDataRules) }
        AlertDialog(
            onDismissRequest = { showRulesDialog = false },
            title = { Text("Aturan Draf AI Khusus") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = linkText,
                        onValueChange = { linkText = it },
                        label = { Text("Tautan Tombol CTA (Opsional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = rulesText,
                        onValueChange = { rulesText = it },
                        label = { Text("Aturan Data Tambahan") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onSaveCustomSettings(linkText.trim(), rulesText.trim())
                    showRulesDialog = false
                }) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRulesDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    onApplyDraft: (subject: String?, htmlOrBody: String) -> Unit
) {
    val isUser = message.role == "user"
    val htmlDraft = message.extractedHtml
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isUser) NavyPrimary else Color.White,
            shape = RoundedCornerShape(18.dp),
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 315.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (!isUser && !message.usedModel.isNullOrBlank()) {
                    Text(
                        text = message.usedModel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClaudeTerracotta
                    )
                }
                Text(
                    text = message.text.take(1500),
                    fontSize = 13.sp,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                )
                if (!isUser && !htmlDraft.isNullOrBlank()) {
                    Button(
                        onClick = { onApplyDraft(message.extractedSubject, htmlDraft) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Terapkan Draf ke Kirim", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
