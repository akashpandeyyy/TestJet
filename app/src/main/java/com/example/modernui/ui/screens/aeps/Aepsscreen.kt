package com.example.modernui.ui.screens.aeps

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.modernui.ui.screens.aeps.AepsModelResponse
import com.example.modernui.ui.screens.aeps.Data
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.modernui.ui.components.*
import com.example.modernui.ui.components.intentpackage.RdHelper
import com.example.modernui.ui.screens.addharpay.FingerprintScanningAnimation
import com.example.modernui.ui.screens.addharpay.VerificationFailedBanner
import com.example.modernui.ui.screens.cashdeposite.DeviceSelectionSheet
import com.example.modernui.ui.screens.cashdeposite.DeviceStatusBar
import com.example.modernui.ui.screens.cashdeposite.FingerprintDevice
import com.example.modernui.ui.screens.cashdeposite.SelectedDeviceCard
import com.example.modernui.ui.theme.AppColors
import com.example.modernui.ui.theme.FintechColors
import kotlinx.coroutines.flow.collectLatest

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
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()

    val aadhaarNumber by viewModel.aadhaarNumber.collectAsState()
    val mobileNumber by viewModel.mobileNumber.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val selectedBank by viewModel.selectedBank.collectAsState()
    val selectedTxnType by viewModel.selectedTxnType.collectAsState()
    val selectedDevice by viewModel.selectedDevice.collectAsState()
    val selectedDeviceObj = fingerprintDevices.find { it.id == selectedDevice }
    val banks by viewModel.banks.collectAsState()
    val scanState by viewModel.scanState.collectAsState()

    val rdCaptureState by viewModel.rdCaptureState.collectAsState()

    var showDeviceSheet by remember { mutableStateOf(false) }
    var showBankSheet by remember { mutableStateOf(false) }

    val faceCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val dataIntent  = result.data
        val outputKey   = RdHelper.getOutputKey(selectedDevice)
        val responseData = dataIntent?.getStringExtra(outputKey) ?: ""

        Log.d("AepsScreen", "Capture: Code=${result.resultCode}, Key=$outputKey, HasData=${responseData.isNotEmpty()}")

        if (result.resultCode == Activity.RESULT_OK) {
            if (responseData.isNotEmpty()) {
                viewModel.handleRdServiceResult(responseData)
            } else {
                Toast.makeText(context, "Device returned empty data ($outputKey)", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
        } else {
            viewModel.resetState()
            Toast.makeText(context, "Verification Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(rdCaptureState) {
        if (rdCaptureState is RdCaptureState.Capture) {
            val state = rdCaptureState as RdCaptureState.Capture
            try {
                val intent = android.content.Intent(state.action).apply {
                    setPackage(state.packageName)
                    putExtra(state.inputKey, state.pidOptions)
                }
                faceCaptureLauncher.launch(intent)
                viewModel.resetRdCaptureState()
            } catch (e: Exception) {
                Toast.makeText(context, "RD Service not found: ${state.packageName}", Toast.LENGTH_SHORT).show()
                viewModel.resetRdCaptureState()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshBanklist()
    }

    // ── Validation ────────────────────────────
    val aadhaarError = aadhaarNumber.isNotEmpty() && aadhaarNumber.length != 12
    val mobileError  = mobileNumber.isNotEmpty()  && mobileNumber.length  != 10
    val amountError  = amount.isNotEmpty() && (amount.toDoubleOrNull() ?: 0.0) <= 0.0
    val isFormValid  = aadhaarNumber.length == 12
            && mobileNumber.length == 10
            && selectedBank != null
            && selectedTxnType.isNotEmpty()
            && (selectedTxnType == "Balance Enquiry" || selectedTxnType == "Mini Statement"
            || (amount.isNotEmpty() && !amountError))

    val txnTypes = listOf("Cash Withdrawal", "Balance Enquiry", "Mini Statement")
    val needsAmount = selectedTxnType == "Cash Withdrawal"

    // ── Dialogs & Sheets ───────────────────
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

    if (showBankSheet) {
        BankSelectionSheet(
            banks          = banks,
            selectedBank   = selectedBank,
            onBankSelected = { viewModel.onBankSelected(it) },
            onDismiss      = { showBankSheet = false }
        )
    }

    // --- Receipt Dialog ---
    val receiptState = uiState
    if (receiptState is AepsUiState.Success || (receiptState is AepsUiState.Error && receiptState.response != null)) {
        val response = if (receiptState is AepsUiState.Success) receiptState.response else (receiptState as AepsUiState.Error).response!!
        
        Dialog(
            onDismissRequest = { viewModel.resetState() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface
            ) {
                AepsReceiptContent(
                    response = response,
                    onClose = { viewModel.resetState() }
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .systemBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .background(AppColors.NavyAlpha)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
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
                is AepsUiState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                is AepsUiState.Error -> Text(state.message, color = colorScheme.error, modifier = Modifier.padding(8.dp))
                else -> {}
            }

            NavyHeaderCard(
                icon     = Icons.Default.Fingerprint,
                title    = "Aadhaar Enabled Payment",
                subtitle = "Biometric authenticated banking"
            )

            // Biometric Animation Overlay
            AnimatedVisibility(
                visible = scanState == VerificationStep.SCANNING || scanState == VerificationStep.ERROR,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
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
                        VerificationFailedBanner(onRetry = { viewModel.setScanState(VerificationStep.SCANNING) })
                    }
                }
            }

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
                        errorMessage  = "Aadhaar must be exactly 12 digits"
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
                        errorMessage  = "Enter a valid 10-digit mobile number"
                    )
                }
            }

            SectionCard(title = "Transaction Details", icon = Icons.Default.AccountBalance) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    NavyDropdownField(
                        label            = "Transaction Type *",
                        leadingIcon      = Icons.Default.SwapHoriz,
                        selectedValue    = selectedTxnType,
                        options          = txnTypes,
                        onOptionSelected = { viewModel.onTxnTypeSelected(it) }
                    )

                    if (selectedBank != null) {
                        SelectedBankRow(
                            bank    = selectedBank!!,
                            onClick = { showBankSheet = true }
                        )
                    } else {
                        OutlinedCard(
                            onClick = { showBankSheet = true },
                            shape   = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            border  = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.AccountBalance, null,
                                    tint     = FintechColors.NavyDark,
                                    modifier = Modifier.size(20.dp))
                                Text("Select Bank *",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge)
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, null,
                                    tint = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }

                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    val amountError = amount.isNotEmpty() && amountValue < 100
                    if (needsAmount) {
                        NavyOutlinedField(
                            value         = amount,
                            onValueChange = { viewModel.onAmountChange(it) },
                            label         = "Amount (₹) *",
                            placeholder   = "Enter amount",
                            leadingIcon   = Icons.Default.CurrencyRupee,
                            keyboardType  = KeyboardType.Decimal,
                            isError       = amountError,
                            errorMessage  = "Minimum amount is ₹100"
                        )
                    }
                }
            }

            SectionCard(title = "Fingerprint Device", icon = Icons.Default.Fingerprint) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    selectedDeviceObj?.let { device ->
                        SelectedDeviceCard(device = device, onClick = { showDeviceSheet = true })
                    }
                    DeviceStatusBar(device = selectedDeviceObj)
                }
            }

            Button(
                onClick = { viewModel.cusTwoFA() },
                enabled = isFormValid && uiState !is AepsUiState.Loading && scanState != VerificationStep.SCANNING,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FintechColors.NavyDark)
            ) {
                if (uiState is AepsUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        if (selectedTxnType == "Balance Enquiry") "CHECK BALANCE" else "PROCEED TRANSACTION",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun AepsReceiptContent(
    response: AepsModelResponse,
    onClose: () -> Unit
) {
    val data = response.data
    val isSuccess = response.status == 1 && data?.status == "SUCCESS"
    val statusColor = if (isSuccess) Color(0xFF2E7D32) else Color(0xFFD32F2F)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(rememberScrollState())
    ) {
        // Receipt Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(FintechColors.NavyDark)
                .padding(vertical = 24.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFFF5252),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (isSuccess) "Transaction Successful" else "Transaction Failed",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = data?.date ?: "",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Amount Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Transaction Amount", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                Text(
                    "₹ ${data?.txnAmount ?: "0.00"}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = FintechColors.NavyDark
                )
                if (!isSuccess) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = data?.errormessage ?: response.message ?: "Unknown Error",
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Details Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ReceiptRow("Transaction Type", data?.txnType ?: "N/A")
                ReceiptRow("Aadhaar Number", data?.aadhaar ?: "N/A")
                ReceiptRow("RRN", data?.rrn ?: "N/A")
                ReceiptRow("STAN", data?.stan ?: "N/A")
                ReceiptRow("Remaining Balance", "₹ ${data?.remainingBal ?: "0.00"}")
                ReceiptRow("Bank IIN", data?.iin ?: "N/A")
                ReceiptRow("Terminal ID", data?.terminalid ?: "N/A")
                ReceiptRow("Status", data?.status ?: "FAILED", valueColor = statusColor)
            }
        }

        Spacer(Modifier.weight(1f))

        // Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val context = LocalContext.current
            OutlinedButton(
                onClick = {
                    val shareText = """
                        --- Transaction Receipt ---
                        Status: ${if (isSuccess) "SUCCESS" else "FAILED"}
                        Type: ${data?.txnType ?: "N/A"}
                        Amount: ₹ ${data?.txnAmount ?: "0.00"}
                        Aadhaar: ${data?.aadhaar ?: "N/A"}
                        RRN: ${data?.rrn ?: "N/A"}
                        Date: ${data?.date ?: ""}
                        ---------------------------
                    """.trimIndent()

                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }

                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Share, null)
                Spacer(Modifier.width(8.dp))
                Text("Share")
            }
            Button(
                onClick = onClose,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FintechColors.NavyDark)
            ) {
                Text("Done")
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            color = valueColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
