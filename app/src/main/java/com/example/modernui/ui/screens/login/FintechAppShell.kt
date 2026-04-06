package com.example.modernui.ui.screens.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.modernui.ui.screens.addharpay.AadhaarPayScreen
import com.example.modernui.ui.screens.aeps.AepsScreen
import com.example.modernui.ui.screens.cashdeposite.CashDepositScreen
import com.example.modernui.ui.screens.history.HistoryScreen
import com.example.modernui.ui.screens.home.HomeContent
import com.example.modernui.ui.screens.home.HomeDrawerContent
import com.example.modernui.ui.screens.recharge.RechargeScreen
import com.example.modernui.ui.screens.report.ReportScreen
import com.example.modernui.ui.screens.wallet.WalletScreen
import com.example.modernui.ui.screens.dmt.DmtScreen
import com.example.modernui.ui.screens.mtb.MoveToBankScreen
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────
// ROUTE CONSTANTS
// ─────────────────────────────────────────────

object Routes {
    const val SHELL        = "shell"
    const val AEPS         = "aeps"
    const val RECHARGE     = "recharge"
    const val CASH_DEPOSIT = "cash_deposit"
    const val AADHAAR_PAY  = "aadhaar_pay"
    const val DMT_AIRTEL   = "dmt_airtel"
    const val DMT_JIO      = "dmt_jio"
    const val MOVE_TO_BANK = "move_to_bank"
}

// ─────────────────────────────────────────────
// SERVICE TITLE → ROUTE
// Add one line here whenever you build a new screen
// ─────────────────────────────────────────────

fun serviceRoute(title: String): String? = when (title) {
    "AEPS",
    "AEPS2"          -> Routes.AEPS
    "Mobile Recharge",
    "DTH Recharge"   -> Routes.RECHARGE
    "Cash Deposit"   -> Routes.CASH_DEPOSIT
    "Aadhar Pay"     -> Routes.AADHAAR_PAY
    "Airtel DMT"     -> Routes.DMT_AIRTEL
    "Jio DMT"        -> Routes.DMT_JIO
    "Move To Bank"   -> Routes.MOVE_TO_BANK
    else             -> null
}


// ─────────────────────────────────────────────
// ENTRY POINT
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

        composable(Routes.SHELL) {
            FintechAppShellContent(
                viewModel      = viewModel,
                onServiceClick = { title ->
                    serviceRoute(title)?.let { navController.navigate(it) }
                }
            )
        }

        composable(Routes.AEPS) {
            AepsScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Routes.RECHARGE) {
            RechargeScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Routes.CASH_DEPOSIT) {
            CashDepositScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Routes.AADHAAR_PAY) {
            AadhaarPayScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Routes.DMT_AIRTEL) {
            DmtScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Routes.DMT_JIO) {
            DmtScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Routes.MOVE_TO_BANK) {
            MoveToBankScreen(onBackClick = { navController.popBackStack() })
        }
    }
}


// ─────────────────────────────────────────────
// SHELL CONTENT
// ─────────────────────────────────────────────

@Composable
fun FintechAppShellContent(
    viewModel:      UserViewModel,
    onServiceClick: (String) -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val drawerState  = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope        = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState   = drawerState,
        drawerContent = {
            HomeDrawerContent(onClose = { scope.launch { drawerState.close() } })
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
                        0 -> HomeContent(
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
