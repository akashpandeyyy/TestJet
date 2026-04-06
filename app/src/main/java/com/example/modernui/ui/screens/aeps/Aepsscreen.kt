package com.example.modernui.ui.screens.aeps

import android.content.res.Configuration
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.modernui.ui.components.*
import com.example.modernui.ui.screens.cashdeposite.DeviceSelectionSheet
import com.example.modernui.ui.screens.cashdeposite.DeviceStatusBar
import com.example.modernui.ui.screens.cashdeposite.FingerprintDevice
import com.example.modernui.ui.screens.cashdeposite.SelectedDeviceCard
import com.example.modernui.ui.theme.AppColors
import com.example.modernui.ui.theme.FintechColors

// --- DEVICE DATA ---
private val fingerprintDevices = listOf(
    FingerprintDevice("face_scan", "Face Scan", "Face", "Generic", false),
    FingerprintDevice("mantra_mfs110", "Mantra L1", "MFS110", "Mantra Softech", false),
    FingerprintDevice("mantra_iris", "Mantra IRIS", "MIS100V2", "Mantra Softech", false),
    FingerprintDevice("morpho_l1", "Morpho - L1", "MSO 1300 E3", "IDEMIA (Morpho)", false)
)

@Composable
fun AepsScreen(
    viewModel: AepsViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()

    val aadhaarNumber by viewModel.aadhaarNumber.collectAsState()
    val mobileNumber by viewModel.mobileNumber.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val selectedBank by viewModel.selectedBank.collectAsState()
    val selectedTxnType by viewModel.selectedTxnType.collectAsState()
    val selectedDevice by viewModel.selectedDevice.collectAsState()

    var showDeviceSheet by remember { mutableStateOf(false) }

    // ── Validation ────────────────────────────
    val aadhaarError = aadhaarNumber.isNotEmpty() && aadhaarNumber.length != 12
    val mobileError  = mobileNumber.isNotEmpty()  && mobileNumber.length  != 10
    val amountError  = amount.isNotEmpty() && (amount.toDoubleOrNull() ?: 0.0) <= 0.0
    val isFormValid  = aadhaarNumber.length == 12
            && mobileNumber.length == 10
            && selectedBank.isNotEmpty()
            && selectedTxnType.isNotEmpty()
            && (selectedTxnType == "Balance Enquiry" || selectedTxnType == "Mini Statement"
            || (amount.isNotEmpty() && !amountError))

    val banks = listOf(
        "State Bank of India", "Punjab National Bank", "Bank of Baroda",
        "Canara Bank", "HDFC Bank", "ICICI Bank", "Axis Bank",
        "Union Bank of India", "Indian Bank", "Bank of India"
    )
    val txnTypes = listOf(
        "Cash Withdrawal", "Balance Enquiry", "Mini Statement", "Cash Deposit"
    )
    val needsAmount = selectedTxnType == "Cash Withdrawal" || selectedTxnType == "Cash Deposit"

    val selectedDeviceObj = fingerprintDevices.find { it.id == selectedDevice }

    if (showDeviceSheet) {
        DeviceSelectionSheet(
            devices          = fingerprintDevices,
            selectedDeviceId = selectedDevice,
            onDeviceSelected = { device ->
                viewModel.onDeviceSelected(device.id)
                showDeviceSheet = false
            },
            onDismiss = { showDeviceSheet = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .systemBarsPadding()
    ) {
        // ── Header ────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .background(AppColors.NavyAlpha)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "AEPS",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
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

            // Handle UI State
            when (val state = uiState) {
                is AepsUiState.Loading -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                is AepsUiState.Error -> {
                    Text(state.message, color = colorScheme.error, modifier = Modifier.padding(8.dp))
                }
                is AepsUiState.Success -> {
                    // Handle success UI or side effects
                }
                else -> {}
            }

            // ── Header banner ─────────────────
            NavyHeaderCard(
                icon     = Icons.Default.Fingerprint,
                title    = "Aadhaar Enabled Payment",
                subtitle = "Biometric authenticated banking"
            )

            // ── Customer Details ──────────────
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

                    // Masked Aadhaar preview
                    if (aadhaarNumber.length == 12) {
                        Surface(
                            shape    = RoundedCornerShape(8.dp),
                            color    = FintechColors.NavyDark.copy(alpha = 0.07f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Lock, null,
                                    tint     = FintechColors.NavyDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "XXXX XXXX ${aadhaarNumber.takeLast(4)}",
                                    style      = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = FintechColors.NavyDark
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    "Masked for security",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.outline
                                )
                            }
                        }
                    }

                    NavyOutlinedField(
                        value         = mobileNumber,
                        onValueChange = { viewModel.onMobileChange(it) },
                        label         = "Mobile Number *",
                        placeholder   = "10-digit mobile number",
                        leadingIcon   = Icons.Default.Phone,
                        keyboardType  = KeyboardType.Phone,
                        maxLength     = 10,
                        isError       = mobileError,
                        errorMessage  = "Enter a valid 10-digit mobile number"
                    )
                }
            }

            // ── Transaction Details ───────────
            SectionCard(title = "Transaction Details", icon = Icons.Default.AccountBalance) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    NavyDropdownField(
                        label            = "Transaction Type *",
                        leadingIcon      = Icons.Default.SwapHoriz,
                        selectedValue    = selectedTxnType,
                        options          = txnTypes,
                        onOptionSelected = { viewModel.onTxnTypeSelected(it) }
                    )

                    NavyDropdownField(
                        label            = "Bank Name *",
                        leadingIcon      = Icons.Default.AccountBalance,
                        selectedValue    = selectedBank,
                        options          = banks,
                        onOptionSelected = { viewModel.onBankSelected(it) }
                    )

                    // Amount — only for Withdrawal / Deposit
                    if (needsAmount) {
                        NavyOutlinedField(
                            value         = amount,
                            onValueChange = { viewModel.onAmountChange(it) },
                            label         = "Amount (₹) *",
                            placeholder   = "Enter amount",
                            leadingIcon   = Icons.Default.CurrencyRupee,
                            keyboardType  = KeyboardType.Decimal,
                            isError       = amountError,
                            errorMessage  = "Please enter a valid amount"
                        )

                        // Quick amount chips
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("500", "1000", "2000", "5000").forEach { preset ->
                                FilterChip(
                                    selected = amount == preset,
                                    onClick  = { viewModel.onAmountChange(preset) },
                                    label    = {
                                        Text(
                                            "₹$preset",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ── Fingerprint Device ────────────
            SectionCard(title = "Fingerprint Device", icon = Icons.Default.Fingerprint) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    selectedDeviceObj?.let { device ->
                        SelectedDeviceCard(
                            device = device,
                            onClick = { showDeviceSheet = true }
                        )
                    }

                    DeviceStatusBar(device = selectedDeviceObj)
                }
            }

            // ── Submit Button ─────────────────
            Button(
                onClick = { viewModel.performTransaction() },
                enabled = isFormValid && uiState !is AepsUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FintechColors.NavyDark,
                    contentColor = Color.White
                )
            ) {
                if (uiState is AepsUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        if (selectedTxnType == "Balance Enquiry") "CHECK BALANCE" else "PROCEED TRANSACTION",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAepsScreen() {
    MaterialTheme {
        AepsScreen(onBackClick = {})
    }
}
