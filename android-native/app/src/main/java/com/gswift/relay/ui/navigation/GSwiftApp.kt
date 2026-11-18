package com.gswift.relay.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.gswift.relay.ui.theme.ClaudeTerracotta
import com.gswift.relay.ui.theme.EmeraldOnline
import com.gswift.relay.ui.theme.GoldAccent
import com.gswift.relay.ui.theme.NavyPrimary
import com.gswift.relay.ui.theme.RoseError
import com.gswift.relay.ui.viewmodel.GSwiftViewModel

sealed class AppDestination(val route: String, val label: String, val icon: ImageVector) {
    data object Send : AppDestination("send", "Kirim", Icons.Rounded.Send)
    data object Ai : AppDestination("ai", "AI Chat", Icons.Rounded.AutoAwesome)
    data object Templates : AppDestination("templates", "Template", Icons.Rounded.Description)
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

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NavyPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "G",
                                    color = GoldAccent,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                            }
                            Column {
                                Text(
                                    text = "G-Swift Relay",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Native Android • Claude Mythos",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        val (dotColor, statusText) = when (uiState.smtpState) {
                            SmtpConnectionState.CONNECTED -> EmeraldOnline to "Online"
                            SmtpConnectionState.DISCONNECTED -> RoseError to "Putus"
                            SmtpConnectionState.CHECKING -> GoldAccent to "Cek..."
                            SmtpConnectionState.UNCONFIGURED -> Color(0xFF94A3B8) to "Atur"
                        }
                        Surface(
                            shape = CircleShape,
                            color = dotColor.copy(alpha = 0.12f),
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .clip(CircleShape)
                                .clickable {
                                    if (uiState.smtpState == SmtpConnectionState.UNCONFIGURED) {
                                        navigateTo(AppDestination.Accounts.route)
                                    } else {
                                        viewModel.verifySmtpConnection()
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = statusText,
                                    color = dotColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                )

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
            NavigationBar(containerColor = Color.White) {
                bottomTabs.forEach { dest ->
                    val selected = currentRoute == dest.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = { navigateTo(dest.route) },
                        icon = {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = dest.label,
                                tint = if (selected && dest == AppDestination.Ai) {
                                    ClaudeTerracotta
                                } else if (selected) {
                                    NavyPrimary
                                } else {
                                    Color(0xFF64748B)
                                }
                            )
                        },
                        label = {
                            Text(
                                text = dest.label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Send.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(AppDestination.Send.route) {
                SendScreen(
                    uiState = uiState,
                    onUpdateFields = viewModel::updateMailFields,
                    onSendClick = viewModel::sendEmail,
                    onClearClick = viewModel::clearMailForm,
                    onDismissBanner = viewModel::dismissDeliveryBanner,
                    onGoToAccounts = { navigateTo(AppDestination.Accounts.route) },
                    onGoToAi = { navigateTo(AppDestination.Ai.route) },
                    extractEmails = viewModel::extractCleanEmails
                )
            }
            composable(AppDestination.Ai.route) {
                AiChatScreen(
                    uiState = uiState,
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
