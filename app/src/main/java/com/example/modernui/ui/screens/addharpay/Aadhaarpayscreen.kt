package com.example.modernui.ui.screens.addharpay

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modernui.ui.components.*
import com.example.modernui.ui.screens.cashdeposite.DeviceSelectionSheet
import com.example.modernui.ui.screens.cashdeposite.FingerprintDevice
import com.example.modernui.ui.screens.cashdeposite.SelectedDeviceCard
import com.example.modernui.ui.theme.FintechColors
import kotlinx.coroutines.delay


// ─────────────────────────────────────────────
// VERIFICATION STATE MACHINE
// ─────────────────────────────────────────────

enum class VerificationStep {
    IDLE,        // waiting for agent to enter aadhaar + pick device
    SCANNING,    // fingerprint capture in progress (simulated)
    SUCCESS,     // agent verified — show transaction form
    FAILED       // scan failed — allow retry
}

private val fingerprintDevices = listOf(
    FingerprintDevice(
        id           = "mantra_mfs100",
        name         = "Mantra MFS100",
        model        = "MFS100",
        manufacturer = "Mantra Softech",
        isConnected  = true
    ),
    FingerprintDevice(
        id           = "mantra_mfs110",
        name         = "Mantra MFS110",
        model        = "MFS110",
        manufacturer = "Mantra Softech",
        isConnected  = false
    ),
    FingerprintDevice(
        id           = "morpho_mso300",
        name         = "Morpho MSO 300",
        model        = "MSO 300",
        manufacturer = "IDEMIA (Morpho)",
        isConnected  = false
    ),
    FingerprintDevice(
        id           = "morpho_mso1300",
        name         = "Morpho MSO 1300",
        model        = "MSO 1300 E3",
        manufacturer = "IDEMIA (Morpho)",
        isConnected  = false
    ),
    FingerprintDevice(
        id           = "startek_fm220u",
        name         = "Startek FM220U",
        model        = "FM220U",
        manufacturer = "Startek",
        isConnected  = false
    )
)


// ─────────────────────────────────────────────
// AADHAAR PAY SCREEN
// ─────────────────────────────────────────────

@Composable
fun AadhaarPayScreen(
    onBackClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    // ── Step 1 state (agent auth) ─────────────
    var agentAadhaar     by remember { mutableStateOf("") }
    var selectedDeviceId by remember { mutableStateOf<String>(fingerprintDevices[0].id) }
    var showDeviceSheet  by remember { mutableStateOf(false) }
    var verifyStep       by remember { mutableStateOf(VerificationStep.IDLE) }

    // ── Step 2 state (transaction form) ───────
    var custAadhaar     by remember { mutableStateOf("") }
    var custMobile      by remember { mutableStateOf("") }
    var amount          by remember { mutableStateOf("") }
    var selectedBank    by remember { mutableStateOf("") }
    var selectedTxnType by remember { mutableStateOf("") }

    // ── Derived ───────────────────────────────
    val isAgentFormReady = agentAadhaar.length == 12 && selectedDeviceId.isNotEmpty()
    val agentAadhaarError = agentAadhaar.isNotEmpty() && agentAadhaar.length != 12
    val isVerified       = verifyStep == VerificationStep.SUCCESS

    val custAadhaarError = custAadhaar.isNotEmpty() && custAadhaar.length != 12
    val custMobileError  = custMobile.isNotEmpty()  && custMobile.length  != 10
    val amountError      = amount.isNotEmpty() && (amount.toDoubleOrNull() ?: 0.0) <= 0.0
    val needsAmount      = selectedTxnType == "Pay to Merchant" || selectedTxnType == "Cash Withdrawal"
    val isTxnFormValid   = isVerified
            && custAadhaar.length == 12
            && custMobile.length  == 10
            && selectedBank.isNotEmpty()
            && selectedTxnType.isNotEmpty()
            && (!needsAmount || (amount.isNotEmpty() && !amountError))

    val selectedDeviceObj = fingerprintDevices.find { it.id == selectedDeviceId }

    val banks = listOf(
        "State Bank of India",  "Punjab National Bank", "Bank of Baroda",
        "Canara Bank",          "HDFC Bank",            "ICICI Bank",
        "Axis Bank",            "Union Bank of India",  "Indian Bank",
        "Bank of India",        "Central Bank of India","Bank of Maharashtra"
    )
    val txnTypes = listOf(
        "Pay to Merchant", "Cash Withdrawal", "Balance Enquiry", "Mini Statement"
    )

    // ── Device bottom sheet ───────────────────
    if (showDeviceSheet) {
        DeviceSelectionSheet(
            devices          = fingerprintDevices,
            selectedDeviceId = selectedDeviceId,
            onDeviceSelected = { device ->
                selectedDeviceId = device.id
                showDeviceSheet  = false
            },
            onDismiss = { showDeviceSheet = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {

        // ── Top bar ───────────────────────────
        DetailTopBar(
            title       = "Aadhaar Pay",
            onBackClick = onBackClick,
            actions     = {
                // Show verified badge in topbar once authenticated
                AnimatedVisibility(visible = isVerified) {
                    Surface(
                        shape  = RoundedCornerShape(20.dp),
                        color  = FintechColors.SuccessGreenLight,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.VerifiedUser, null,
                                tint     = FintechColors.SuccessGreen,
                                modifier = Modifier.size(14.dp))
                            Text("Verified",
                                style      = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color      = FintechColors.SuccessGreenDark)
                        }
                    }
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Header banner ─────────────────
            NavyHeaderCard(
                icon     = Icons.Default.Pin,
                title    = "Aadhaar Pay",
                subtitle = "2-factor agent verification before transaction"
            )

            // ── STEP INDICATOR ────────────────
            StepIndicator(currentStep = if (isVerified) 2 else 1)

            // ═══════════════════════════════════
            // STEP 1 — AGENT VERIFICATION
            // ═══════════════════════════════════

            Step1AgentVerification(
                agentAadhaar      = agentAadhaar,
                onAadhaarChange   = {
                    if (it.all(Char::isDigit)) agentAadhaar = it
                    // reset if agent changes aadhaar after verify
                    if (verifyStep == VerificationStep.SUCCESS) verifyStep = VerificationStep.IDLE
                },
                agentAadhaarError = agentAadhaarError,
                selectedDevice    = selectedDeviceObj,
                onDeviceClick     = { showDeviceSheet = true },
                verifyStep        = verifyStep,
                isFormReady       = isAgentFormReady,
                onScanClick       = { verifyStep = VerificationStep.SCANNING },
                onVerifyDone      = { success ->
                    verifyStep = if (success) VerificationStep.SUCCESS
                    else VerificationStep.FAILED
                },
                onRetry           = { verifyStep = VerificationStep.IDLE }
            )

            // ═══════════════════════════════════
            // STEP 2 — TRANSACTION FORM
            // Hidden until Step 1 passes
            // ═══════════════════════════════════

            AnimatedVisibility(
                visible = isVerified,
                enter   = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit    = fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    // ── Customer details ──────
                    SectionCard(
                        title = "Customer Details",
                        icon  = Icons.Default.Person
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                            NavyOutlinedField(
                                value         = custAadhaar,
                                onValueChange = { if (it.all(Char::isDigit)) custAadhaar = it },
                                label         = "Customer Aadhaar *",
                                placeholder   = "Enter 12-digit Aadhaar",
                                leadingIcon   = Icons.Default.CreditCard,
                                keyboardType  = KeyboardType.Number,
                                maxLength     = 12,
                                isError       = custAadhaarError,
                                errorMessage  = "Aadhaar must be exactly 12 digits",
                                trailingIcon  = if (custAadhaar.length == 12) ({
                                    Icon(Icons.Default.CheckCircle, null,
                                        tint = FintechColors.SuccessGreen)
                                }) else null
                            )

                            // Masked preview
                            if (custAadhaar.length == 12) {
                                Surface(
                                    shape    = RoundedCornerShape(8.dp),
                                    color    = FintechColors.NavyDark.copy(alpha = 0.07f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment     = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Lock, null,
                                            tint = FintechColors.NavyDark, modifier = Modifier.size(16.dp))
                                        Text(
                                            "XXXX  XXXX  ${custAadhaar.takeLast(4)}",
                                            style      = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color      = FintechColors.NavyDark
                                        )
                                        Spacer(Modifier.weight(1f))
                                        Text("Masked for security",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colorScheme.outline)
                                    }
                                }
                            }

                            NavyOutlinedField(
                                value         = custMobile,
                                onValueChange = { if (it.all(Char::isDigit)) custMobile = it },
                                label         = "Customer Mobile *",
                                placeholder   = "10-digit registered mobile",
                                leadingIcon   = Icons.Default.Phone,
                                keyboardType  = KeyboardType.Phone,
                                maxLength     = 10,
                                isError       = custMobileError,
                                errorMessage  = "Enter a valid 10-digit mobile number",
                                trailingIcon  = if (custMobile.length == 10) ({
                                    Icon(Icons.Default.CheckCircle, null,
                                        tint = FintechColors.SuccessGreen)
                                }) else null
                            )
                        }
                    }

                    // ── Transaction details ───
                    SectionCard(
                        title = "Transaction Details",
                        icon  = Icons.Default.AccountBalance
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                            NavyDropdownField(
                                label            = "Transaction Type *",
                                leadingIcon      = Icons.Default.SwapHoriz,
                                selectedValue    = selectedTxnType,
                                options          = txnTypes,
                                onOptionSelected = { selectedTxnType = it }
                            )

                            NavyDropdownField(
                                label            = "Bank Name *",
                                leadingIcon      = Icons.Default.AccountBalance,
                                selectedValue    = selectedBank,
                                options          = banks,
                                onOptionSelected = { selectedBank = it }
                            )

                            // Amount — only for payment/withdrawal
                            AnimatedVisibility(visible = needsAmount) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    NavyOutlinedField(
                                        value         = amount,
                                        onValueChange = { amount = it },
                                        label         = "Amount (₹) *",
                                        placeholder   = "Enter amount",
                                        leadingIcon   = Icons.Default.CurrencyRupee,
                                        keyboardType  = KeyboardType.Decimal,
                                        isError       = amountError,
                                        errorMessage  = "Please enter a valid amount"
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("500", "1000", "2000", "5000").forEach { preset ->
                                            FilterChip(
                                                selected = amount == preset,
                                                onClick  = { amount = preset },
                                                label    = { Text("₹$preset",
                                                    style = MaterialTheme.typography.labelSmall) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── Transaction summary ───
                    if (isTxnFormValid) {
                        AadhaarPaySummaryCard(
                            custAadhaar = custAadhaar,
                            custMobile  = custMobile,
                            bank        = selectedBank,
                            txnType     = selectedTxnType,
                            amount      = amount.ifEmpty { "—" },
                            agentDevice = selectedDeviceObj?.name ?: ""
                        )
                    }

                    // ── Customer fingerprint notice ─
                    Surface(
                        shape    = RoundedCornerShape(12.dp),
                        color    = FintechColors.SuccessGreenLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Fingerprint, null,
                                tint     = FintechColors.SuccessGreen,
                                modifier = Modifier.size(24.dp))
                            Column {
                                Text("Customer Biometric Required",
                                    style      = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = FintechColors.SuccessGreenDark)
                                Text("Customer fingerprint will be captured on the next step",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FintechColors.SuccessGreen)
                            }
                        }
                    }

                    // ── Submit button ─────────
                    NavyPrimaryButton(
                        text    = "Proceed to Payment",
                        onClick = { /* navigate to payment capture */ },
                        enabled = isTxnFormValid,
                        icon    = Icons.Default.Payment
                    )
                }
            }

            // ── Locked state placeholder ──────
            AnimatedVisibility(
                visible = !isVerified && verifyStep != VerificationStep.SCANNING,
                enter   = fadeIn(),
                exit    = fadeOut()
            ) {
                LockedFormPlaceholder()
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}


// ─────────────────────────────────────────────
// STEP INDICATOR  (1 → 2)
// ─────────────────────────────────────────────

@Composable
fun StepIndicator(currentStep: Int) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        listOf(
            1 to "Agent Verification",
            2 to "Transaction Details"
        ).forEachIndexed { index, (step, label) ->
            val isDone   = currentStep > step
            val isActive = currentStep == step

            // Step circle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.weight(1f)
            ) {
                Surface(
                    shape    = CircleShape,
                    color    = when {
                        isDone   -> FintechColors.SuccessGreen
                        isActive -> FintechColors.NavyDark
                        else     -> colorScheme.outline.copy(alpha = 0.25f)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (isDone) {
                            Icon(Icons.Default.Check, null,
                                tint = Color.White, modifier = Modifier.size(18.dp))
                        } else {
                            Text(
                                "$step",
                                color      = if (isActive) Color.White
                                else colorScheme.outline,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 14.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    label,
                    style     = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isActive || isDone) FontWeight.Bold else FontWeight.Normal,
                    color     = when {
                        isDone   -> FintechColors.SuccessGreen
                        isActive -> FintechColors.NavyDark
                        else     -> colorScheme.outline
                    },
                    textAlign = TextAlign.Center
                )
            }

            // Connector line between steps
            if (index < 1) {
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .height(2.dp)
                        .padding(bottom = 20.dp) // align with circle center
                        .background(
                            if (isDone) FintechColors.SuccessGreen
                            else colorScheme.outline.copy(alpha = 0.25f)
                        )
                )
            }
        }
    }
}


// ─────────────────────────────────────────────
// STEP 1 — AGENT VERIFICATION CARD
// ─────────────────────────────────────────────

@Composable
fun Step1AgentVerification(
    agentAadhaar:      String,
    onAadhaarChange:   (String) -> Unit,
    agentAadhaarError: Boolean,
    selectedDevice:    FingerprintDevice?,
    onDeviceClick:     () -> Unit,
    verifyStep:        VerificationStep,
    isFormReady:       Boolean,
    onScanClick:       () -> Unit,
    onVerifyDone:      (Boolean) -> Unit,
    onRetry:           () -> Unit
) {
    SectionCard(title = "Step 1 — Agent Verification", icon = Icons.Default.AdminPanelSettings) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            when (verifyStep) {

                // ── IDLE / entry form ─────────
                VerificationStep.IDLE, VerificationStep.FAILED -> {

                    if (verifyStep == VerificationStep.FAILED) {
                        VerificationFailedBanner(onRetry = onRetry)
                    }

                    NavyOutlinedField(
                        value         = agentAadhaar,
                        onValueChange = onAadhaarChange,
                        label         = "Agent Aadhaar Number *",
                        placeholder   = "Enter your 12-digit Aadhaar",
                        leadingIcon   = Icons.Default.Badge,
                        keyboardType  = KeyboardType.Number,
                        maxLength     = 12,
                        isError       = agentAadhaarError,
                        errorMessage  = "Aadhaar must be exactly 12 digits",
                        trailingIcon  = if (agentAadhaar.length == 12) ({
                            Icon(Icons.Default.CheckCircle, null,
                                tint = FintechColors.SuccessGreen)
                        }) else null
                    )

                    // Device selector
                    selectedDevice?.let { device ->
                        SelectedDeviceCard(device = device, onClick = onDeviceClick)
                    }
                    OutlinedButton(
                        onClick  = onDeviceClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = FintechColors.NavyDark
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, FintechColors.NavyDark.copy(alpha = 0.4f)
                        )
                    ) {
                        Icon(Icons.Default.SwapHoriz, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Change Device", fontWeight = FontWeight.Medium)
                    }

                    // Scan button
                    NavyPrimaryButton(
                        text    = "Scan Agent Fingerprint",
                        onClick = onScanClick,
                        enabled = isFormReady,
                        icon    = Icons.Default.Fingerprint
                    )
                }

                // ── SCANNING animation ────────
                VerificationStep.SCANNING -> {
                    FingerprintScanningAnimation(
                        onScanComplete = onVerifyDone
                    )
                }

                // ── SUCCESS state ─────────────
                VerificationStep.SUCCESS -> {
                    VerificationSuccessBanner(
                        agentAadhaar = agentAadhaar,
                        deviceName   = selectedDevice?.name ?: ""
                    )
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
// FINGERPRINT SCANNING ANIMATION
// Simulates a 2-second scan then calls back
// ─────────────────────────────────────────────

@Composable
fun FingerprintScanningAnimation(
    onScanComplete: (Boolean) -> Unit
) {
    // Simulate scan: 2.5 seconds then succeed
    LaunchedEffect(Unit) {
        delay(2500)
        onScanComplete(true)   // pass false to simulate failure
    }

    val infiniteTransition = rememberInfiniteTransition(label = "fingerprint_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue  = 1.1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(700, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue  = 1.0f,
        animationSpec = infiniteRepeatable(
            animation  = tween(700, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pulsing outer ring
        Box(contentAlignment = Alignment.Center) {
            // Outer glow ring
            Surface(
                shape    = CircleShape,
                color    = FintechColors.NavyDark.copy(alpha = alpha * 0.15f),
                modifier = Modifier
                    .size(110.dp)
                    .scale(scale)
            ) {}
            // Middle ring
            Surface(
                shape    = CircleShape,
                color    = FintechColors.NavyDark.copy(alpha = alpha * 0.2f),
                modifier = Modifier.size(90.dp)
            ) {}
            // Inner icon circle
            Surface(
                shape    = CircleShape,
                color    = FintechColors.NavyDark,
                modifier = Modifier.size(68.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Default.Fingerprint, null,
                        tint     = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Text(
            "Scanning fingerprint...",
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color      = FintechColors.NavyDark
        )
        Text(
            "Keep your finger on the scanner",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )

        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .clip(RoundedCornerShape(4.dp)),
            color        = FintechColors.NavyDark,
            trackColor   = FintechColors.NavyDark.copy(alpha = 0.15f)
        )
    }
}


// ─────────────────────────────────────────────
// VERIFICATION SUCCESS BANNER
// ─────────────────────────────────────────────

@Composable
fun VerificationSuccessBanner(
    agentAadhaar: String,
    deviceName:   String
) {
    Surface(
        shape    = RoundedCornerShape(14.dp),
        color    = FintechColors.SuccessGreenLight,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = FintechColors.SuccessGreen.copy(alpha = 0.4f),
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape    = CircleShape,
                    color    = FintechColors.SuccessGreen,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.CheckCircle, null,
                            tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
                Column {
                    Text(
                        "Agent Verified Successfully",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = FintechColors.SuccessGreenDark
                    )
                    Text(
                        "You can now fill in the transaction details",
                        style = MaterialTheme.typography.labelSmall,
                        color = FintechColors.SuccessGreen
                    )
                }
            }

            HorizontalDivider(color = FintechColors.SuccessGreen.copy(alpha = 0.2f))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Aadhaar", style = MaterialTheme.typography.labelSmall,
                        color = FintechColors.SuccessGreen)
                    Text(
                        "XXXX XXXX ${agentAadhaar.takeLast(4)}",
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color      = FintechColors.SuccessGreenDark
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Device", style = MaterialTheme.typography.labelSmall,
                        color = FintechColors.SuccessGreen)
                    Text(
                        deviceName,
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color      = FintechColors.SuccessGreenDark
                    )
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
// VERIFICATION FAILED BANNER
// ─────────────────────────────────────────────

@Composable
fun VerificationFailedBanner(onRetry: () -> Unit) {
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.ErrorOutline, null,
                tint     = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Fingerprint not recognised",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.error
                )
                Text(
                    "Please try again or use a different finger",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            TextButton(onClick = onRetry) {
                Text("Retry", color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold)
            }
        }
    }
}


// ─────────────────────────────────────────────
// LOCKED FORM PLACEHOLDER
// Shown below Step 1 before verification
// ─────────────────────────────────────────────

@Composable
fun LockedFormPlaceholder() {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        shape    = RoundedCornerShape(16.dp),
        color    = colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier            = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Lock icon with layered rings
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    shape    = CircleShape,
                    color    = colorScheme.outline.copy(alpha = 0.08f),
                    modifier = Modifier.size(80.dp)
                ) {}
                Surface(
                    shape    = CircleShape,
                    color    = colorScheme.outline.copy(alpha = 0.12f),
                    modifier = Modifier.size(60.dp)
                ) {}
                Surface(
                    shape    = CircleShape,
                    color    = colorScheme.outline.copy(alpha = 0.18f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            Icons.Default.Lock, null,
                            tint     = colorScheme.outline,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Text(
                "Step 2 — Transaction Details",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = colorScheme.outline
            )
            Text(
                "Complete agent fingerprint verification above\nto unlock the transaction form",
                style     = MaterialTheme.typography.bodySmall,
                color     = colorScheme.outline.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            // Dummy locked field outlines for visual hint
            repeat(2) {
                Surface(
                    shape    = RoundedCornerShape(12.dp),
                    color    = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .border(
                            width = 1.dp,
                            color = colorScheme.outline.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {}
            }
        }
    }
}


// ─────────────────────────────────────────────
// SUMMARY CARD
// ─────────────────────────────────────────────

@Composable
fun AadhaarPaySummaryCard(
    custAadhaar: String,
    custMobile:  String,
    bank:        String,
    txnType:     String,
    amount:      String,
    agentDevice: String
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(FintechColors.NavyDark, FintechColors.NavyLight)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Summarize, null,
                        tint = FintechColors.NavyDark, modifier = Modifier.size(18.dp))
                    Text("Transaction Summary",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = FintechColors.NavyDark)
                }

                HorizontalDivider(color = FintechColors.NavyDark.copy(alpha = 0.15f))

                listOf(
                    Triple(Icons.Default.CreditCard,    "Customer",  "XXXX XXXX ${custAadhaar.takeLast(4)}"),
                    Triple(Icons.Default.Phone,          "Mobile",    custMobile),
                    Triple(Icons.Default.AccountBalance, "Bank",      bank.split(" ").take(3).joinToString(" ")),
                    Triple(Icons.Default.SwapHoriz,      "Type",      txnType),
                    Triple(Icons.Default.CurrencyRupee,  "Amount",    if (amount == "—") "N/A" else "₹$amount"),
                    Triple(Icons.Default.Fingerprint,    "Device",    agentDevice),
                ).forEach { (icon, label, value) ->
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(icon, null,
                            tint     = FintechColors.NavyDark.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp))
                        Text(label,
                            style    = MaterialTheme.typography.bodySmall,
                            color    = colorScheme.outline,
                            modifier = Modifier.width(72.dp))
                        Text(value,
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color      = colorScheme.onSurface,
                            modifier   = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────

@Preview(name = "Aadhaar Pay – Light", showBackground = true)
@Preview(name = "Aadhaar Pay – Dark",  showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewAadhaarPayScreen() {
    MaterialTheme { AadhaarPayScreen() }
}

@Preview(name = "Scanning Animation", showBackground = true)
@Composable
fun PreviewScanningAnimation() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            FingerprintScanningAnimation(onScanComplete = {})
        }
    }
}

@Preview(name = "Verification Success", showBackground = true)
@Composable
fun PreviewVerificationSuccess() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            VerificationSuccessBanner(agentAadhaar = "123456789012", deviceName = "Mantra MFS100")
        }
    }
}

@Preview(name = "Locked Placeholder", showBackground = true)
@Composable
fun PreviewLockedPlaceholder() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            LockedFormPlaceholder()
        }
    }
}