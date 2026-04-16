package com.example.modernui.ui.screens.aeps

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.modernui.ui.components.*
import com.example.modernui.ui.screens.addharpay.FingerprintScanningAnimation
import com.example.modernui.ui.screens.addharpay.VerificationFailedBanner
import com.example.modernui.ui.screens.cashdeposite.DeviceSelectionSheet
import com.example.modernui.ui.screens.cashdeposite.DeviceStatusBar
import com.example.modernui.ui.screens.cashdeposite.FingerprintDevice
import com.example.modernui.ui.screens.cashdeposite.SelectedDeviceCard
import com.example.modernui.ui.theme.AppColors
import com.example.modernui.ui.theme.FintechColors


// ─────────────────────────────────────────────
// DEVICE LIST — shared between AEPS1 & AEPS2
// ─────────────────────────────────────────────

private val aepsDevices = listOf(
    FingerprintDevice("face_scan",      "Face Scan",    "Face",        "Generic",          false),
    FingerprintDevice("mantra_mfs110",  "Mantra L1",    "MFS110",      "Mantra Softech",   false),
    FingerprintDevice("mantra_iris",    "Mantra IRIS",  "MIS100V2",    "Mantra Softech",   false),
    FingerprintDevice("morpho_l1",      "Morpho - L1",  "MSO 1300 E3", "IDEMIA (Morpho)",  false)
)


// ══════════════════════════════════════════════════════════
//  AEPS 1 SCREEN
// ══════════════════════════════════════════════════════════

@Composable
fun AepsScreen(
    viewModel:   AepsViewModel = hiltViewModel(),
    onBackClick: () -> Unit    = {}
) {
    AepsFormScreen(
        viewModel    = viewModel,
        screenTitle  = "AEPS",
        txnTypes     = listOf("Cash Withdrawal", "Balance Enquiry", "Mini Statement"),
        onBackClick  = onBackClick
    )
}


// ══════════════════════════════════════════════════════════
//  AEPS 2 SCREEN
// ══════════════════════════════════════════════════════════

@Composable
fun Aeps2Screen(
    viewModel:   Aeps2ViewModel = hiltViewModel(),
    onBackClick: () -> Unit     = {}
) {
    AepsFormScreen(
        viewModel    = viewModel,
        screenTitle  = "AEPS 2",
        txnTypes     = listOf("Cash Withdrawal", "Balance Enquiry", "Mini Statement"),
        onBackClick  = onBackClick
    )
}


// ══════════════════════════════════════════════════════════
//  SHARED AEPS FORM SCREEN
//  Used by both AepsScreen and Aeps2Screen.
//  Receives a BaseAepsViewModel so both VMs work identically.
// ══════════════════════════════════════════════════════════

@Composable
fun AepsFormScreen(
    viewModel:    BaseAepsViewModel,
    screenTitle:  String,
    txnTypes:     List<String>,
    onBackClick:  () -> Unit
) {
    val colorScheme    = MaterialTheme.colorScheme
    val uiState        by viewModel.uiState.collectAsState()
    val aadhaarNumber  by viewModel.aadhaarNumber.collectAsState()
    val mobileNumber   by viewModel.mobileNumber.collectAsState()
    val amount         by viewModel.amount.collectAsState()
    val selectedBank   by viewModel.selectedBank.collectAsState()
    val selectedTxnType by viewModel.selectedTxnType.collectAsState()
    val selectedDevice by viewModel.selectedDevice.collectAsState()
    val banks          by viewModel.banks.collectAsState()
    val scanState      by viewModel.scanState.collectAsState()

    var showDeviceSheet by remember { mutableStateOf(false) }
    var showBankSheet   by remember { mutableStateOf(false) }  // ← NEW

    LaunchedEffect(Unit) { viewModel.refreshBanklist() }

    // ── Validation ────────────────────────────
    val aadhaarError = aadhaarNumber.isNotEmpty() && aadhaarNumber.length != 12
    val mobileError  = mobileNumber.isNotEmpty()  && mobileNumber.length  != 10
    val amountVal    = amount.toDoubleOrNull() ?: 0.0
    val amountError  = amount.isNotEmpty() && amountVal < 100
    val needsAmount  = selectedTxnType == "Cash Withdrawal"

    val isFormValid = aadhaarNumber.length == 12
            && mobileNumber.length == 10
            && selectedBank != null
            && selectedTxnType.isNotEmpty()
            && (!needsAmount || (amount.isNotEmpty() && !amountError))

    val selectedDeviceObj = aepsDevices.find { it.id == selectedDevice }

    // ── Device sheet ──────────────────────────
    if (showDeviceSheet) {
        DeviceSelectionSheet(
            devices          = aepsDevices,
            selectedDeviceId = selectedDevice,
            onDeviceSelected = { device ->
                viewModel.onDeviceSelected(device.id)
                showDeviceSheet = false
            },
            onDismiss = { showDeviceSheet = false }
        )
    }

    // ── Bank selection sheet ──────────────────
    if (showBankSheet) {
        BankSelectionSheet(
            banks          = banks,
            selectedBank   = selectedBank,
            onBankSelected = { bank ->
                viewModel.onBankSelected(bank)
                showBankSheet = false
            },
            onDismiss = { showBankSheet = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .systemBarsPadding()
    ) {

        // ── Top bar ───────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .background(AppColors.NavyAlpha)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Text(screenTitle,
                color      = Color.White,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(start = 8.dp))
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {}) {
                Icon(Icons.Default.Notifications, "Notifications", tint = Color.White)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.MoreVert, "More", tint = Color.White)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── UI state feedback ─────────────
            when (val state = uiState) {
                is AepsUiState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                is AepsUiState.Error   -> Surface(
                    shape    = RoundedCornerShape(10.dp),
                    color    = colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, null,
                            tint = colorScheme.error, modifier = Modifier.size(18.dp))
                        Text(state.message,
                            color = colorScheme.error,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                is AepsUiState.Success -> Surface(
                    shape    = RoundedCornerShape(10.dp),
                    color    = FintechColors.SuccessGreenLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null,
                            tint = FintechColors.SuccessGreen, modifier = Modifier.size(18.dp))
                        Column {
                            Text(state.message,
                                color      = FintechColors.SuccessGreenDark,
                                fontWeight = FontWeight.Bold,
                                style      = MaterialTheme.typography.bodySmall)
                            if (state.txnId.isNotEmpty()) {
                                Text("Txn ID: ${state.txnId}",
                                    color = FintechColors.SuccessGreen,
                                    style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                else -> {}
            }

            // ── Header ────────────────────────
            NavyHeaderCard(
                icon     = Icons.Default.Fingerprint,
                title    = "Aadhaar Enabled Payment",
                subtitle = "$screenTitle — Biometric authenticated banking"
            )

            // ── Biometric scanning overlay ────
            AnimatedVisibility(
                visible = scanState == VerificationStep.SCANNING || scanState == VerificationStep.ERROR,
                enter   = fadeIn() + expandVertically(),
                exit    = fadeOut() + shrinkVertically()
            ) {
                Box(
                    modifier         = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (scanState == VerificationStep.SCANNING) {
                        FingerprintScanningAnimation(
                            onScanComplete = { success ->
                                if (success) {
                                    viewModel.onScanComplete("<PID_DATA_XML_HERE>")
                                } else {
                                    viewModel.setScanState(VerificationStep.ERROR)
                                }
                            }
                        )
                    } else if (scanState == VerificationStep.ERROR) {
                        VerificationFailedBanner(
                            onRetry = { viewModel.setScanState(VerificationStep.SCANNING) }
                        )
                    }
                }
            }

            // ── Customer details ──────────────
            SectionCard(title = "Customer Details", icon = Icons.Default.Person) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    NavyOutlinedField(
                        value         = aadhaarNumber,
                        onValueChange = { viewModel.onAadhaarChange(it) },
                        label         = "Aadhaar Number *",
                        placeholder   = "Enter 12-digit Aadhaar",
                        leadingIcon   = Icons.Default.CreditCard,
                        keyboardType  = KeyboardType.Number,
                        maxLength     = 12,
                        isError       = aadhaarError,
                        errorMessage  = "Aadhaar must be exactly 12 digits",
                        trailingIcon  = if (aadhaarNumber.length == 12) ({
                            Icon(Icons.Default.CheckCircle, null,
                                tint = FintechColors.SuccessGreen)
                        }) else null
                    )
                    NavyOutlinedField(
                        value         = mobileNumber,
                        onValueChange = { viewModel.onMobileChange(it) },
                        label         = "Mobile Number *",
                        placeholder   = "10-digit mobile number",
                        leadingIcon   = Icons.Default.Phone,
                        keyboardType  = KeyboardType.Phone,
                        maxLength     = 10,
                        isError       = mobileError,
                        errorMessage  = "Enter a valid 10-digit mobile number",
                        trailingIcon  = if (mobileNumber.length == 10) ({
                            Icon(Icons.Default.CheckCircle, null,
                                tint = FintechColors.SuccessGreen)
                        }) else null
                    )
                }
            }

            // ── Transaction details ───────────
            SectionCard(title = "Transaction Details", icon = Icons.Default.AccountBalance) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    NavyDropdownField(
                        label            = "Transaction Type *",
                        leadingIcon      = Icons.Default.SwapHoriz,
                        selectedValue    = selectedTxnType,
                        options          = txnTypes,
                        onOptionSelected = { viewModel.onTxnTypeSelected(it) }
                    )

                    // ── Bank selector — opens BankSelectionSheet ──
                    if (selectedBank == null) {
                        OutlinedButton(
                            onClick  = { showBankSheet = true },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.outlinedButtonColors(
                                contentColor = FintechColors.NavyDark
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, FintechColors.NavyDark.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(Icons.Default.AccountBalance, null,
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Select Bank *", fontWeight = FontWeight.Medium)
                        }
                    } else {
                        // ── Selected bank row with Change button ──
                        SelectedBankRow(
                            bank    = selectedBank!!,
                            onClick = { showBankSheet = true }
                        )
                    }

                    // ── Amount field ──────────────────────────────
                    if (needsAmount) {
                        NavyOutlinedField(
                            value         = amount,
                            onValueChange = { viewModel.onAmountChange(it) },
                            label         = "Amount (₹) *",
                            placeholder   = "Minimum ₹100",
                            leadingIcon   = Icons.Default.CurrencyRupee,
                            keyboardType  = KeyboardType.Decimal,
                            isError       = amountError,
                            errorMessage  = "Minimum amount is ₹100"
                        )
                        // Quick amount chips
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("500", "1000", "2000", "5000").forEach { preset ->
                                FilterChip(
                                    selected = amount == preset,
                                    onClick  = { viewModel.onAmountChange(preset) },
                                    label    = {
                                        Text("₹$preset",
                                            style = MaterialTheme.typography.labelSmall)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // ── Device section ────────────────
            SectionCard(title = "Biometric Device", icon = Icons.Default.Fingerprint) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    selectedDeviceObj?.let { device ->
                        SelectedDeviceCard(
                            device  = device,
                            onClick = { showDeviceSheet = true }
                        )
                    }
                    OutlinedButton(
                        onClick  = { showDeviceSheet = true },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = FintechColors.NavyDark),
                        border   = androidx.compose.foundation.BorderStroke(
                            1.dp, FintechColors.NavyDark.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Default.SwapHoriz, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Change Device", fontWeight = FontWeight.Medium)
                    }
                    DeviceStatusBar(device = selectedDeviceObj)
                }
            }

            // ── Submit button ─────────────────
            Button(
                onClick  = {
                    if (selectedDevice == "face_scan") viewModel.cusTwoFA()
                    else viewModel.setScanState(VerificationStep.SCANNING)
                },
                enabled  = isFormValid
                        && uiState !is AepsUiState.Loading
                        && scanState != VerificationStep.SCANNING,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = FintechColors.NavyDark,
                    contentColor           = Color.White,
                    disabledContainerColor = FintechColors.NavyDark.copy(alpha = 0.35f),
                    disabledContentColor   = Color.White.copy(alpha = 0.5f)
                )
            ) {
                if (uiState is AepsUiState.Loading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(22.dp),
                        color       = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        when (selectedTxnType) {
                            "Balance Enquiry" -> "CHECK BALANCE"
                            "Mini Statement"  -> "GET STATEMENT"
                            else              -> "PROCEED TRANSACTION"
                        },
                        fontWeight   = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
