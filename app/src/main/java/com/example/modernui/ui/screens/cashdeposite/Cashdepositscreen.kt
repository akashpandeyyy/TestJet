package com.example.modernui.ui.screens.cashdeposite

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.modernui.ui.theme.AppColors
import com.example.modernui.ui.theme.FintechColors


// ─────────────────────────────────────────────
// FINGERPRINT DEVICE DATA MODEL
// ─────────────────────────────────────────────

data class FingerprintDevice(
    val id:           String,
    val name:         String,
    val model:        String,
    val manufacturer: String,
    val isConnected:  Boolean,
    val icon:         ImageVector = Icons.Default.Fingerprint
)

 val fingerprintDevicesMock = listOf(
    FingerprintDevice("face_scan", "Face Scan", "Face", "Generic", false),
    FingerprintDevice("mantra_mfs110", "Mantra L1", "MFS110", "Mantra Softech", false),
    FingerprintDevice("mantra_iris", "Mantra IRIS", "MIS100V2", "Mantra Softech", false),
    FingerprintDevice("morpho_l1", "Morpho - L1", "MSO 1300 E3", "IDEMIA (Morpho)", false)
)

//val fingerprintDevicesMock = listOf(
//    FingerprintDevice(
//        id           = "mantra_mfs100",
//        name         = "Mantra MFS100",
//        model        = "MFS100",
//        manufacturer = "Mantra Softech",
//        isConnected  = true
//    ),
//    FingerprintDevice(
//        id           = "mantra_mfs110",
//        name         = "Mantra MFS110",
//        model        = "MFS110",
//        manufacturer = "Mantra Softech",
//        isConnected  = false
//    ),
//    FingerprintDevice(
//        id           = "morpho_mso300",
//        name         = "Morpho MSO 300",
//        model        = "MSO 300",
//        manufacturer = "IDEMIA (Morpho)",
//        isConnected  = false
//    ),
//    FingerprintDevice(
//        id           = "morpho_mso1300",
//        name         = "Morpho MSO 1300",
//        model        = "MSO 1300 E3",
//        manufacturer = "IDEMIA (Morpho)",
//        isConnected  = false
//    ),
//    FingerprintDevice(
//        id           = "startek_fm220u",
//        name         = "Startek FM220U",
//        model        = "FM220U",
//        manufacturer = "Startek",
//        isConnected  = false
//    ),
//    FingerprintDevice(
//        id           = "secugen_hamster",
//        name         = "SecuGen Hamster Pro",
//        model        = "HU20",
//        manufacturer = "SecuGen",
//        isConnected  = false
//    ),
//    FingerprintDevice(
//        id           = "precision_biometric",
//        name         = "Precision PB510",
//        model        = "PB510",
//        manufacturer = "Precision Biometric",
//        isConnected  = false
//    ),
//    FingerprintDevice(
//        id           = "next_biometrics",
//        name         = "Next NB-3010-U",
//        model        = "NB-3010-U",
//        manufacturer = "Next Biometrics",
//        isConnected  = false
//    ),
//)


// ─────────────────────────────────────────────
// CASH DEPOSIT SCREEN
// ─────────────────────────────────────────────

@Composable
fun CashDepositScreen(
    onBackClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    // ── Form state ────────────────────────────
    var aadhaarNumber       by remember { mutableStateOf("") }
    var mobileNumber        by remember { mutableStateOf("") }
    var amount              by remember { mutableStateOf("") }
    var selectedBank        by remember { mutableStateOf("") }
    var selectedDevice      by remember { mutableStateOf(fingerprintDevicesMock[0].id) }
    var showDeviceSheet     by remember { mutableStateOf(false) }

    // ── Validation ────────────────────────────
    val aadhaarError = aadhaarNumber.isNotEmpty() && aadhaarNumber.length != 12
    val mobileError  = mobileNumber.isNotEmpty()  && mobileNumber.length  != 10
    val amountError  = amount.isNotEmpty() && (amount.toDoubleOrNull() ?: 0.0) <= 0.0
    val isFormValid  = aadhaarNumber.length == 12
            && mobileNumber.length  == 10
            && amount.isNotEmpty() && !amountError
            && selectedBank.isNotEmpty()
            && selectedDevice.isNotEmpty()

    val banks = listOf(
        "State Bank of India",  "Punjab National Bank", "Bank of Baroda",
        "Canara Bank",          "HDFC Bank",            "ICICI Bank",
        "Axis Bank",            "Union Bank of India",  "Indian Bank",
        "Bank of India",        "Central Bank of India","Bank of Maharashtra"
    )

    val selectedDeviceObj = fingerprintDevicesMock.find { it.id == selectedDevice }

    // ── Device bottom sheet ───────────────────
    if (showDeviceSheet) {
        DeviceSelectionSheet(
            devices          = fingerprintDevicesMock,
            selectedDeviceId = selectedDevice,
            onDeviceSelected = { device ->
                selectedDevice  = device.id
                showDeviceSheet = false
            },
            onDismiss = { showDeviceSheet = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .systemBarsPadding() // This implements the edge-to-edge padding
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
                text = "Cash Deposit",
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

            // ── Header banner ─────────────────
            NavyHeaderCard(
                icon     = Icons.Default.AccountBalance,
                title    = "Cash Deposit",
                subtitle = "Deposit cash directly to Aadhaar-linked bank account"
            )

            // ── Customer Details ──────────────
            SectionCard(title = "Customer Details", icon = Icons.Default.Person) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    NavyOutlinedField(
                        value         = aadhaarNumber,
                        onValueChange = { if (it.all(Char::isDigit)) aadhaarNumber = it },
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
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Lock, null,
                                    tint = FintechColors.NavyDark, modifier = Modifier.size(16.dp))
                                Text(
                                    "XXXX  XXXX  ${aadhaarNumber.takeLast(4)}",
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
                        value         = mobileNumber,
                        onValueChange = { if (it.all(Char::isDigit)) mobileNumber = it },
                        label         = "Mobile Number *",
                        placeholder   = "10-digit registered mobile",
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

            // ── Bank Details ──────────────────
            SectionCard(title = "Bank Details", icon = Icons.Default.AccountBalance) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    NavyDropdownField(
                        label            = "Bank Name *",
                        leadingIcon      = Icons.Default.AccountBalance,
                        selectedValue    = selectedBank,
                        options          = banks,
                        onOptionSelected = { selectedBank = it }
                    )

                    // Amount input
                    NavyOutlinedField(
                        value         = amount,
                        onValueChange = { amount = it },
                        label         = "Deposit Amount (₹) *",
                        placeholder   = "Enter amount to deposit",
                        leadingIcon   = Icons.Default.CurrencyRupee,
                        keyboardType  = KeyboardType.Decimal,
                        isError       = amountError,
                        errorMessage  = "Please enter a valid amount"
                    )

                    // Quick amount presets
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier              = Modifier.fillMaxWidth()
                    ) {
                        listOf("1000", "2000", "5000", "10000").forEach { preset ->
                            FilterChip(
                                selected = amount == preset,
                                onClick  = { amount = preset },
                                label    = {
                                    Text("₹$preset",
                                        style = MaterialTheme.typography.labelSmall)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Deposit limit info
                    Surface(
                        shape    = RoundedCornerShape(10.dp),
                        color    = colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, null,
                                tint     = colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp))
                            Text(
                                "Min: ₹100  •  Max: ₹49,999 per transaction",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // ── Fingerprint Device ────────────
            SectionCard(title = "Fingerprint Device", icon = Icons.Default.Fingerprint) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // Selected device display
                    selectedDeviceObj?.let { device ->
                        SelectedDeviceCard(
                            device  = device,
                            onClick = { showDeviceSheet = true }
                        )
                    }

                    // Change device button
                    OutlinedButton(
                        onClick  = { showDeviceSheet = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = FintechColors.NavyDark
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, FintechColors.NavyDark.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(Icons.Default.SwapHoriz, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Change Device", fontWeight = FontWeight.Medium)
                    }

                    // Device status indicator
                    DeviceStatusBar(device = selectedDeviceObj)
                }
            }

            // ── Transaction summary ───────────
            if (isFormValid) {
                DepositSummaryCard(
                    aadhaar = aadhaarNumber,
                    mobile  = mobileNumber,
                    bank    = selectedBank,
                    amount  = amount,
                    device  = selectedDeviceObj?.name ?: ""
                )
            }

            // ── Biometric notice ──────────────
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
                        tint = FintechColors.SuccessGreen, modifier = Modifier.size(24.dp))
                    Column {
                        Text(
                            "Biometric Authentication Required",
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color      = FintechColors.SuccessGreenDark
                        )
                        Text(
                            "Customer fingerprint will be captured on the next step",
                            style = MaterialTheme.typography.labelSmall,
                            color = FintechColors.SuccessGreen
                        )
                    }
                }
            }

            // ── Submit ────────────────────────
            NavyPrimaryButton(
                text    = "Proceed to Biometric",
                onClick = { /* navigate to biometric capture */ },
                enabled = isFormValid,
                icon    = Icons.Default.Fingerprint
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}


// ─────────────────────────────────────────────
// SELECTED DEVICE CARD
// ─────────────────────────────────────────────

@Composable
fun SelectedDeviceCard(
    device:  FingerprintDevice,
    onClick: () -> Unit
) {
    Surface(
        shape    = RoundedCornerShape(14.dp),
        color    = FintechColors.NavyDark.copy(alpha = 0.06f),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = FintechColors.NavyDark.copy(alpha = 0.3f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Device icon circle
            Surface(
                shape    = CircleShape,
                color    = FintechColors.NavyDark.copy(alpha = 0.12f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Default.Fingerprint, null,
                        tint     = FintechColors.NavyDark,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    device.name,
                    fontWeight = FontWeight.Bold,
                    color      = FintechColors.NavyDark,
                    style      = MaterialTheme.typography.bodyMedium
                )
                Text(
                    device.manufacturer,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    "Model: ${device.model}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Connection badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (device.isConnected)
                    FintechColors.SuccessGreenLight
                else
                    MaterialTheme.colorScheme.errorContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (device.isConnected) FintechColors.SuccessGreen
                                else MaterialTheme.colorScheme.error
                            )
                    )
                    Text(
                        if (device.isConnected) "Connected" else "Not Connected",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (device.isConnected) FintechColors.SuccessGreenDark
                        else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
// DEVICE STATUS BAR
// ─────────────────────────────────────────────

@Composable
fun DeviceStatusBar(device: FingerprintDevice?) {
    val colorScheme = MaterialTheme.colorScheme

    val steps = listOf(
        "Device detected" to (device != null),
        "Driver loaded"   to (device?.isConnected == true),
        "Ready to scan"   to (device?.isConnected == true)
    )

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        steps.forEach { (label, isDone) ->
            Column(
                modifier            = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape  = CircleShape,
                    color  = if (isDone) FintechColors.SuccessGreen
                    else colorScheme.outline.copy(alpha = 0.25f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = if (isDone) Icons.Default.Check
                            else Icons.Default.Close,
                            contentDescription = null,
                            tint     = if (isDone) Color.White
                            else colorScheme.outline,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    label,
                    style     = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color     = if (isDone) FintechColors.SuccessGreen
                    else colorScheme.outline
                )
            }
        }
    }
}


// ─────────────────────────────────────────────
// DEVICE SELECTION BOTTOM SHEET
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSelectionSheet(
    devices:          List<FingerprintDevice>,
    selectedDeviceId: String,
    onDeviceSelected: (FingerprintDevice) -> Unit,
    onDismiss:        () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.surface,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Sheet header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(FintechColors.NavyDark, FintechColors.NavyLight)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Fingerprint, null,
                        tint = Color.White, modifier = Modifier.size(24.dp))
                    Column {
                        Text(
                            "Select Fingerprint Device",
                            color      = Color.White,
                            fontWeight = FontWeight.Bold,
                            style      = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "${devices.size} devices available",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Device list
            devices.forEach { device ->
                val isSelected = device.id == selectedDeviceId
                DeviceListItem(
                    device     = device,
                    isSelected = isSelected,
                    onClick    = { onDeviceSelected(device) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}


// ─────────────────────────────────────────────
// DEVICE LIST ITEM  (inside bottom sheet)
// ─────────────────────────────────────────────

@Composable
fun DeviceListItem(
    device:     FingerprintDevice,
    isSelected: Boolean,
    onClick:    () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) FintechColors.NavyDark.copy(alpha = 0.06f)
                else Color.Transparent
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon
        Surface(
            shape    = RoundedCornerShape(12.dp),
            color    = if (isSelected)
                FintechColors.NavyDark.copy(alpha = 0.12f)
            else
                colorScheme.surfaceVariant,
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    Icons.Default.Fingerprint, null,
                    tint     = if (isSelected) FintechColors.NavyDark
                    else colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Device info
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    device.name,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color      = if (isSelected) FintechColors.NavyDark else colorScheme.onSurface,
                    style      = MaterialTheme.typography.bodyMedium
                )
                // "Connected" tag
                if (device.isConnected) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = FintechColors.SuccessGreenLight
                    ) {
                        Text(
                            "Live",
                            modifier  = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style     = MaterialTheme.typography.labelSmall,
                            color     = FintechColors.SuccessGreenDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(
                "${device.manufacturer}  •  ${device.model}",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.outline
            )
        }

        // Selected checkmark
        if (isSelected) {
            Surface(
                shape    = CircleShape,
                color    = FintechColors.NavyDark,
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Default.Check, null,
                        tint     = Color.White,
                        modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
// DEPOSIT SUMMARY CARD
// ─────────────────────────────────────────────

@Composable
fun DepositSummaryCard(
    aadhaar: String,
    mobile:  String,
    bank:    String,
    amount:  String,
    device:  String
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    Text(
                        "Transaction Summary",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = FintechColors.NavyDark
                    )
                }

                HorizontalDivider(color = FintechColors.NavyDark.copy(alpha = 0.15f))

                listOf(
                    Triple(Icons.Default.CreditCard,    "Aadhaar",  "XXXX XXXX ${aadhaar.takeLast(4)}"),
                    Triple(Icons.Default.Phone,          "Mobile",   mobile),
                    Triple(Icons.Default.AccountBalance, "Bank",     bank.split(" ").take(3).joinToString(" ")),
                    Triple(Icons.Default.CurrencyRupee,  "Amount",   "₹$amount"),
                    Triple(Icons.Default.Fingerprint,    "Device",   device),
                ).forEach { (icon, label, value) ->
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(icon, null,
                            tint     = FintechColors.NavyDark.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp))
                        Text(
                            label,
                            style    = MaterialTheme.typography.bodySmall,
                            color    = colorScheme.outline,
                            modifier = Modifier.width(70.dp)
                        )
                        Text(
                            value,
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color      = colorScheme.onSurface,
                            modifier   = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────

@Preview(name = "Cash Deposit – Light", showBackground = true)
@Preview(name = "Cash Deposit – Dark",  showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewCashDepositScreen() {
    MaterialTheme { CashDepositScreen() }
}

@Preview(name = "Device Sheet – Light", showBackground = true)
@Composable
fun PreviewDeviceSheet() {
    MaterialTheme {
        DeviceSelectionSheet(
            devices          = fingerprintDevicesMock,
            selectedDeviceId = "mantra_mfs100",
            onDeviceSelected = {},
            onDismiss        = {}
        )
    }
}

@Preview(name = "Selected Device Card", showBackground = true)
@Composable
fun PreviewSelectedDeviceCard() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            SelectedDeviceCard(
                device  = fingerprintDevicesMock[0],
                onClick = {}
            )
        }
    }
}