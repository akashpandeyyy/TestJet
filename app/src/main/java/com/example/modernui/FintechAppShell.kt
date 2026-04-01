package com.example.modernui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.modernui.ui.screens.AepsScreen
import com.example.modernui.ui.screens.RechargeScreen
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────
// ROUTE CONSTANTS — avoids typo bugs
// ─────────────────────────────────────────────

object Routes {
    const val SHELL    = "shell"
    const val AEPS     = "aeps"
    const val RECHARGE = "recharge"
}

// ─────────────────────────────────────────────
// SERVICE TITLE → ROUTE MAPPING
// Add new services here as you build them
// ─────────────────────────────────────────────

fun serviceRoute(title: String): String? = when (title) {
    "AEPS",
    "AEPS2"          -> Routes.AEPS
    "Mobile Recharge",
    "DTH Recharge"   -> Routes.RECHARGE
    else             -> null   // not yet built — click is silently no-op
}


// ─────────────────────────────────────────────
// ENTRY POINT
// NavHost wraps the shell so detail screens
// (AEPS, Recharge …) push on top of it cleanly
// ─────────────────────────────────────────────

@Composable
fun FintechAppShell(
    viewModel: UserViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController    = navController,
        startDestination = Routes.SHELL
    ) {

        // ── Bottom-nav shell ──────────────────
        composable(Routes.SHELL) {
            FintechAppShellContent(
                viewModel      = viewModel,
                onServiceClick = { title ->
                    serviceRoute(title)?.let { navController.navigate(it) }
                }
            )
        }

        // ── AEPS detail screen ────────────────
        composable(Routes.AEPS) {
            AepsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // ── Recharge detail screen ────────────
        composable(Routes.RECHARGE) {
            RechargeScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}


// ─────────────────────────────────────────────
// SHELL CONTENT
// Owns drawer + bottom nav + tab switching
// ─────────────────────────────────────────────

@Composable
fun FintechAppShellContent(
    viewModel:      UserViewModel,
    onServiceClick: (String) -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope       = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState   = drawerState,
        drawerContent = {
            MainHomeDrawerContent(onClose = { scope.launch { drawerState.close() } })
        }
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick  = { selectedTab = 0 },
                        icon     = { Icon(Icons.Default.Home, null) },
                        label    = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick  = { selectedTab = 1 },
                        icon     = { Icon(Icons.Default.AccountBalanceWallet, null) },
                        label    = { Text("Wallet") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick  = { selectedTab = 2 },
                        icon     = { Icon(Icons.Default.FileCopy, null) },
                        label    = { Text("Report") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick  = { selectedTab = 3 },
                        icon     = { Icon(Icons.Default.History, null) },
                        label    = { Text("History") }
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                AnimatedContent(
                    targetState    = selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label          = "tab_switch"
                ) { tab ->
                    when (tab) {
                        0 -> MainHomeContent(
                            onMenuClick    = { scope.launch { drawerState.open() } },
                            onServiceClick = onServiceClick
                        )
                        1 -> WalletScreen(viewModel)
                        2 -> ReportScreen()
                        3 -> HistoryScreen(viewModel)
                    }
                }
            }
        }
    }
}
