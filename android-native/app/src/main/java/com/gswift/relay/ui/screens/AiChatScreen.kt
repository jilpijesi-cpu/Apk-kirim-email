package com.gswift.relay.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.MicNone
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gswift.relay.data.model.AppPresets
import com.gswift.relay.data.model.ChatMessage
import com.gswift.relay.ui.theme.AppSkyGradient
import com.gswift.relay.ui.theme.ClaudeStarburstLogo
import com.gswift.relay.ui.theme.ClaudeTerracotta
import com.gswift.relay.ui.theme.EmeraldOnline
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
    var showPresetPicker by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var showQuickPlusMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val allModels = remember(uiState.aiProviders) {
        uiState.aiProviders.flatMap { it.models }
    }
    val hasUserConversation = remember(uiState.aiMessages) {
        uiState.aiMessages.any { it.role == "user" }
    }

    LaunchedEffect(uiState.aiMessages.size) {
        if (hasUserConversation && uiState.aiMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.aiMessages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppSkyGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Minimal Full-Screen Top Bar matching Screenshot 3 (AI Tab)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackToSend) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Kembali ke Kirim",
                    tint = Color(0xFF1E293B)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // 1. Key rotation / AI Model switcher with green active indicator
                Box {
                    IconButton(onClick = { showModelMenu = true }) {
                        Box {
                            Icon(
                                imageVector = Icons.Rounded.Sync,
                                contentDescription = "Model & Key AI",
                                tint = EmeraldOnline,
                                modifier = Modifier.size(22.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 3.dp, y = (-2).dp)
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                                    .border(1.dp, Color.White, CircleShape)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = showModelMenu,
                        onDismissRequest = { showModelMenu = false }
                    ) {
                        if (allModels.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Auto-Rotate Multi-Provider", fontSize = 13.sp) },
                                onClick = { showModelMenu = false }
                            )
                        } else {
                            allModels.forEach { model ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (model == uiState.selectedAiModel) "✓ $model" else model,
                                            fontSize = 12.5.sp,
                                            fontWeight = if (model == uiState.selectedAiModel) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onSelectModel(model)
                                        showModelMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 2. Compose / New Chat
                IconButton(onClick = onClearChat) {
                    Icon(
                        imageVector = Icons.Rounded.EditNote,
                        contentDescription = "Chat Baru",
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(23.dp)
                    )
                }

                // 3. Link / Custom CTA URL & Rules
                IconButton(onClick = { showRulesDialog = true }) {
                    Icon(
                        imageVector = Icons.Rounded.Link,
                        contentDescription = "Tautan CTA & Aturan",
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(21.dp)
                    )
                }

                // 4. Brain / Bank & Scenario Picker
                IconButton(onClick = { showPresetPicker = true }) {
                    Icon(
                        imageVector = Icons.Rounded.Psychology,
                        contentDescription = "Bank & Skenario",
                        tint = ClaudeTerracotta,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }
        }

        // Center Area: Hero Welcome (Screenshot 3) OR Active Conversation
        if (!hasUserConversation && !uiState.isAiThinking) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    ClaudeStarburstLogo(
                        modifier = Modifier.size(72.dp),
                        animated = true
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Claude Mythos",
                        fontFamily = FontFamily.Serif,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Ada yang bisa saya bantu buatkan hari ini?",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.aiMessages.filterNot { it.id == "welcome" }, key = { it.id }) { msg ->
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
        }

        // Bottom Floating White Pill Composer matching Screenshot 3
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(30.dp),
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box {
                        IconButton(
                            onClick = { showQuickPlusMenu = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Sisipkan Skenario",
                                tint = Color(0xFF1E293B),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showQuickPlusMenu,
                            onDismissRequest = { showQuickPlusMenu = false }
                        ) {
                            val currentBank = AppPresets.banks.find { it.key == uiState.selectedBankKey }?.label ?: "BCA"
                            AppPresets.scenarios.forEach { sc ->
                                DropdownMenuItem(
                                    text = { Text("${sc.label} ($currentBank)", fontSize = 13.sp) },
                                    onClick = {
                                        onSelectScenario(sc.id)
                                        inputPrompt = sc.promptTemplate(currentBank)
                                        showQuickPlusMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (inputPrompt.isEmpty()) {
                            Text(
                                text = "Minta Claude...",
                                fontSize = 15.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        BasicTextField(
                            value = inputPrompt,
                            onValueChange = { inputPrompt = it },
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            ),
                            cursorBrush = SolidColor(ClaudeTerracotta),
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (inputPrompt.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(ClaudeTerracotta)
                                .clickable(enabled = !uiState.isAiThinking) {
                                    val toSend = inputPrompt
                                    inputPrompt = ""
                                    onSendPrompt(toSend)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Send,
                                contentDescription = "Kirim Prompt",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { showPresetPicker = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MicNone,
                                contentDescription = "Pilih Skenario Cepat",
                                tint = Color(0xFF334155),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Bank & Scenario Configurator Dialog (triggered from Brain icon)
    if (showPresetPicker) {
        AlertDialog(
            onDismissRequest = { showPresetPicker = false },
            title = { Text("Pilih Bank & Skenario Draf") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Bank Sasaran:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                    Text("Skenario Transaksi:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
            },
            confirmButton = {
                Button(
                    onClick = { showPresetPicker = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Selesai")
                }
            }
        )
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
                Button(
                    onClick = {
                        onSaveCustomSettings(linkText.trim(), rulesText.trim())
                        showRulesDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
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
            shadowElevation = 2.dp,
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
