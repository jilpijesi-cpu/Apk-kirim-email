package com.gswift.relay.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.MicNone
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Visibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gswift.relay.data.model.AppPresets
import com.gswift.relay.data.model.ChatMessage
import com.gswift.relay.ui.components.NativeReceiptPreview
import com.gswift.relay.ui.theme.AiMythosGradient
import com.gswift.relay.ui.theme.ClaudeStarburstLogo
import com.gswift.relay.ui.theme.ClaudeTerracotta
import com.gswift.relay.ui.theme.EmeraldOnline
import com.gswift.relay.ui.theme.GoldAccent
import com.gswift.relay.ui.theme.NavyPrimary
import com.gswift.relay.ui.theme.RoseError
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
    onApplyDraft: (subject: String?, htmlOrBody: String) -> Unit,
    onRandomizeRef: (messageId: String) -> Unit,
    onSaveAsTemplate: (subject: String?, htmlContent: String) -> Unit,
    onRefreshKeys: () -> Unit,
    onResetKeys: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var inputPrompt by remember { mutableStateOf("") }
    var showKeyRotationModal by remember { mutableStateOf(false) }
    var showCancelLinkDialog by remember { mutableStateOf(false) }
    var showRulesAndPresetDialog by remember { mutableStateOf(false) }
    var showBankQuickGrid by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                inputPrompt = if (inputPrompt.isBlank()) spoken else "$inputPrompt $spoken"
            }
        }
    }

    val allModels = remember(uiState.aiProviders) {
        uiState.aiProviders.flatMap { it.models }.distinct()
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
            .background(AiMythosGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Top Minimal Toolbar matching Screenshot 3 (AI Tab)
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
                // 1. Key Rotation Status & AI Model Modal
                IconButton(onClick = {
                    onRefreshKeys()
                    showKeyRotationModal = true
                }) {
                    Box {
                        Icon(
                            imageVector = Icons.Rounded.Sync,
                            contentDescription = "Status Rotasi API Key",
                            tint = EmeraldOnline,
                            modifier = Modifier.size(22.dp)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 3.dp, y = (-2).dp)
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    if ((uiState.keyRotationStatus?.activeKeys ?: 1) > 0) Color(0xFF10B981) else GoldAccent
                                )
                                .border(1.dp, Color.White, CircleShape)
                        )
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

                // 3. Link Tombol Batal Modal
                IconButton(onClick = { showCancelLinkDialog = true }) {
                    Box {
                        Icon(
                            imageVector = Icons.Rounded.Link,
                            contentDescription = "Link Tombol Batal",
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(21.dp)
                        )
                        if (uiState.customCancelLink.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 2.dp, y = (-2).dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(NavyPrimary)
                            )
                        }
                    }
                }

                // 4. Brain / Aturan Data Utama & Bank Picker Modal
                IconButton(onClick = { showRulesAndPresetDialog = true }) {
                    Box {
                        Icon(
                            imageVector = Icons.Rounded.Psychology,
                            contentDescription = "Aturan Data & Bank",
                            tint = ClaudeTerracotta,
                            modifier = Modifier.size(23.dp)
                        )
                        if (uiState.customDataRules.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 2.dp, y = (-2).dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(ClaudeTerracotta)
                            )
                        }
                    }
                }
            }
        }

        // Hero Greeting (Screenshot 3) OR Active Conversation
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.aiMessages.filterNot { it.id == "welcome" }, key = { it.id }) { msg ->
                    ChatBubble(
                        message = msg,
                        onCopyText = { text -> clipboard.setText(AnnotatedString(text)) },
                        onApplyDraft = onApplyDraft,
                        onRandomizeRef = { onRandomizeRef(msg.id) },
                        onSaveAsTemplate = { onSaveAsTemplate(msg.extractedSubject, it) }
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

        // Bottom Bank Quick Grid (6 Official Logos) + Floating Pill Composer (Screenshot 3)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedVisibility(visible = showBankQuickGrid) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(22.dp),
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Template Cepat Bank (Klik untuk Buat)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF334155)
                            )
                            IconButton(
                                onClick = { showBankQuickGrid = false },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = "Tutup", modifier = Modifier.size(15.dp))
                            }
                        }

                        // 2 rows of 3 banks with real PNG logos
                        AppPresets.banks.chunked(3).forEach { rowBanks ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowBanks.forEach { bank ->
                                    Surface(
                                        color = Color(0xFFF8FAFC),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(
                                            1.dp,
                                            if (uiState.selectedBankKey == bank.key) Color(bank.colorHex) else Color(0xFFE2E8F0)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                onSelectBank(bank.key)
                                                showBankQuickGrid = false
                                                val prompt = AppPresets.quickBankPrompts[bank.key]
                                                    ?: "Buatkan draf email transaksi Bank ${bank.label} Rp 5.000.000 di Shopee"
                                                onSendPrompt(prompt)
                                            }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(id = bank.logoRes),
                                                contentDescription = bank.label,
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier
                                                    .height(20.dp)
                                                    .fillMaxWidth(0.8f)
                                            )
                                            Text(
                                                text = bank.label,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E293B),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

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
                    IconButton(
                        onClick = { showBankQuickGrid = !showBankQuickGrid },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (showBankQuickGrid) Icons.Rounded.Close else Icons.Rounded.Add,
                            contentDescription = "Template Cepat Bank",
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(22.dp)
                        )
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
                                .background(Color(0xFF0F172A))
                                .clickable(enabled = !uiState.isAiThinking) {
                                    val toSend = inputPrompt
                                    inputPrompt = ""
                                    showBankQuickGrid = false
                                    onSendPrompt(toSend)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowUpward,
                                contentDescription = "Kirim Prompt",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Bicara untuk meminta draf email...")
                                }
                                val launched = runCatching { speechLauncher.launch(intent) }.isSuccess
                                if (!launched) {
                                    showRulesAndPresetDialog = true
                                }
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MicNone,
                                contentDescription = "Input Suara",
                                tint = Color(0xFF334155),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // 1. Key Rotation & Model Switcher Modal
    if (showKeyRotationModal) {
        Dialog(onDismissRequest = { showKeyRotationModal = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sistem Rotasi API Key",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Multi-provider fallback & pilih model AI",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        IconButton(onClick = { showKeyRotationModal = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.Close, contentDescription = "Tutup", modifier = Modifier.size(18.dp))
                        }
                    }

                    // Model Selection Chips
                    Text("Model AI Aktif", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val modelList = (listOf("Claude Mythos") + allModels).distinct()
                        modelList.forEach { m ->
                            FilterChip(
                                selected = uiState.selectedAiModel == m,
                                onClick = { onSelectModel(m) },
                                label = { Text(m, fontSize = 11.sp) }
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    val rot = uiState.keyRotationStatus
                    if (rot != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Key Aktif: ${rot.activeKeys} / ${rot.totalKeys}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldOnline
                            )
                            TextButton(onClick = onRefreshKeys, enabled = !uiState.isRefreshingKeys) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Refresh", fontSize = 11.sp)
                            }
                        }

                        rot.keys.take(8).forEach { key ->
                            val isActive = key.status == "active"
                            Surface(
                                color = if (isActive) Color(0xFFF0FDF4) else Color(0xFFFFFBEB),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isActive) Color(0xFFBBF7D0) else Color(0xFFFDE68A)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${key.provider.uppercase()} • ${key.maskedKey}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "Sukses: ${key.totalSuccess} • Error: ${key.totalErrors}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    Surface(
                                        color = if (isActive) EmeraldOnline else GoldAccent,
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = if (isActive) "AKTIF" else "${key.cooldownRemainingSec}s",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = onResetKeys,
                            enabled = !uiState.isRefreshingKeys,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Reset Semua Cooldown API Key", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "Status rotasi API key belum termuat atau server offline.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }

    // 2. Link Tombol Batal Modal (Link icon)
    if (showCancelLinkDialog) {
        var linkText by remember(uiState.customCancelLink) { mutableStateOf(uiState.customCancelLink) }
        Dialog(onDismissRequest = { showCancelLinkDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Link Tombol Batalkan Transaksi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Tautan tujuan tombol CTA pada draf email HTML yang dibuat AI maupun Smart Bank Switcher.",
                        fontSize = 11.5.sp,
                        color = Color(0xFF64748B)
                    )
                    OutlinedTextField(
                        value = linkText,
                        onValueChange = { linkText = it },
                        label = { Text("URL Tujuan Tombol Batal (HTTPS)") },
                        placeholder = { Text("https://...") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showCancelLinkDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Batal")
                        }
                        Button(
                            onClick = {
                                onSaveCustomSettings(linkText.trim(), uiState.customDataRules)
                                showCancelLinkDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Simpan Link")
                        }
                    }
                }
            }
        }
    }

    // 3. Aturan Data Utama & Bank/Skenario Picker Modal (Brain icon)
    if (showRulesAndPresetDialog) {
        var rulesText by remember(uiState.customDataRules) { mutableStateOf(uiState.customDataRules) }
        Dialog(onDismissRequest = { showRulesAndPresetDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Aturan Data & Skenario Bank",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )

                    Text("Bank Sasaran:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
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

                    Text("Skenario Transaksi:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
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
                                    val bankName = AppPresets.banks.find { it.key == uiState.selectedBankKey }?.label ?: "BCA"
                                    inputPrompt = sc.promptTemplate(bankName)
                                },
                                label = { Text(sc.label, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = rulesText,
                        onValueChange = { rulesText = it },
                        label = { Text("Aturan Data Tambahan untuk AI") },
                        placeholder = { Text("Misal: Nama Nasabah: Budi, Nominal Rp 2.500.000...") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showRulesAndPresetDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Tutup")
                        }
                        Button(
                            onClick = {
                                onSaveCustomSettings(uiState.customCancelLink, rulesText.trim())
                                showRulesAndPresetDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Simpan Aturan")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    onCopyText: (String) -> Unit,
    onApplyDraft: (subject: String?, htmlOrBody: String) -> Unit,
    onRandomizeRef: () -> Unit,
    onSaveAsTemplate: (String) -> Unit
) {
    val isUser = message.role == "user"
    val htmlDraft = message.extractedHtml
    var showCodeTab by remember(message.id) { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (isUser) {
            Surface(
                color = Color(0xFF18181B),
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 2.dp,
                modifier = Modifier.widthIn(max = 290.dp)
            ) {
                Text(
                    text = message.text,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        } else {
            Surface(
                color = if (message.isError) Color(0xFFFFF1F2) else Color.White,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    1.dp,
                    if (message.isError) Color(0xFFFDA4AF) else Color(0xFFE2E8F0)
                ),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth(0.95f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Model & Auto-Rotation Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ClaudeStarburstLogo(modifier = Modifier.size(14.dp), animated = false)
                        Text(
                            text = "Dikerjakan oleh ${message.usedModel ?: "Claude Mythos"}",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (message.isError) RoseError else ClaudeTerracotta
                        )
                        if (!message.rotationNote.isNullOrBlank()) {
                            Surface(
                                color = Color(0xFFFEF3C7),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "Rotasi Otomatis",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (message.text.isNotBlank()) {
                        Text(
                            text = message.text,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = if (message.isError) Color(0xFF9F1239) else Color(0xFF1E293B)
                        )
                    }

                    // Rich HTML Email Draft Preview & Toolbar (1:1 App.tsx)
                    if (!htmlDraft.isNullOrBlank()) {
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF1F5F9))
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
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
                                                .background(if (!showCodeTab) Color.White else Color.Transparent)
                                                .clickable { showCodeTab = false }
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Rounded.Visibility, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Text("Pratinjau", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (showCodeTab) Color.White else Color.Transparent)
                                                .clickable { showCodeTab = true }
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Rounded.Code, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Text("Kode HTML", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    IconButton(
                                        onClick = { onCopyText(htmlDraft) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.ContentCopy,
                                            contentDescription = "Salin Kode HTML",
                                            tint = Color(0xFF475569),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                if (!message.extractedSubject.isNullOrBlank()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White)
                                            .padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Subjek: ${message.extractedSubject}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NavyPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            Icons.Rounded.ContentCopy,
                                            contentDescription = "Salin Subjek",
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { onCopyText(message.extractedSubject) }
                                        )
                                    }
                                    HorizontalDivider(color = Color(0xFFE2E8F0))
                                }

                                if (!showCodeTab) {
                                    NativeReceiptPreview(
                                        html = htmlDraft,
                                        subject = message.extractedSubject.orEmpty(),
                                        compactMobileMode = false
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 220.dp)
                                            .background(Color(0xFF0F172A))
                                            .verticalScroll(rememberScrollState())
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = htmlDraft,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            lineHeight = 14.sp,
                                            color = Color(0xFF93C5FD)
                                        )
                                    }
                                }
                            }
                        }

                        // Primary CTA + Secondary Toolbar ("Acak Ref", "Salin HTML", "Simpan")
                        Button(
                            onClick = { onApplyDraft(message.extractedSubject, htmlDraft) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Gunakan Draf di Pengirim Email", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = onRandomizeRef,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                            ) {
                                Icon(Icons.Rounded.Shuffle, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Acak Ref", fontSize = 10.sp, maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = { onCopyText(htmlDraft) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                            ) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Salin", fontSize = 10.sp, maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = { onSaveAsTemplate(htmlDraft) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                            ) {
                                Icon(Icons.Rounded.BookmarkAdd, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Simpan", fontSize = 10.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}
