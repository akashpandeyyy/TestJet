package com.example.modernui.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
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
import com.example.modernui.ui.screens.home.HomeViewModel
import com.example.modernui.ui.screens.recharge.RechargeScreen
import com.example.modernui.ui.screens.report.ReportScreen
import com.example.modernui.ui.screens.wallet.WalletScreen
import com.example.modernui.ui.screens.dmt.DmtScreen
import com.example.modernui.ui.screens.mtb.MoveToBankScreen
import com.example.modernui.ui.screens.bbps.BbpsScreen
import com.example.modernui.ui.screens.bbps.BookingInsuranceScreen
import com.example.modernui.ui.screens.cms.AirtelCmsScreen
import com.example.modernui.ui.screens.cms.InsuranceDetailScreen
import com.example.modernui.ui.screens.pan.NsdlPanApplyScreen
import com.example.modernui.ui.screens.aeps.AepsViewModel
import com.example.modernui.ui.screens.common.TwoFaConfig
import com.example.modernui.ui.screens.common.TwoFaStep
import com.example.modernui.ui.screens.common.TwoFactorAuthScreen
import com.example.modernui.ui.screens.login.FintechLoginScreenM3
import com.example.modernui.ui.screens.login.UserDetailScreenM3
import com.example.modernui.ui.screens.login.UserViewModel
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
    const val MOVE_TO_BANK      = "move_to_bank"
    const val BBPS             = "bbps"
    const val BOOKING_INSURANCE = "booking_insurance"
    const val NSDL_PAN         = "nsdl_pan"
    const val UTI_PAN          = "uti_pan"
    const val AIRTEL_CMS       = "airtel_cms"
    const val INSURANCE_DETAIL = "insurance_detail"
    const val TWO_FA           = "two_fa"
    const val USER_PROFILE     = "user_profile"
    const val LOGIN            = "login"
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
    "Move To Bank"       -> Routes.MOVE_TO_BANK
    "BBPS"                -> Routes.BBPS
    "Booking Insurance"   -> Routes.BOOKING_INSURANCE
    "NSDL Pan Apply"      -> Routes.NSDL_PAN
    "UTI PAN Apply"       -> Routes.UTI_PAN
    "Airtel CMS"          -> Routes.AIRTEL_CMS
    "Insurance Detail"    -> Routes.INSURANCE_DETAIL
    else                  -> null
    // Note: 2FA is used as a wrapper via TwoFaGate, not a direct route tile
}


// ─────────────────────────────────────────────
// ENTRY POINT
// ─────────────────────────────────────────────

@Composable
fun FintechAppShell(
    viewModel: UserViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    NavHost(
        navController    = navController,
        startDestination = Routes.SHELL
    ) {

        composable(Routes.SHELL) {
            val aepsViewModel: AepsViewModel = hiltViewModel()
            FintechAppShellContent(
                viewModel      = viewModel,
                homeViewModel  = homeViewModel,
                onServiceClick = { title ->
                    val route = serviceRoute(title)
                    if (route == Routes.AEPS) {
                        aepsViewModel.checkAepsStatusAndNavigate(
                            onNavigateToAeps = { navController.navigate(Routes.AEPS) },
                            onNavigateTo2FA = { navController.navigate(Routes.TWO_FA) }
                        )
                    } else if (route != null) {
                        navController.navigate(route)
                    }
                },
                onProfileClick = {
                    navController.navigate(Routes.USER_PROFILE)
                },
                onLogout = {
                    homeViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SHELL) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.USER_PROFILE) {
            UserDetailScreenM3(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onContinueToDashboard = { navController.popBackStack() }
            )
        }

        composable(Routes.LOGIN) {
            FintechLoginScreenM3(
                onLoginSuccess = {
                    navController.navigate(Routes.SHELL) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onRegisterClick = { /* Handled by AppNavigation or add Route if needed */ }
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

        composable(Routes.BBPS) {
            BbpsScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Routes.BOOKING_INSURANCE) {
            BookingInsuranceScreen(onBackClick = { navController.popBackStack() })
        }

        // NSDL and UTI both use the same form — UTI can get its own screen later
        composable(Routes.NSDL_PAN) {
            NsdlPanApplyScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Routes.UTI_PAN) {
            NsdlPanApplyScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Routes.AIRTEL_CMS) {
            AirtelCmsScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Routes.INSURANCE_DETAIL) {
            InsuranceDetailScreen(onBackClick = { navController.popBackStack() })
        }

        // ── Universal 2FA screen — standalone route ────────────────
        composable(Routes.TWO_FA) {
            TwoFactorAuthScreen(
                config = TwoFaConfig(
                    title = "Two-Factor Authentication",
                    subtitle = "Verify your identity to continue",
                    serviceName = "AEPS Service",
                    steps = listOf(TwoFaStep.FACE_VERIFICATION)
                ),
                onVerified  = {
                    navController.popBackStack() // Go back to shell
                    navController.navigate(Routes.AEPS) // Then to AEPS
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // ── Example: AEPS wrapped behind 2FA gate ──────────────────
        // Uncomment and use this pattern for any screen that needs 2FA:
        //
        // composable("aeps_secure") {
        //     TwoFaGate(
        //         config = TwoFaConfig(
        //             title       = "AEPS Verification",
        //             subtitle    = "Verify identity before AEPS transaction",
        //             serviceName = "AEPS",
        //             serviceIcon = Icons.Default.Face,
        //             steps       = listOf(TwoFaStep.FACE_VERIFICATION)
        //         ),
        //         onCancel = { navController.popBackStack() }
        //     ) {
        //         AepsScreen(onBackClick = { navController.popBackStack() })
        //     }
        // }
    }
}


// ─────────────────────────────────────────────
// SHELL CONTENT
// ─────────────────────────────────────────────

@Composable
fun FintechAppShellContent(
    viewModel: UserViewModel,
    homeViewModel: HomeViewModel = hiltViewModel(),
    onServiceClick: (String) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val drawerState  = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope        = rememberCoroutineScope()
    val userName by homeViewModel.userName.collectAsState()

    ModalNavigationDrawer(
        drawerState   = drawerState,
        drawerContent = {
            HomeDrawerContent(
                userName = userName,
                onClose = { scope.launch { drawerState.close() } },
                onProfileClick = {
                    scope.launch { drawerState.close() }
                    onProfileClick()
                },
                onLogoutClick = {
                    scope.launch { drawerState.close() }
                    onLogout()
                }
            )
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
                            viewModel      = homeViewModel,
                            onMenuClick    = { scope.launch { drawerState.open() } },
                            onServiceClick = onServiceClick,
                            onLogout       = onLogout
                        )
                        1 -> WalletScreen(onMenuClick = { scope.launch { drawerState.open() } })
                        2 -> ReportScreen(onMenuClick = { scope.launch { drawerState.open() } })
                        3 -> HistoryScreen(viewModel = viewModel, onMenuClick = { scope.launch { drawerState.open() } })
                    }
                }
            }
        }
    }
}