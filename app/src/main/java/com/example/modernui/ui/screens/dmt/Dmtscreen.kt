package com.example.modernui.ui.screens.dmt

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.modernui.ui.components.*
import com.example.modernui.ui.screens.addharpay.FingerprintScanningAnimation
import com.example.modernui.ui.screens.addharpay.VerificationFailedBanner
import com.example.modernui.ui.screens.addharpay.VerificationStep
import com.example.modernui.ui.theme.FintechColors
import kotlinx.coroutines.delay


enum class DmtScreen {
    ENTER_MOBILE,
    BENEFICIARY_LIST,
    PAY_ENTER_AMOUNT,
    PAY_SEND_OTP,
    PAY_VERIFY_OTP,
    TRANSACTION_SUCCESS,
    NEW_USER_SEND_OTP,
    NEW_USER_VERIFY_OTP,
    NEW_USER_BIOMETRIC,
    NEW_USER_SUCCESS
}


// ─────────────────────────────────────────────
// MOCK DATA
// ─────────────────────────────────────────────

data class Beneficiary(
    val id:          String,
    val name:        String,
    val accountNo:   String,
    val bankName:    String,
    val ifsc:        String,
    val initials:    String
)

// Simulated "existing" mobiles that have a beneficiary list
private val knownMobiles = setOf("91653371777", "9999999999", "8888888888")

private val transferLimits = listOf("1000", "2000", "5000",)

val mockBeneficiaries = listOf(
    Beneficiary("b1", "Ansh Sharma", "XXXX XXXX 4291", "State Bank of India", "SBIN0001234", "RS"),
    Beneficiary("b2", "Ayush Mishra", "XXXX XXXX 8803", "HDFC Bank", "HDFC0005678", "PV"),
    Beneficiary("b3", "Akhil Dwivedi", "XXXX XXXX 1147", "ICICI Bank", "ICIC0009876", "AG"),
    Beneficiary("b4", "Anurag Dwivedi", "XXXX XXXX 3366", "Punjab National Bank", "PUNB0004321", "SD"),
)


// ─────────────────────────────────────────────
// ROOT SCREEN
// ─────────────────────────────────────────────

@Composable
fun DmtScreen(
    initialTab:  Int = 0,
    onBackClick: () -> Unit = {},
    viewModel: DmtViewModel = hiltViewModel()
) {
    var currentScreen    by remember { mutableStateOf(DmtScreen.ENTER_MOBILE) }
    var senderMobile     by remember { mutableStateOf("") }
    var selectedBenef    by remember { mutableStateOf<Beneficiary?>(null) }
    var transferAmount   by remember { mutableStateOf("") }
    var otpValue         by remember { mutableStateOf("") }
    var selectedTab      by remember { mutableIntStateOf(initialTab) }

    val beneficiaries by viewModel.beneficiaries.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        // ── Top bar — title changes per screen ──
        DetailTopBar(
            title = when (currentScreen) {
                DmtScreen.ENTER_MOBILE         -> "DMT Transfer"
                DmtScreen.BENEFICIARY_LIST     -> "Beneficiaries"
                DmtScreen.PAY_ENTER_AMOUNT     -> "Send Money"
                DmtScreen.PAY_SEND_OTP,
                DmtScreen.PAY_VERIFY_OTP       -> "Confirm Payment"
                DmtScreen.TRANSACTION_SUCCESS  -> "Transfer Complete"
                DmtScreen.NEW_USER_SEND_OTP,
                DmtScreen.NEW_USER_VERIFY_OTP  -> "Register Sender"
                DmtScreen.NEW_USER_BIOMETRIC   -> "Biometric Verify"
                DmtScreen.NEW_USER_SUCCESS     -> "Registration Done"
            },
            actions = {
                Text(
                    text = balance,
                    color = Color.White,
                    modifier = Modifier.padding(end = 12.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            },
            onBackClick = {
                // Custom back — navigate within flow or go up
                when (currentScreen) {
                    DmtScreen.BENEFICIARY_LIST    -> currentScreen = DmtScreen.ENTER_MOBILE
                    DmtScreen.PAY_ENTER_AMOUNT    -> currentScreen = DmtScreen.BENEFICIARY_LIST
                    DmtScreen.PAY_SEND_OTP        -> currentScreen = DmtScreen.PAY_ENTER_AMOUNT
                    DmtScreen.PAY_VERIFY_OTP      -> currentScreen = DmtScreen.PAY_SEND_OTP
                    DmtScreen.NEW_USER_VERIFY_OTP -> currentScreen = DmtScreen.NEW_USER_SEND_OTP
                    DmtScreen.NEW_USER_BIOMETRIC  -> currentScreen = DmtScreen.NEW_USER_VERIFY_OTP
                    else                          -> onBackClick()
                }
            }
        )

        if (errorMessage != null) {
            ErrorMessageBanner(message = errorMessage!!, onDismiss = { /* Optionally clear error */ })
        }

        // ── Screen content ─────────────────────
        AnimatedContent(
            targetState    = currentScreen,
            transitionSpec = {
                (fadeIn(tween(220)) + slideInHorizontally { it / 6 })
                    .togetherWith(fadeOut(tween(180)) + slideOutHorizontally { -it / 6 })
            },
            label = "dmt_screen"
        ) { screen ->
            when (screen) {

                DmtScreen.ENTER_MOBILE -> EnterMobileScreen(
                    onKnownMobile   = { mobile ->
                        senderMobile  = mobile
                        currentScreen = DmtScreen.BENEFICIARY_LIST
                    },
                    onUnknownMobile = { mobile ->
                        senderMobile  = mobile
                        currentScreen = DmtScreen.NEW_USER_SEND_OTP
                    },
                    selectedTab     = selectedTab,
                    onTabChange     = { selectedTab = it },
                    viewModel       = viewModel
                )

                DmtScreen.BENEFICIARY_LIST -> BeneficiaryListScreen(
                    senderMobile    = senderMobile,
                    beneficiaries   = beneficiaries,
                    onPayClick      = { benef ->
                        selectedBenef = benef
                        currentScreen = DmtScreen.PAY_ENTER_AMOUNT
                    }
                )

                DmtScreen.PAY_ENTER_AMOUNT -> PayEnterAmountScreen(
                    beneficiary    = selectedBenef!!,
                    amount         = transferAmount,
                    onAmountChange = { transferAmount = it },
                    onProceed      = {
                        viewModel.sendOtp(senderMobile) { success ->
                            if (success) currentScreen = DmtScreen.PAY_VERIFY_OTP
                        }
                    },
                    isLoading      = isLoading
                )

                DmtScreen.PAY_SEND_OTP -> { /* Deprecated in new flow or integrated */ }

                DmtScreen.PAY_VERIFY_OTP -> PayVerifyOtpScreen(
                    otp         = otpValue,
                    onOtpChange = { otpValue = it },
                    senderMobile = senderMobile,
                    beneficiary  = selectedBenef!!,
                    amount       = transferAmount,
                    onVerified  = {
                        viewModel.performTransfer(selectedBenef!!, transferAmount) { success, _ ->
                            if (success) currentScreen = DmtScreen.TRANSACTION_SUCCESS
                        }
                    },
                    isLoading   = isLoading,
                    onResend    = { viewModel.sendOtp(senderMobile) {} }
                )

                DmtScreen.TRANSACTION_SUCCESS -> TransactionSuccessScreen(
                    beneficiary = selectedBenef!!,
                    amount      = transferAmount,
                    onDone      = {
                        // Reset full flow
                        senderMobile   = ""
                        selectedBenef  = null
                        transferAmount = ""
                        otpValue       = ""
                        currentScreen  = DmtScreen.ENTER_MOBILE
                    }
                )

                DmtScreen.NEW_USER_SEND_OTP -> NewUserSendOtpScreen(
                    senderMobile = senderMobile,
                    onOtpSent    = {
                        viewModel.sendOtp(senderMobile) { success ->
                            if (success) currentScreen = DmtScreen.NEW_USER_VERIFY_OTP
                        }
                    },
                    isLoading    = isLoading
                )

                DmtScreen.NEW_USER_VERIFY_OTP -> NewUserVerifyOtpScreen(
                    otp         = otpValue,
                    onOtpChange = { otpValue = it },
                    senderMobile = senderMobile,
                    onVerified  = {
                        viewModel.verifyOtp(otpValue) { success ->
                            if (success) currentScreen = DmtScreen.NEW_USER_BIOMETRIC
                        }
                    },
                    isLoading   = isLoading
                )

                DmtScreen.NEW_USER_BIOMETRIC -> NewUserBiometricScreen(
                    onVerified = { currentScreen = DmtScreen.NEW_USER_SUCCESS }
                )

                DmtScreen.NEW_USER_SUCCESS -> NewUserSuccessScreen(
                    senderMobile = senderMobile,
                    onContinue   = {
                        otpValue      = ""
                        currentScreen = DmtScreen.ENTER_MOBILE
                    }
                )
            }
        }
    }
}


// ════════════════════════════════════════════════════════
//  SCREEN 1 — ENTER MOBILE NUMBER
// ════════════════════════════════════════════════════════

@Composable
fun EnterMobileScreen(
    onKnownMobile:   (String) -> Unit,
    onUnknownMobile: (String) -> Unit,
    selectedTab:     Int,
    onTabChange:     (Int) -> Unit,
    viewModel:       DmtViewModel
) {
    var mobile     by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val mobileError = mobile.isNotEmpty() && mobile.length != 10
    val isReady     = mobile.length == 10

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        NavyHeaderCard(
            icon     = Icons.Default.SendToMobile,
            title    = "Domestic Money Transfer",
            subtitle = "Send money instantly to any bank account"
        )

        // ── Type selector card (Airtel / JIO Tabs) ─────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp),
            colors   = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(FintechColors.NavyDark, FintechColors.NavyLight)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Select DMT Provider",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                    ) {
                        listOf(
                            "Airtel" to Icons.Default.CellTower,
                            "JIO"    to Icons.Default.CellTower
                        ).forEachIndexed { index, (label, icon) ->
                            val isSelected = selectedTab == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color.White else Color.Transparent)
                                    .clickable { onTabChange(index) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        icon, null,
                                        tint     = if (isSelected) FintechColors.NavyDark else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        label,
                                        color      = if (isSelected) FintechColors.NavyDark else Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        style      = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        SectionCard(title = "Sender Details", icon = Icons.Default.Person) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                NavyOutlinedField(
                    value         = mobile,
                    onValueChange = { if (it.all(Char::isDigit)) mobile = it },
                    label         = "Sender Mobile Number *",
                    placeholder   = "Enter 10-digit mobile",
                    leadingIcon   = Icons.Default.Phone,
                    keyboardType  = KeyboardType.Phone,
                    maxLength     = 10,
                    isError       = mobileError,
                    errorMessage  = "Enter a valid 10-digit mobile number",
                    trailingIcon  = if (isReady) ({
                        Icon(Icons.Default.CheckCircle, null,
                            tint = FintechColors.SuccessGreen)
                    }) else null
                )

                Surface(
                    shape  = RoundedCornerShape(10.dp),
                    color  = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, null,
                            tint     = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp))
                        Text(
                            "Enter the sender's registered mobile to look up their beneficiary list",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                if (isLoading) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier  = Modifier.size(20.dp),
                            color     = FintechColors.NavyDark,
                            strokeWidth = 2.dp
                        )
                        Text("Checking mobile number...",
                            style = MaterialTheme.typography.bodySmall,
                            color = FintechColors.NavyDark)
                    }
                }

                NavyPrimaryButton(
                    text    = "Check & Continue",
                    onClick = {
                        viewModel.checkMobile(mobile) { isKnown ->
                            if (isKnown) onKnownMobile(mobile)
                            else onUnknownMobile(mobile)
                        }
                    },
                    enabled = isReady && !isLoading,
                    icon    = Icons.Default.Search
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}


// ════════════════════════════════════════════════════════
//  SCREEN 2 — BENEFICIARY LIST
// ════════════════════════════════════════════════════════

@Composable
fun BeneficiaryListScreen(
    senderMobile:  String,
    beneficiaries: List<Beneficiary>,
    onPayClick:    (Beneficiary) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sender info strip
        Surface(
            shape    = RoundedCornerShape(12.dp),
            color    = FintechColors.NavyDark.copy(alpha = 0.06f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape    = CircleShape,
                    color    = FintechColors.NavyDark.copy(alpha = 0.12f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Person, null,
                            tint = FintechColors.NavyDark, modifier = Modifier.size(22.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Sender Verified",
                        style      = MaterialTheme.typography.labelSmall,
                        color      = colorScheme.outline)
                    Text(senderMobile,
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color      = FintechColors.NavyDark)
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = FintechColors.SuccessGreenLight
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null,
                            tint     = FintechColors.SuccessGreen,
                            modifier = Modifier.size(12.dp))
                        Text("Active",
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color      = FintechColors.SuccessGreenDark)
                    }
                }
            }
        }

        // Header
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("Saved Beneficiaries",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = colorScheme.onBackground)
            Text("${beneficiaries.size} accounts",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.outline)
        }

        // Beneficiary cards
        beneficiaries.forEach { benef ->
            BeneficiaryCard(
                beneficiary = benef,
                onPayClick  = { onPayClick(benef) }
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun BeneficiaryCard(
    beneficiary: Beneficiary,
    onPayClick:  () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar circle
            Surface(
                shape    = CircleShape,
                color    = FintechColors.NavyDark.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        beneficiary.initials,
                        color      = FintechColors.NavyDark,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(beneficiary.name,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color      = colorScheme.onSurface)
                Text(beneficiary.bankName,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.outline)
                Text(beneficiary.accountNo,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.outline)
            }

            Button(
                onClick = onPayClick,
                shape   = RoundedCornerShape(10.dp),
                colors  = ButtonDefaults.buttonColors(
                    containerColor = FintechColors.NavyDark,
                    contentColor   = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Send, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Pay", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}


// ════════════════════════════════════════════════════════
//  SCREEN 3 — PAY: ENTER AMOUNT
// ════════════════════════════════════════════════════════

@Composable
fun PayEnterAmountScreen(
    beneficiary:    Beneficiary,
    amount:         String,
    onAmountChange: (String) -> Unit,
    onProceed:      () -> Unit,
    isLoading:      Boolean = false
) {
    val amountError = amount.isNotEmpty() && (amount.toDoubleOrNull() ?: 0.0) <= 0.0
    val isReady     = amount.isNotEmpty() && !amountError

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Beneficiary summary at top
        BeneficiarySummaryStrip(beneficiary = beneficiary)

        SectionCard(title = "Transfer Amount", icon = Icons.Default.CurrencyRupee) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                NavyOutlinedField(
                    value         = amount,
                    onValueChange = onAmountChange,
                    label         = "Amount (₹) *",
                    placeholder   = "Enter amount to send",
                    leadingIcon   = Icons.Default.CurrencyRupee,
                    keyboardType  = KeyboardType.Decimal,
                    isError       = amountError,
                    errorMessage  = "Enter a valid amount"
                )

                // Quick presets
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    transferLimits.forEach { preset ->
                        FilterChip(
                            selected = amount == preset,
                            onClick  = { onAmountChange(preset) },
                            label    = {
                                Text("₹$preset",
                                    style = MaterialTheme.typography.labelSmall)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Daily limit info
                Surface(
                    shape    = RoundedCornerShape(10.dp),
                    color    = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, null,
                            tint     = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp))
                        Text("Daily limit: ₹25,000  •  Per transaction: ₹10,000",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }

        NavyPrimaryButton(
            text    = if (isLoading) "Processing..." else "Send OTP to Confirm",
            onClick = onProceed,
            enabled = isReady && !isLoading,
            icon    = Icons.Default.Sms
        )

        Spacer(Modifier.height(8.dp))
    }
}


// ════════════════════════════════════════════════════════
//  SCREEN 4 — PAY: SEND OTP
// ════════════════════════════════════════════════════════

@Composable
fun PaySendOtpScreen(
    senderMobile: String,
    beneficiary:  Beneficiary,
    amount:       String,
    onOtpSent:    () -> Unit
) {
    var isSending by remember { mutableStateOf(false) }

    LaunchedEffect(isSending) {
        if (isSending) {
            delay(1500)
            isSending = false
            onOtpSent()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BeneficiarySummaryStrip(beneficiary = beneficiary)

        // Transaction preview card
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(FintechColors.NavyDark, FintechColors.NavyLight)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.fillMaxWidth()
                ) {
                    Text("You are sending",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelMedium)
                    Text("₹$amount",
                        color      = Color.White,
                        style      = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("to ${beneficiary.name}",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium)
                    Text(beneficiary.bankName,
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        SectionCard(title = "OTP Verification", icon = Icons.Default.Sms) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("An OTP will be sent to",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline)
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Phone, null,
                        tint = FintechColors.NavyDark, modifier = Modifier.size(18.dp))
                    Text("+91 ${senderMobile.chunked(5).joinToString(" ")}",
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color      = FintechColors.NavyDark)
                }
            }
        }

        NavyPrimaryButton(
            text    = if (isSending) "Sending OTP..." else "Send OTP",
            onClick = { isSending = true },
            enabled = !isSending,
            icon    = Icons.Default.Sms
        )

        Spacer(Modifier.height(8.dp))
    }
}


// ════════════════════════════════════════════════════════
//  SCREEN 5 — PAY: VERIFY OTP
// ════════════════════════════════════════════════════════

@Composable
fun PayVerifyOtpScreen(
    otp:          String,
    onOtpChange:  (String) -> Unit,
    senderMobile: String,
    beneficiary:  Beneficiary,
    amount:       String,
    onVerified:   () -> Unit,
    isLoading:    Boolean = false,
    onResend:     () -> Unit = {}
) {
    var resendTimer by remember { mutableIntStateOf(30) }

    // Countdown timer
    LaunchedEffect(Unit) {
        while (resendTimer > 0) {
            delay(1000)
            resendTimer--
        }
    }

    val isReady = otp.length == 6

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BeneficiarySummaryStrip(beneficiary = beneficiary)

        SectionCard(title = "Enter OTP", icon = Icons.Default.Password) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Text("Enter the 6-digit OTP sent to +91 $senderMobile",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline)

                // OTP box row
                OtpInputRow(
                    otp         = otp,
                    onOtpChange = { if (it.all(Char::isDigit) && it.length <= 6) onOtpChange(it) }
                )

                // Resend row
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        if (resendTimer > 0) "Resend in ${resendTimer}s"
                        else "Didn't receive OTP?",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (resendTimer == 0) {
                        TextButton(onClick = {
                            resendTimer = 30
                            onResend()
                        }) {
                            Text("Resend OTP",
                                color      = FintechColors.NavyDark,
                                fontWeight = FontWeight.Bold,
                                style      = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                // Transfer summary
                TransferSummaryRow(
                    beneficiary = beneficiary,
                    amount      = amount
                )
            }
        }

        NavyPrimaryButton(
            text    = if (isLoading) "Verifying..." else "Verify & Transfer",
            onClick = onVerified,
            enabled = isReady && !isLoading,
            icon    = Icons.Default.VerifiedUser
        )

        Spacer(Modifier.height(8.dp))
    }
}


// ════════════════════════════════════════════════════════
//  SCREEN 6 — TRANSACTION SUCCESS
// ════════════════════════════════════════════════════════

@Composable
fun TransactionSuccessScreen(
    beneficiary: Beneficiary,
    amount:      String,
    onDone:      () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    // Bounce-in animation for the success icon
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }

    val scale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "success_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        // Success icon
        Surface(
            shape    = CircleShape,
            color    = FintechColors.SuccessGreenLight,
            modifier = Modifier
                .size(100.dp)
                .scale(scale)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Default.CheckCircle, null,
                    tint     = FintechColors.SuccessGreen,
                    modifier = Modifier.size(56.dp))
            }
        }

        Text("Transfer Successful!",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color      = FintechColors.SuccessGreenDark,
            textAlign  = TextAlign.Center)

        Text("₹$amount sent to ${beneficiary.name}",
            style     = MaterialTheme.typography.bodyMedium,
            color     = colorScheme.outline,
            textAlign = TextAlign.Center)

        // Receipt card
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier              = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(Icons.Default.Receipt, null,
                        tint = FintechColors.NavyDark, modifier = Modifier.size(18.dp))
                    Text("Transaction Receipt",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = FintechColors.NavyDark)
                }
                HorizontalDivider(color = FintechColors.NavyDark.copy(alpha = 0.12f))

                listOf(
                    "Beneficiary" to beneficiary.name,
                    "Account"     to beneficiary.accountNo,
                    "Bank"        to beneficiary.bankName,
                    "IFSC"        to beneficiary.ifsc,
                    "Amount"      to "₹$amount",
                    "Status"      to "SUCCESS",
                    "Txn Ref"     to "DMT${System.currentTimeMillis().toString().takeLast(8)}"
                ).forEach { (label, value) ->
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.outline)
                        Text(value,
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = if (label == "Status") FontWeight.Bold else FontWeight.Medium,
                            color      = if (label == "Status") FintechColors.SuccessGreen
                            else colorScheme.onSurface)
                    }
                }
            }
        }

        NavyPrimaryButton(
            text    = "New Transfer",
            onClick = onDone,
            icon    = Icons.Default.Add
        )

        Spacer(Modifier.height(8.dp))
    }
}


// ════════════════════════════════════════════════════════
//  SCREEN 7 — NEW USER: SEND OTP
// ════════════════════════════════════════════════════════

@Composable
fun NewUserSendOtpScreen(
    senderMobile: String,
    onOtpSent:    () -> Unit,
    isLoading:    Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Not-found banner
        Surface(
            shape    = RoundedCornerShape(12.dp),
            color    = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.PersonAdd, null,
                    tint     = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(22.dp))
                Column {
                    Text("New Sender Detected",
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.error)
                    Text("$senderMobile is not registered. Complete verification to register.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }

        SectionCard(title = "Step 1 — OTP Verification", icon = Icons.Default.Sms) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("We'll send an OTP to verify this number",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline)
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Phone, null,
                        tint = FintechColors.NavyDark, modifier = Modifier.size(18.dp))
                    Text("+91 ${senderMobile.chunked(5).joinToString(" ")}",
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color      = FintechColors.NavyDark)
                }
            }
        }

        RegistrationStepBar(currentStep = 1)

        NavyPrimaryButton(
            text    = if (isLoading) "Sending..." else "Send OTP",
            onClick = onOtpSent,
            enabled = !isLoading,
            icon    = Icons.Default.Sms
        )
    }
}


// ════════════════════════════════════════════════════════
//  SCREEN 8 — NEW USER: VERIFY OTP
// ════════════════════════════════════════════════════════

@Composable
fun NewUserVerifyOtpScreen(
    otp:          String,
    onOtpChange:  (String) -> Unit,
    senderMobile: String,
    onVerified:   () -> Unit,
    isLoading:    Boolean = false
) {
    var resendTimer by remember { mutableIntStateOf(30) }

    LaunchedEffect(Unit) { while (resendTimer > 0) { delay(1000); resendTimer-- } }

    val isReady = otp.length == 6

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RegistrationStepBar(currentStep = 1)

        SectionCard(title = "Enter OTP", icon = Icons.Default.Password) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Enter the 6-digit OTP sent to +91 $senderMobile",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline)

                OtpInputRow(
                    otp         = otp,
                    onOtpChange = { if (it.all(Char::isDigit) && it.length <= 6) onOtpChange(it) }
                )

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        if (resendTimer > 0) "Resend in ${resendTimer}s" else "Didn't receive OTP?",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (resendTimer == 0) {
                        TextButton(onClick = { resendTimer = 30 }) {
                            Text("Resend OTP",
                                color      = FintechColors.NavyDark,
                                fontWeight = FontWeight.Bold,
                                style      = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        NavyPrimaryButton(
            text    = if (isLoading) "Verifying..." else "Verify OTP",
            onClick = onVerified,
            enabled = isReady && !isLoading,
            icon    = Icons.Default.VerifiedUser
        )
    }
}


// ════════════════════════════════════════════════════════
//  SCREEN 9 — NEW USER: BIOMETRIC
// ════════════════════════════════════════════════════════

@Composable
fun NewUserBiometricScreen(
    onVerified: () -> Unit
) {
    var scanState by remember { mutableStateOf(VerificationStep.IDLE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RegistrationStepBar(currentStep = 2)

        SectionCard(title = "Step 2 — Biometric Verification", icon = Icons.Default.Fingerprint) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                when (scanState) {
                    VerificationStep.IDLE -> {
                        Surface(
                            shape  = RoundedCornerShape(10.dp),
                            color  = FintechColors.NavyDark.copy(alpha = 0.06f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Info, null,
                                    tint = FintechColors.NavyDark, modifier = Modifier.size(18.dp))
                                Text("Place sender's finger on the biometric device to complete registration",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FintechColors.NavyDark)
                            }
                        }
                        NavyPrimaryButton(
                            text    = "Start Biometric Scan",
                            onClick = { scanState = VerificationStep.SCANNING },
                            icon    = Icons.Default.Fingerprint
                        )
                    }

                    VerificationStep.SCANNING -> {
                        FingerprintScanningAnimation(
                            onScanComplete = { success ->
                                scanState = if (success) VerificationStep.SUCCESS
                                else VerificationStep.FAILED
                            }
                        )
                    }

                    VerificationStep.SUCCESS -> {
                        LaunchedEffect(Unit) { delay(600); onVerified() }
                        Surface(
                            shape  = RoundedCornerShape(12.dp),
                            color  = FintechColors.SuccessGreenLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, null,
                                    tint = FintechColors.SuccessGreen, modifier = Modifier.size(24.dp))
                                Text("Biometric verified!",
                                    style      = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = FintechColors.SuccessGreenDark)
                            }
                        }
                    }

                    VerificationStep.FAILED -> {
                        VerificationFailedBanner(onRetry = { scanState = VerificationStep.IDLE })
                    }
                }
            }
        }
    }
}


// ════════════════════════════════════════════════════════
//  SCREEN 10 — NEW USER: SUCCESS
// ════════════════════════════════════════════════════════

@Composable
fun NewUserSuccessScreen(
    senderMobile: String,
    onContinue:   () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }

    val scale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "success_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(40.dp))

        // Animated success icon
        Surface(
            shape    = CircleShape,
            color    = FintechColors.SuccessGreenLight,
            modifier = Modifier
                .size(110.dp)
                .scale(scale)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Default.HowToReg, null,
                    tint     = FintechColors.SuccessGreen,
                    modifier = Modifier.size(60.dp))
            }
        }

        Text("User Validated Successfully!",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color      = FintechColors.SuccessGreenDark,
            textAlign  = TextAlign.Center)

        Text("+91 $senderMobile has been registered and verified via OTP and biometrics.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = colorScheme.outline,
            textAlign = TextAlign.Center)

        // Verification summary
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    Icons.Default.Phone       to "Mobile"       to senderMobile,
                    Icons.Default.Sms         to "OTP"          to "Verified ✓",
                    Icons.Default.Fingerprint to "Biometric"    to "Verified ✓",
                    Icons.Default.Person      to "Status"       to "Active"
                ).forEach { (iconLabel, value) ->
                    val (icon, label) = iconLabel
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(icon, null,
                            tint = FintechColors.NavyDark.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp))
                        Text(label,
                            style    = MaterialTheme.typography.bodySmall,
                            color    = colorScheme.outline,
                            modifier = Modifier.width(80.dp))
                        Text(value,
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color      = if (value.contains("✓") || value == "Active")
                                FintechColors.SuccessGreen
                            else colorScheme.onSurface)
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        NavyPrimaryButton(
            text    = "Start New Transfer",
            onClick = onContinue,
            icon    = Icons.Default.ArrowForward
        )
    }
}


// ─────────────────────────────────────────────
// SHARED SUB-COMPONENTS
// ─────────────────────────────────────────────

@Composable
fun BeneficiarySummaryStrip(beneficiary: Beneficiary) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = FintechColors.NavyDark.copy(alpha = 0.06f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape    = CircleShape,
                color    = FintechColors.NavyDark.copy(alpha = 0.12f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(beneficiary.initials,
                        color      = FintechColors.NavyDark,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 13.sp)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(beneficiary.name,
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color      = FintechColors.NavyDark)
                Text("${beneficiary.bankName}  •  ${beneficiary.accountNo}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.outline)
            }
        }
    }
}

@Composable
fun TransferSummaryRow(beneficiary: Beneficiary, amount: String) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        shape  = RoundedCornerShape(10.dp),
        color  = FintechColors.NavyDark.copy(alpha = 0.05f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("To", style = MaterialTheme.typography.labelSmall, color = colorScheme.outline)
                Text(beneficiary.name,
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Amount", style = MaterialTheme.typography.labelSmall, color = colorScheme.outline)
                Text("₹$amount",
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color      = FintechColors.NavyDark)
            }
        }
    }
}

/** 6-box OTP input row */
@Composable
fun OtpInputRow(
    otp:         String,
    onOtpChange: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(6) { index ->
            val char = otp.getOrNull(index)?.toString() ?: ""

            OutlinedTextField(
                value         = char,
                onValueChange = {},
                modifier      = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape         = RoundedCornerShape(10.dp),
                textStyle     = LocalTextStyle.current.copy(
                    textAlign  = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp
                ),
                singleLine    = true,
                readOnly      = true,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = FintechColors.NavyDark,
                    unfocusedBorderColor = if (char.isNotEmpty())
                        FintechColors.NavyDark.copy(alpha = 0.6f)
                    else
                        colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor   = if (char.isNotEmpty())
                        FintechColors.NavyDark.copy(alpha = 0.06f)
                    else Color.Transparent,
                    unfocusedContainerColor = if (char.isNotEmpty())
                        FintechColors.NavyDark.copy(alpha = 0.06f)
                    else Color.Transparent
                )
            )
        }
    }

    // Hidden actual input field drives the OTP boxes
    OutlinedTextField(
        value         = otp,
        onValueChange = onOtpChange,
        modifier      = Modifier
            .fillMaxWidth()
            .height(1.dp),
        colors        = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor   = Color.Transparent
        )
    )
}

/** Registration progress bar — 3 steps */
@Composable
fun RegistrationStepBar(currentStep: Int) {
    val colorScheme = MaterialTheme.colorScheme
    val steps = listOf("OTP Sent", "OTP Verified", "Biometric")

    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val step     = index + 1
            val isDone   = currentStep > step
            val isActive = currentStep == step

            Column(
                modifier            = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape  = CircleShape,
                    color  = when {
                        isDone   -> FintechColors.SuccessGreen
                        isActive -> FintechColors.NavyDark
                        else     -> colorScheme.outline.copy(alpha = 0.2f)
                    },
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (isDone)
                            Icon(Icons.Default.Check, null,
                                tint = Color.White, modifier = Modifier.size(14.dp))
                        else
                            Text("$step",
                                color      = if (isActive) Color.White else colorScheme.outline,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 12.sp)
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(label,
                    style     = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color     = when {
                        isDone   -> FintechColors.SuccessGreen
                        isActive -> FintechColors.NavyDark
                        else     -> colorScheme.outline
                    },
                    textAlign = TextAlign.Center)
            }

            if (index < steps.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(0.4f)
                        .height(2.dp)
                        .padding(bottom = 16.dp)
                        .background(
                            if (isDone) FintechColors.SuccessGreen
                            else colorScheme.outline.copy(alpha = 0.2f)
                        )
                )
            }
        }
    }
}


// ─────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────

@Preview(name = "DMT – Light", showBackground = true)
@Preview(name = "DMT – Dark",  showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewDmtScreen() {
    MaterialTheme { DmtScreen() }
}

@Preview(name = "Beneficiary List", showBackground = true)
@Composable
fun PreviewBeneficiaryList() {
    MaterialTheme {
        BeneficiaryListScreen(
            senderMobile  = "9876543210",
            beneficiaries = mockBeneficiaries,
            onPayClick    = {}
        )
    }
}

@Preview(name = "New User Success", showBackground = true)
@Composable
fun PreviewNewUserSuccess() {
    MaterialTheme {
        NewUserSuccessScreen(senderMobile = "9876543210", onContinue = {})
    }
}

@Preview(name = "Transaction Success", showBackground = true)
@Composable
fun PreviewTransactionSuccess() {
    MaterialTheme {
        TransactionSuccessScreen(
            beneficiary = mockBeneficiaries[0],
            amount      = "5000",
            onDone      = {}
        )
    }
}