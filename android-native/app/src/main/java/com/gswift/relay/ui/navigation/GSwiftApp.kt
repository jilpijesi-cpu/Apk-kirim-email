package com.gswift.relay.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gswift.relay.data.model.SmtpConnectionState
import com.gswift.relay.ui.screens.AccountsScreen
import com.gswift.relay.ui.screens.AiChatScreen
import com.gswift.relay.ui.screens.SendScreen
import com.gswift.relay.ui.screens.TemplatesScreen
import com.gswift.relay.ui.screens.TerminalScreen
import com.gswift.relay.ui.theme.ClaudeStarburstLogo
import com.gswift.relay.ui.theme.ClaudeTerracotta
import com.gswift.relay.ui.theme.EmeraldOnline
import com.gswift.relay.ui.theme.NavyPrimary
import com.gswift.relay.ui.theme.RoseError
import com.gswift.relay.ui.viewmodel.GSwiftViewModel

sealed class AppDestination(val route: String, val label: String, val icon: ImageVector) {
    data object Ai : AppDestination("ai", "AI", Icons.Rounded.AutoAwesome)
    data object Templates : AppDestination("templates", "Templates", Icons.Rounded.Description)
    data object Send : AppDestination("send", "Kirim", Icons.Rounded.Send)
    data object Terminal : AppDestination("terminal", "Terminal", Icons.Rounded.Terminal)
    data object Accounts : AppDestination("accounts", "Akun", Icons.Rounded.Settings)
}

private val bottomTabs = listOf(
    AppDestination.Ai,
    AppDestination.Templates,
    AppDestination.Send,
    AppDestination.Terminal,
    AppDestination.Accounts
)

@Composable
fun GSwiftApp(viewModel: GSwiftViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppDestination.Send.route
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val isAiTab = currentRoute == AppDestination.Ai.route

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Column {
                if (!isAiTab) {
                    Surface(
                        color = Color.White.copy(alpha = 0.97f),
                        shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.statusBarsPadding()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Left: Authentic Claude Mythos Header matching App.tsx
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    ClaudeStarburstLogo(
                                        modifier = Modifier.size(26.dp),
                                        animated = true
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(16.dp)
                                            .background(Color(0xFFE2E8F0))
                                    )
                                    Text(
                                        text = "Claude Mythos",
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF451A03)
                                    )
                                }

                                // Right: SMTP Status Pill matching App.tsx
                                val (bgCol, borderCol, textCol, dotCol, statusText) = when (uiState.smtpState) {
                                    SmtpConnectionState.CONNECTED -> Tuple5(
                                        Color(0xFFECFDF5),
                                        Color(0xFF6EE7B7),
                                        Color(0xFF065F46),
                                        EmeraldOnline,
                                        "Online"
                                    )
                                    SmtpConnectionState.DISCONNECTED -> Tuple5(
                                        Color(0xFFFFF1F2),
                                        Color(0xFFFDA4AF),
                                        Color(0xFF9F1239),
                                        RoseError,
                                        "Offline"
                                    )
                                    SmtpConnectionState.CHECKING -> Tuple5(
                                        Color(0xFFFFFBEB),
                                        Color(0xFFFCD34D),
                                        Color(0xFF92400E),
                                        Color(0xFFD97706),
                                        "Cek..."
                                    )
                                    SmtpConnectionState.UNCONFIGURED -> Tuple5(
                                        Color(0xFFF1F5F9),
                                        Color(0xFFCBD5E1),
                                        Color(0xFF334155),
                                        Color(0xFF94A3B8),
                                        "Atur SMTP"
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(bgCol)
                                        .border(1.dp, borderCol, CircleShape)
                                        .clickable {
                                            if (uiState.smtpState == SmtpConnectionState.UNCONFIGURED) {
                                                navigateTo(AppDestination.Accounts.route)
                                            } else {
                                                viewModel.verifySmtpConnection()
                                            }
                                        }
                                        .padding(horizontal = 11.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(dotCol)
                                    )
                                    Text(
                                        text = statusText,
                                        color = textCol,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                        }
                    }
                }

                AnimatedVisibility(visible = !uiState.isOnline) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFD97706))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WifiOff,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Mode Offline — Data lokal tetap tersedia",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (!isAiTab) {
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.navigationBarsPadding()) {
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(62.dp)
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            bottomTabs.forEach { dest ->
                                val selected = currentRoute == dest.route
                                val pillColor = when {
                                    selected && dest == AppDestination.Ai -> Color(0xFFFEF3C7)
                                    selected -> Color(0xFFDBEAFE)
                                    else -> Color.Transparent
                                }
                                val contentColor = when {
                                    selected && dest == AppDestination.Ai -> Color(0xFF92400E)
                                    selected -> NavyPrimary
                                    else -> Color(0xFF64748B)
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { navigateTo(dest.route) }
                                        .padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(44.dp)
                                            .height(28.dp)
                                            .clip(CircleShape)
                                            .background(pillColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (dest == AppDestination.Ai) {
                                            ClaudeStarburstLogo(
                                                modifier = Modifier.size(18.dp),
                                                color = if (selected) ClaudeTerracotta else Color(0xFF94A3B8),
                                                animated = selected
                                            )
                                        } else {
                                            Icon(
                                                imageVector = dest.icon,
                                                contentDescription = dest.label,
                                                tint = contentColor,
                                                modifier = Modifier.size(19.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = dest.label,
                                        fontSize = 10.sp,
                                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = contentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Send.route,
            modifier = Modifier
                .fillMaxSize()
                .background(com.gswift.relay.ui.theme.AppSkyGradient)
                .padding(innerPadding)
        ) {
            composable(AppDestination.Send.route) {
                SendScreen(
                    uiState = uiState,
                    onUpdateFields = viewModel::updateMailFields,
                    onSendClick = viewModel::sendEmail,
                    onClearClick = viewModel::clearMailForm,
                    onApplyLastSent = viewModel::applyLastSentEmail,
                    onDismissBanner = viewModel::dismissDeliveryBanner,
                    onGoToAccounts = { navigateTo(AppDestination.Accounts.route) },
                    onGoToAi = { navigateTo(AppDestination.Ai.route) },
                    onGoToTemplates = { navigateTo(AppDestination.Templates.route) },
                    extractEmails = viewModel::extractCleanEmails
                )
            }
            composable(AppDestination.Ai.route) {
                AiChatScreen(
                    uiState = uiState,
                    onBackToSend = { navigateTo(AppDestination.Send.route) },
                    onSendPrompt = viewModel::sendAiPrompt,
                    onSelectBank = viewModel::selectBank,
                    onSelectScenario = viewModel::selectScenario,
                    onSelectModel = viewModel::selectAiModel,
                    onSaveCustomSettings = viewModel::updateAiCustomSettings,
                    onClearChat = viewModel::clearAiChat,
                    onApplyDraft = { subj, html ->
                        viewModel.applyAiDraftToSend(subj, html)
                        navigateTo(AppDestination.Send.route)
                    }
                )
            }
            composable(AppDestination.Templates.route) {
                TemplatesScreen(
                    uiState = uiState,
                    onSaveTemplate = viewModel::saveTemplate,
                    onDeleteTemplate = viewModel::deleteTemplate,
                    onApplyTemplate = { tpl ->
                        viewModel.applyTemplateToSend(tpl)
                        navigateTo(AppDestination.Send.route)
                    },
                    onSendTest = viewModel::sendTestEmail
                )
            }
            composable(AppDestination.Terminal.route) {
                TerminalScreen(
                    uiState = uiState,
                    onRefreshKeys = viewModel::refreshKeyRotationStatus,
                    onResetKeys = viewModel::resetKeyRotationCooldowns,
                    onClearLogs = viewModel::clearLogs
                )
            }
            composable(AppDestination.Accounts.route) {
                AccountsScreen(
                    uiState = uiState,
                    onUpdateConfig = viewModel::updateSmtpConfig,
                    onSaveAndVerify = viewModel::saveAndVerifySmtp,
                    onAutoDetectDomain = viewModel::autoDetectSmtpByEmail
                )
            }
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
