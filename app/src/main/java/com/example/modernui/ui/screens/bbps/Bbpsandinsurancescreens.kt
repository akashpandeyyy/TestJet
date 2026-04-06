package com.example.modernui.ui.screens.bbps


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
import com.example.modernui.ui.theme.FintechColors
import kotlinx.coroutines.delay


// ══════════════════════════════════════════════════════════
//  BBPS SCREEN
// ══════════════════════════════════════════════════════════

// ─────────────────────────────────────────────
// BBPS CATEGORY MODEL
// ─────────────────────────────────────────────

enum class BbpsCategory(
    val label:    String,
    val icon:     ImageVector,
    val color:    Color,
    val desc:     String
) {
    EMI(
        label = "EMI Payment",
        icon  = Icons.Default.CreditScore,
        color = Color(0xFF1565C0),
        desc  = "Pay loan EMIs — home, personal, vehicle"
    ),
    ELECTRICITY(
        label = "Electricity",
        icon  = Icons.Default.Bolt,
        color = Color(0xFFF57C00),
        desc  = "Pay electricity bills for any DISCOM"
    ),
    LPG(
        label = "LPG",
        icon  = Icons.Default.LocalFireDepartment,
        color = Color(0xFF2E7D32),
        desc  = "Book or pay for LPG cylinder"
    ),
    FASTAG(
        label = "FASTag",
        icon  = Icons.Default.DirectionsCar,
        color = Color(0xFF6A1B9A),
        desc  = "Recharge FASTag for toll payments"
    )
}

// ─────────────────────────────────────────────
// BBPS SCREEN STEPS
// ─────────────────────────────────────────────

enum class BbpsStep {
    CATEGORY,   // pick one of the 4 categories
    DETAILS,    // fill category-specific fields
    CONFIRM,    // review + pay
    PROCESSING,
    RESULT
}

// ─────────────────────────────────────────────
// BBPS OPERATORS per category
// ─────────────────────────────────────────────

private val emiLenders = listOf(
    "HDFC Bank", "ICICI Bank", "SBI Home Loans", "Bajaj Finserv",
    "Axis Bank", "Kotak Mahindra", "Tata Capital", "L&T Finance"
)
private val electricityBoards = listOf(
    "BESCOM (Karnataka)", "MSEDCL (Maharashtra)", "BSES Rajdhani (Delhi)",
    "TNEB (Tamil Nadu)", "UPPCL (Uttar Pradesh)", "WBSEDCL (West Bengal)",
    "PSPCL (Punjab)", "CESC (Kolkata)", "JDVVNL (Rajasthan)", "KSEBL (Kerala)"
)
private val lpgAgencies = listOf(
    "HP Gas", "Indane Gas", "Bharat Gas"
)
private val fastagBanks = listOf(
    "NHAI FASTag", "HDFC FASTag", "ICICI FASTag", "SBI FASTag",
    "Axis Bank FASTag", "Paytm Payments Bank", "Kotak FASTag", "Yes Bank FASTag"
)


@Composable
fun BbpsScreen(
    onBackClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    var step            by remember { mutableStateOf(BbpsStep.CATEGORY) }
    var selectedCategory by remember { mutableStateOf<BbpsCategory?>(null) }

    // Shared form fields
    var selectedOperator by remember { mutableStateOf("") }
    var consumerId       by remember { mutableStateOf("") }
    var mobileNumber     by remember { mutableStateOf("") }
    var amount           by remember { mutableStateOf("") }
    var billFetched      by remember { mutableStateOf(false) }
    var fetchedBillAmt   by remember { mutableStateOf("") }

    val txnRef = remember { "BBPS${System.currentTimeMillis().toString().takeLast(9)}" }
    var isSuccess by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        // ── Top bar ───────────────────────────
        DetailTopBar(
            title       = when (step) {
                BbpsStep.CATEGORY   -> "BBPS Bill Payment"
                BbpsStep.DETAILS    -> selectedCategory?.label ?: "Bill Details"
                BbpsStep.CONFIRM    -> "Confirm Payment"
                BbpsStep.PROCESSING -> "Processing..."
                BbpsStep.RESULT     -> if (isSuccess) "Payment Successful" else "Payment Failed"
            },
            onBackClick = {
                when (step) {
                    BbpsStep.DETAILS    -> { step = BbpsStep.CATEGORY; billFetched = false }
                    BbpsStep.CONFIRM    -> step = BbpsStep.DETAILS
                    else                -> onBackClick()
                }
            }
        )

        AnimatedContent(
            targetState    = step,
            transitionSpec = {
                (fadeIn(tween(220)) + slideInHorizontally { it / 6 })
                    .togetherWith(fadeOut(tween(180)) + slideOutHorizontally { -it / 6 })
            },
            label = "bbps_step"
        ) { currentStep ->
            when (currentStep) {

                // ── STEP 1: Pick category ─────
                BbpsStep.CATEGORY -> BbpsCategoryStep(
                    selectedCategory = selectedCategory,
                    onCategorySelect = { selectedCategory = it },
                    onNext           = {
                        selectedOperator = ""
                        consumerId       = ""
                        mobileNumber     = ""
                        amount           = ""
                        billFetched      = false
                        step             = BbpsStep.DETAILS
                    }
                )

                // ── STEP 2: Category-specific form ─
                BbpsStep.DETAILS -> BbpsDetailsStep(
                    category         = selectedCategory!!,
                    selectedOperator = selectedOperator,
                    onOperatorChange = { selectedOperator = it },
                    consumerId       = consumerId,
                    onConsumerChange = { consumerId = it },
                    mobileNumber     = mobileNumber,
                    onMobileChange   = { if (it.all(Char::isDigit)) mobileNumber = it },
                    amount           = amount,
                    onAmountChange   = { amount = it },
                    billFetched      = billFetched,
                    fetchedBillAmt   = fetchedBillAmt,
                    onFetchBill      = {
                        // Simulate fetch
                        fetchedBillAmt = "1,249.00"
                        amount         = "1249"
                        billFetched    = true
                    },
                    onProceed        = { step = BbpsStep.CONFIRM }
                )

                // ── STEP 3: Confirm ───────────
                BbpsStep.CONFIRM -> BbpsConfirmStep(
                    category   = selectedCategory!!,
                    operator   = selectedOperator,
                    consumerId = consumerId,
                    amount     = amount,
                    txnRef     = txnRef,
                    onPay      = { step = BbpsStep.PROCESSING },
                    onEdit     = { step = BbpsStep.DETAILS }
                )

                // ── STEP 4: Processing ────────
                BbpsStep.PROCESSING -> {
                    LaunchedEffect(Unit) {
                        delay(2000)
                        isSuccess = true
                        step      = BbpsStep.RESULT
                    }
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            CircularProgressIndicator(
                                color       = FintechColors.NavyDark,
                                modifier    = Modifier.size(56.dp),
                                strokeWidth = 4.dp
                            )
                            Text("Processing Payment...",
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color      = FintechColors.NavyDark)
                            Text("Please do not press back",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.outline)
                        }
                    }
                }

                // ── STEP 5: Result ────────────
                BbpsStep.RESULT -> BbpsResultStep(
                    isSuccess  = isSuccess,
                    category   = selectedCategory!!,
                    amount     = amount,
                    txnRef     = txnRef,
                    onDone     = {
                        step             = BbpsStep.CATEGORY
                        selectedCategory = null
                        billFetched      = false
                        amount           = ""
                        consumerId       = ""
                        selectedOperator = ""
                    }
                )
            }
        }
    }
}


// ─────────────────────────────────────────────
// STEP 1 — CATEGORY SELECTOR
// ─────────────────────────────────────────────

@Composable
fun BbpsCategoryStep(
    selectedCategory: BbpsCategory?,
    onCategorySelect: (BbpsCategory) -> Unit,
    onNext:           () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NavyHeaderCard(
            icon     = Icons.Default.Receipt,
            title    = "Bharat Bill Payment System",
            subtitle = "Pay bills across multiple categories instantly"
        )

        Text("Select Bill Category",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onBackground)

        // 4 category radio cards
        BbpsCategory.entries.forEach { category ->
            BbpsCategoryCard(
                category   = category,
                isSelected = selectedCategory == category,
                onClick    = { onCategorySelect(category) }
            )
        }

        NavyPrimaryButton(
            text    = "Continue",
            onClick = onNext,
            enabled = selectedCategory != null,
            icon    = Icons.Default.ArrowForward
        )

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun BbpsCategoryCard(
    category:   BbpsCategory,
    isSelected: Boolean,
    onClick:    () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        shape    = RoundedCornerShape(16.dp),
        color    = if (isSelected) category.color.copy(alpha = 0.08f)
        else colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 0.5.dp,
                color = if (isSelected) category.color
                else colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Radio button
            RadioButton(
                selected = isSelected,
                onClick  = onClick,
                colors   = RadioButtonDefaults.colors(
                    selectedColor   = category.color,
                    unselectedColor = colorScheme.outline
                )
            )

            // Icon circle
            Surface(
                shape    = RoundedCornerShape(12.dp),
                color    = category.color.copy(alpha = if (isSelected) 0.15f else 0.08f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(category.icon, null,
                        tint     = category.color,
                        modifier = Modifier.size(26.dp))
                }
            }

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(category.label,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color      = if (isSelected) category.color else colorScheme.onSurface)
                Text(category.desc,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.outline)
            }

            // Check icon when selected
            AnimatedVisibility(visible = isSelected) {
                Icon(Icons.Default.CheckCircle, null,
                    tint     = category.color,
                    modifier = Modifier.size(20.dp))
            }
        }
    }
}


// ─────────────────────────────────────────────
// STEP 2 — DETAILS (category-specific form)
// ─────────────────────────────────────────────

@Composable
fun BbpsDetailsStep(
    category:         BbpsCategory,
    selectedOperator: String,
    onOperatorChange: (String) -> Unit,
    consumerId:       String,
    onConsumerChange: (String) -> Unit,
    mobileNumber:     String,
    onMobileChange:   (String) -> Unit,
    amount:           String,
    onAmountChange:   (String) -> Unit,
    billFetched:      Boolean,
    fetchedBillAmt:   String,
    onFetchBill:      () -> Unit,
    onProceed:        () -> Unit
) {
    val operators = when (category) {
        BbpsCategory.EMI         -> emiLenders
        BbpsCategory.ELECTRICITY -> electricityBoards
        BbpsCategory.LPG         -> lpgAgencies
        BbpsCategory.FASTAG      -> fastagBanks
    }

    val consumerLabel = when (category) {
        BbpsCategory.EMI         -> "Loan Account Number"
        BbpsCategory.ELECTRICITY -> "Consumer Number / CA No"
        BbpsCategory.LPG         -> "LPG Consumer Number"
        BbpsCategory.FASTAG      -> "Vehicle Number / Tag ID"
    }

    val consumerPlaceholder = when (category) {
        BbpsCategory.EMI         -> "Enter loan account number"
        BbpsCategory.ELECTRICITY -> "Enter consumer number"
        BbpsCategory.LPG         -> "Enter LPG consumer number"
        BbpsCategory.FASTAG      -> "e.g. MH12AB1234"
    }

    val operatorLabel = when (category) {
        BbpsCategory.EMI         -> "Select Lender / Bank"
        BbpsCategory.ELECTRICITY -> "Select Electricity Board"
        BbpsCategory.LPG         -> "Select Gas Agency"
        BbpsCategory.FASTAG      -> "Select FASTag Bank"
    }

    val canFetchBill = category == BbpsCategory.ELECTRICITY || category == BbpsCategory.LPG
    val needsManualAmount = category == BbpsCategory.EMI || category == BbpsCategory.FASTAG

    val isFormReady = selectedOperator.isNotEmpty()
            && consumerId.isNotEmpty()
            && mobileNumber.length == 10
            && amount.isNotEmpty()

    val mobileError = mobileNumber.isNotEmpty() && mobileNumber.length != 10
    val amountError = amount.isNotEmpty() && (amount.toDoubleOrNull() ?: 0.0) <= 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Category tag
        BbpsCategoryTag(category = category)

        // Operator / Provider
        SectionCard(
            title = operatorLabel,
            icon  = Icons.Default.Business
        ) {
            NavyDropdownField(
                label            = "$operatorLabel *",
                leadingIcon      = category.icon,
                selectedValue    = selectedOperator,
                options          = operators,
                onOptionSelected = onOperatorChange
            )
        }

        // Consumer / Account details
        SectionCard(title = "Account Details", icon = Icons.Default.AccountBox) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                NavyOutlinedField(
                    value         = consumerId,
                    onValueChange = onConsumerChange,
                    label         = "$consumerLabel *",
                    placeholder   = consumerPlaceholder,
                    leadingIcon   = Icons.Default.Badge,
                    keyboardType  = if (category == BbpsCategory.FASTAG) KeyboardType.Text
                    else KeyboardType.Number,
                    trailingIcon  = if (consumerId.length >= 6) ({
                        Icon(Icons.Default.CheckCircle, null,
                            tint = FintechColors.SuccessGreen)
                    }) else null
                )

                NavyOutlinedField(
                    value         = mobileNumber,
                    onValueChange = onMobileChange,
                    label         = "Registered Mobile *",
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

                // Fetch Bill button for Electricity & LPG
                if (canFetchBill && !billFetched && consumerId.length >= 6 && selectedOperator.isNotEmpty()) {
                    OutlinedButton(
                        onClick  = onFetchBill,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = FintechColors.NavyDark),
                        border   = androidx.compose.foundation.BorderStroke(
                            1.dp, FintechColors.NavyDark.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Fetch Bill Details", fontWeight = FontWeight.Medium)
                    }
                }

                // Fetched bill display
                AnimatedVisibility(visible = billFetched) {
                    Surface(
                        shape    = RoundedCornerShape(10.dp),
                        color    = FintechColors.SuccessGreenLight,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp,
                                FintechColors.SuccessGreen.copy(alpha = 0.4f),
                                RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Receipt, null,
                                tint = FintechColors.SuccessGreen, modifier = Modifier.size(20.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Bill Fetched Successfully",
                                    style      = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = FintechColors.SuccessGreenDark)
                                Text("Outstanding amount: ₹$fetchedBillAmt",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FintechColors.SuccessGreen)
                            }
                        }
                    }
                }
            }
        }

        // Amount section
        SectionCard(title = "Payment Amount", icon = Icons.Default.CurrencyRupee) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                NavyOutlinedField(
                    value         = amount,
                    onValueChange = onAmountChange,
                    label         = "Amount (₹) *",
                    placeholder   = "Enter amount to pay",
                    leadingIcon   = Icons.Default.CurrencyRupee,
                    keyboardType  = KeyboardType.Decimal,
                    isError       = amountError,
                    errorMessage  = "Enter a valid amount"
                )

                // Quick presets
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val presets = when (category) {
                        BbpsCategory.EMI         -> listOf("5000", "10000", "15000", "25000")
                        BbpsCategory.ELECTRICITY -> listOf("500", "1000", "2000", "5000")
                        BbpsCategory.LPG         -> listOf("850", "950", "1050", "1150")
                        BbpsCategory.FASTAG      -> listOf("200", "500", "1000", "2000")
                    }
                    presets.forEach { preset ->
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
            }
        }

        NavyPrimaryButton(
            text    = "Review & Pay",
            onClick = onProceed,
            enabled = isFormReady,
            icon    = Icons.Default.Visibility
        )

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun BbpsCategoryTag(category: BbpsCategory) {
    Surface(
        shape  = RoundedCornerShape(12.dp),
        color  = category.color.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape    = CircleShape,
                color    = category.color.copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(category.icon, null,
                        tint = category.color, modifier = Modifier.size(20.dp))
                }
            }
            Column {
                Text(category.label,
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color      = category.color)
                Text(category.desc,
                    style = MaterialTheme.typography.labelSmall,
                    color = category.color.copy(alpha = 0.7f))
            }
        }
    }
}


// ─────────────────────────────────────────────
// STEP 3 — CONFIRM & PAY
// ─────────────────────────────────────────────

@Composable
fun BbpsConfirmStep(
    category:   BbpsCategory,
    operator:   String,
    consumerId: String,
    amount:     String,
    txnRef:     String,
    onPay:      () -> Unit,
    onEdit:     () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BbpsCategoryTag(category = category)

        // Big amount card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp),
            colors   = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(category.color, category.color.copy(alpha = 0.7f))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Payment Amount",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelMedium)
                    Text("₹$amount",
                        color      = Color.White,
                        style      = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold)
                    Text("via BBPS",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Details table
        SectionCard(title = "Payment Details", icon = Icons.Default.Summarize) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    Icons.Default.Receipt       to ("Category"  to category.label),
                    Icons.Default.Business      to ("Provider"  to operator.split(" ").take(3).joinToString(" ")),
                    Icons.Default.Badge         to ("Consumer"  to consumerId),
                    Icons.Default.CurrencyRupee to ("Amount"    to "₹$amount"),
                    Icons.Default.Tag           to ("Ref No"    to txnRef),
                ).forEach { (icon, pair) ->
                    val (label, value) = pair
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(icon, null,
                            tint     = FintechColors.NavyDark.copy(alpha = 0.5f),
                            modifier = Modifier.size(15.dp))
                        Text(label,
                            style    = MaterialTheme.typography.bodySmall,
                            color    = colorScheme.outline,
                            modifier = Modifier.width(68.dp))
                        Text(value,
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color      = colorScheme.onSurface,
                            modifier   = Modifier.weight(1f))
                    }
                }
            }
        }

        NavyPrimaryButton(
            text  = "Pay Now",
            onClick = onPay,
            icon    = Icons.Default.Payment
        )
        OutlinedButton(
            onClick  = onEdit,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape    = RoundedCornerShape(10.dp),
            colors   = ButtonDefaults.outlinedButtonColors(
                contentColor = FintechColors.NavyDark),
            border   = androidx.compose.foundation.BorderStroke(
                1.dp, FintechColors.NavyDark.copy(alpha = 0.4f))
        ) { Text("Edit Details", fontWeight = FontWeight.Medium) }

        Spacer(Modifier.height(8.dp))
    }
}


// ─────────────────────────────────────────────
// STEP 5 — RESULT
// ─────────────────────────────────────────────

@Composable
fun BbpsResultStep(
    isSuccess: Boolean,
    category:  BbpsCategory,
    amount:    String,
    txnRef:    String,
    onDone:    () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }

    val scale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "scale"
    )

    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier            = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        Surface(
            shape    = CircleShape,
            color    = if (isSuccess) FintechColors.SuccessGreenLight
            else colorScheme.errorContainer,
            modifier = Modifier.size(100.dp).scale(scale)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    null,
                    tint     = if (isSuccess) FintechColors.SuccessGreen else colorScheme.error,
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        Text(if (isSuccess) "Payment Successful!" else "Payment Failed",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color      = if (isSuccess) FintechColors.SuccessGreenDark else colorScheme.error,
            textAlign  = TextAlign.Center)

        Text(if (isSuccess) "₹$amount paid for ${category.label}"
        else "Your payment could not be processed. No amount was deducted.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = colorScheme.outline,
            textAlign = TextAlign.Center)

        if (isSuccess) {
            Surface(
                shape    = RoundedCornerShape(12.dp),
                color    = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier              = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Transaction Ref",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.outline)
                        Text(txnRef,
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color      = FintechColors.NavyDark)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = FintechColors.SuccessGreenLight
                    ) {
                        Text("SUCCESS",
                            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color      = FintechColors.SuccessGreenDark)
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        NavyPrimaryButton(
            text    = "Pay Another Bill",
            onClick = onDone,
            icon    = Icons.Default.Add
        )
    }
}


// ══════════════════════════════════════════════════════════
//  BOOKING INSURANCE SCREEN
// ══════════════════════════════════════════════════════════

// ─────────────────────────────────────────────
// INSURANCE DATA MODELS
// ─────────────────────────────────────────────

enum class InsuranceType(
    val label:    String,
    val icon:     ImageVector,
    val color:    Color,
    val desc:     String
) {
    HEALTH(
        label = "Health Insurance",
        icon  = Icons.Default.HealthAndSafety,
        color = Color(0xFF0277BD),
        desc  = "Covers hospitalisation & medical expenses"
    ),
    LIFE(
        label = "Life Insurance",
        icon  = Icons.Default.Favorite,
        color = Color(0xFFC62828),
        desc  = "Financial protection for your family"
    ),
    VEHICLE(
        label = "Vehicle Insurance",
        icon  = Icons.Default.DirectionsCar,
        color = Color(0xFF2E7D32),
        desc  = "Two-wheeler & four-wheeler coverage"
    ),
    TRAVEL(
        label = "Travel Insurance",
        icon  = Icons.Default.Flight,
        color = Color(0xFF6A1B9A),
        desc  = "Covers trip cancellation & medical abroad"
    ),
    HOME(
        label = "Home Insurance",
        icon  = Icons.Default.Home,
        color = Color(0xFFF57C00),
        desc  = "Protect your home against damage & theft"
    ),
    CROP(
        label = "Crop Insurance",
        icon  = Icons.Default.Spa,
        color = Color(0xFF388E3C),
        desc  = "Coverage for crop loss & farm assets"
    )
}

enum class InsuranceStep {
    TYPE,        // pick insurance type
    DETAILS,     // fill personal + plan details
    PLANS,       // show plan cards
    APPLICANT,   // applicant info
    REVIEW,      // final review
    PROCESSING,
    RESULT
}

data class InsurancePlan(
    val id:       String,
    val name:     String,
    val provider: String,
    val premium:  String,
    val coverage: String,
    val tenure:   String,
    val features: List<String>
)

private fun plansFor(type: InsuranceType): List<InsurancePlan> = when (type) {
    InsuranceType.HEALTH -> listOf(
        InsurancePlan("h1","Silver Health","Star Health","₹4,500/yr","₹3 Lakh","1 Year",
            listOf("Hospitalisation","Day Care","Pre/Post Hospitalisation")),
        InsurancePlan("h2","Gold Health","HDFC Ergo","₹7,200/yr","₹5 Lakh","1 Year",
            listOf("Hospitalisation","Critical Illness","OPD Cover","Maternity")),
        InsurancePlan("h3","Platinum Health","Niva Bupa","₹11,500/yr","₹10 Lakh","1 Year",
            listOf("Hospitalisation","Critical Illness","OPD","Maternity","International Cover")),
    )
    InsuranceType.LIFE -> listOf(
        InsurancePlan("l1","Term Plan Basic","LIC","₹6,000/yr","₹50 Lakh","20 Years",
            listOf("Death Benefit","Accidental Death Rider")),
        InsurancePlan("l2","Term Plan Plus","HDFC Life","₹9,800/yr","₹1 Crore","30 Years",
            listOf("Death Benefit","Critical Illness","Disability Rider","Waiver of Premium")),
    )
    InsuranceType.VEHICLE -> listOf(
        InsurancePlan("v1","Third Party","Bajaj Allianz","₹2,094/yr","As per TP norms","1 Year",
            listOf("Third Party Liability","Personal Accident")),
        InsurancePlan("v2","Comprehensive","ICICI Lombard","₹8,500/yr","IDV of Vehicle","1 Year",
            listOf("Own Damage","Third Party","Zero Dep Add-on","Road Side Assistance")),
    )
    InsuranceType.TRAVEL -> listOf(
        InsurancePlan("t1","Domestic Travel","Tata AIG","₹299/trip","₹2 Lakh","Per Trip",
            listOf("Flight Delay","Baggage Loss","Medical Emergency")),
        InsurancePlan("t2","International Travel","Care Health","₹1,200/trip","$1 Lakh","Per Trip",
            listOf("Medical Emergency","Trip Cancellation","Passport Loss","Adventure Sports")),
    )
    InsuranceType.HOME -> listOf(
        InsurancePlan("ho1","Home Basic","New India","₹3,500/yr","₹25 Lakh","1 Year",
            listOf("Fire & Allied Perils","Burglary","Natural Disaster")),
        InsurancePlan("ho2","Home Comprehensive","HDFC Ergo","₹6,200/yr","₹50 Lakh","1 Year",
            listOf("Structure + Contents","Natural Disaster","Jewellery","Electronic Appliances")),
    )
    InsuranceType.CROP -> listOf(
        InsurancePlan("c1","PMFBY Basic","Govt Scheme","Subsidised","Full Crop Loss","1 Season",
            listOf("Natural Calamity","Drought","Flood","Pest Attack")),
        InsurancePlan("c2","Private Crop","ICICI Lombard","₹2,000/acre","Market Value","1 Season",
            listOf("Natural Calamity","Revenue Protection","Post-Harvest Losses")),
    )
}


@Composable
fun BookingInsuranceScreen(
    onBackClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    var step              by remember { mutableStateOf(InsuranceStep.TYPE) }
    var selectedType      by remember { mutableStateOf<InsuranceType?>(null) }
    var selectedPlan      by remember { mutableStateOf<InsurancePlan?>(null) }

    // Applicant fields
    var fullName     by remember { mutableStateOf("") }
    var dob          by remember { mutableStateOf("") }
    var mobile       by remember { mutableStateOf("") }
    var email        by remember { mutableStateOf("") }
    var aadhaar      by remember { mutableStateOf("") }
    var vehicleNo    by remember { mutableStateOf("") }   // vehicle insurance only

    var isSuccess    by remember { mutableStateOf(true) }
    val policyRef    = remember { "POL${System.currentTimeMillis().toString().takeLast(9)}" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        DetailTopBar(
            title = when (step) {
                InsuranceStep.TYPE       -> "Book Insurance"
                InsuranceStep.DETAILS    -> "${selectedType?.label ?: "Insurance"} Details"
                InsuranceStep.PLANS      -> "Choose a Plan"
                InsuranceStep.APPLICANT  -> "Applicant Details"
                InsuranceStep.REVIEW     -> "Review & Confirm"
                InsuranceStep.PROCESSING -> "Processing..."
                InsuranceStep.RESULT     -> if (isSuccess) "Policy Booked!" else "Booking Failed"
            },
            onBackClick = {
                when (step) {
                    InsuranceStep.DETAILS   -> step = InsuranceStep.TYPE
                    InsuranceStep.PLANS     -> step = InsuranceStep.DETAILS
                    InsuranceStep.APPLICANT -> step = InsuranceStep.PLANS
                    InsuranceStep.REVIEW    -> step = InsuranceStep.APPLICANT
                    else                   -> onBackClick()
                }
            }
        )

        // Progress indicator
        if (step != InsuranceStep.PROCESSING && step != InsuranceStep.RESULT) {
            InsuranceProgressBar(step = step)
        }

        AnimatedContent(
            targetState    = step,
            transitionSpec = {
                (fadeIn(tween(220)) + slideInHorizontally { it / 6 })
                    .togetherWith(fadeOut(tween(180)) + slideOutHorizontally { -it / 6 })
            },
            label = "ins_step"
        ) { current ->
            when (current) {

                InsuranceStep.TYPE -> InsuranceTypeStep(
                    selectedType = selectedType,
                    onTypeSelect = { selectedType = it },
                    onNext       = { step = InsuranceStep.DETAILS }
                )

                InsuranceStep.DETAILS -> InsuranceDetailsStep(
                    type      = selectedType!!,
                    vehicleNo = vehicleNo,
                    onVehicleNoChange = { vehicleNo = it },
                    onNext    = { step = InsuranceStep.PLANS }
                )

                InsuranceStep.PLANS -> InsurancePlansStep(
                    type         = selectedType!!,
                    selectedPlan = selectedPlan,
                    onPlanSelect = { selectedPlan = it },
                    onNext       = { step = InsuranceStep.APPLICANT }
                )

                InsuranceStep.APPLICANT -> InsuranceApplicantStep(
                    type          = selectedType!!,
                    fullName      = fullName,
                    onNameChange  = { fullName = it },
                    dob           = dob,
                    onDobChange   = { dob = it },
                    mobile        = mobile,
                    onMobileChange = { if (it.all(Char::isDigit)) mobile = it },
                    email         = email,
                    onEmailChange = { email = it },
                    aadhaar       = aadhaar,
                    onAadhaarChange = { if (it.all(Char::isDigit)) aadhaar = it },
                    onNext        = { step = InsuranceStep.REVIEW }
                )

                InsuranceStep.REVIEW -> InsuranceReviewStep(
                    type      = selectedType!!,
                    plan      = selectedPlan!!,
                    fullName  = fullName,
                    mobile    = mobile,
                    aadhaar   = aadhaar,
                    policyRef = policyRef,
                    onConfirm = { step = InsuranceStep.PROCESSING },
                    onEdit    = { step = InsuranceStep.APPLICANT }
                )

                InsuranceStep.PROCESSING -> {
                    LaunchedEffect(Unit) {
                        delay(2000); isSuccess = true; step = InsuranceStep.RESULT
                    }
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            CircularProgressIndicator(color = FintechColors.NavyDark,
                                modifier = Modifier.size(56.dp), strokeWidth = 4.dp)
                            Text("Booking your policy...",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold, color = FintechColors.NavyDark)
                            Text("This may take a few seconds",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.outline)
                        }
                    }
                }

                InsuranceStep.RESULT -> InsuranceResultStep(
                    isSuccess = isSuccess,
                    type      = selectedType!!,
                    plan      = selectedPlan!!,
                    policyRef = policyRef,
                    fullName  = fullName,
                    onDone    = {
                        step         = InsuranceStep.TYPE
                        selectedType = null
                        selectedPlan = null
                        fullName     = ""
                        mobile       = ""
                        aadhaar      = ""
                        email        = ""
                        dob          = ""
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// INSURANCE PROGRESS BAR
// ─────────────────────────────────────────────

@Composable
fun InsuranceProgressBar(step: InsuranceStep) {
    val steps = listOf("Type", "Details", "Plans", "Applicant", "Review")
    val current = when (step) {
        InsuranceStep.TYPE      -> 0
        InsuranceStep.DETAILS   -> 1
        InsuranceStep.PLANS     -> 2
        InsuranceStep.APPLICANT -> 3
        InsuranceStep.REVIEW    -> 4
        else                   -> 4
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val isDone   = current > index
            val isActive = current == index
            Column(
                modifier            = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape    = CircleShape,
                    color    = when {
                        isDone   -> FintechColors.SuccessGreen
                        isActive -> FintechColors.NavyDark
                        else     -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    },
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (isDone) Icon(Icons.Default.Check, null,
                            tint = Color.White, modifier = Modifier.size(13.dp))
                        else Text("${index + 1}",
                            color      = if (isActive) Color.White else MaterialTheme.colorScheme.outline,
                            fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(label,
                    style     = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color     = when { isDone -> FintechColors.SuccessGreen; isActive -> FintechColors.NavyDark; else -> MaterialTheme.colorScheme.outline },
                    textAlign = TextAlign.Center)
            }
            if (index < steps.lastIndex) {
                Box(modifier = Modifier.weight(0.3f).height(2.dp).padding(bottom = 14.dp)
                    .background(if (isDone) FintechColors.SuccessGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)))
            }
        }
    }
}


// ─────────────────────────────────────────────
// INSURANCE TYPE STEP
// ─────────────────────────────────────────────

@Composable
fun InsuranceTypeStep(
    selectedType: InsuranceType?,
    onTypeSelect: (InsuranceType) -> Unit,
    onNext:       () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NavyHeaderCard(
            icon     = Icons.Default.Security,
            title    = "Book Insurance",
            subtitle = "Choose the type of insurance you want to book"
        )
        Text("Select Insurance Type",
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        // 2-column grid
        val types = InsuranceType.entries.chunked(2)
        types.forEach { row ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { type ->
                    InsuranceTypeCard(
                        type       = type,
                        isSelected = selectedType == type,
                        onClick    = { onTypeSelect(type) },
                        modifier   = Modifier.weight(1f)
                    )
                }
                if (row.size < 2) Spacer(Modifier.weight(1f))
            }
        }

        NavyPrimaryButton(text = "Continue", onClick = onNext,
            enabled = selectedType != null, icon = Icons.Default.ArrowForward)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun InsuranceTypeCard(
    type:       InsuranceType,
    isSelected: Boolean,
    onClick:    () -> Unit,
    modifier:   Modifier = Modifier
) {
    Surface(
        shape    = RoundedCornerShape(16.dp),
        color    = if (isSelected) type.color.copy(alpha = 0.1f)
        else MaterialTheme.colorScheme.surface,
        modifier = modifier
            .border(
                width = if (isSelected) 2.dp else 0.5.dp,
                color = if (isSelected) type.color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier            = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(shape = RoundedCornerShape(12.dp),
                color    = type.color.copy(alpha = if (isSelected) 0.18f else 0.08f),
                modifier = Modifier.size(52.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(type.icon, null, tint = type.color, modifier = Modifier.size(28.dp))
                }
            }
            Text(type.label,
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color      = if (isSelected) type.color else MaterialTheme.colorScheme.onSurface,
                textAlign  = TextAlign.Center)
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, null,
                    tint = type.color, modifier = Modifier.size(16.dp))
            }
        }
    }
}


// ─────────────────────────────────────────────
// INSURANCE DETAILS STEP
// ─────────────────────────────────────────────

@Composable
fun InsuranceDetailsStep(
    type:             InsuranceType,
    vehicleNo:        String,
    onVehicleNoChange: (String) -> Unit,
    onNext:           () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Type info card
        Surface(shape = RoundedCornerShape(14.dp),
            color    = type.color.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth()
                .border(1.dp, type.color.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
        ) {
            Row(modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = CircleShape, color = type.color.copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp)) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(type.icon, null, tint = type.color, modifier = Modifier.size(24.dp))
                    }
                }
                Column {
                    Text(type.label, style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold, color = type.color)
                    Text(type.desc, style = MaterialTheme.typography.labelSmall,
                        color = type.color.copy(alpha = 0.7f))
                }
            }
        }

        // Vehicle number for vehicle insurance
        if (type == InsuranceType.VEHICLE) {
            SectionCard(title = "Vehicle Details", icon = Icons.Default.DirectionsCar) {
                NavyOutlinedField(
                    value         = vehicleNo,
                    onValueChange = { onVehicleNoChange(it.uppercase()) },
                    label         = "Vehicle Registration Number *",
                    placeholder   = "e.g. MH12AB1234",
                    leadingIcon   = Icons.Default.Numbers
                )
            }
        }

        SectionCard(title = "Coverage Information", icon = Icons.Default.Info) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val points = when (type) {
                    InsuranceType.HEALTH  -> listOf("Cashless at 10,000+ hospitals","Pre-existing after 2 years","Annual health check-up free")
                    InsuranceType.LIFE    -> listOf("Pure term — no survival benefit","Premium stays fixed","Tax benefit under 80C")
                    InsuranceType.VEHICLE -> listOf("Mandatory 3rd party cover","Optional own-damage cover","NCB up to 50% discount")
                    InsuranceType.TRAVEL  -> listOf("Per-trip coverage","24×7 emergency helpline","Covers baggage and delay")
                    InsuranceType.HOME    -> listOf("Structure & contents covered","Natural calamity protection","Easy online claim process")
                    InsuranceType.CROP    -> listOf("Government subsidised scheme","Covers natural calamities","Payout via bank transfer")
                }
                points.forEach { point ->
                    Row(verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CheckCircle, null,
                            tint = FintechColors.SuccessGreen, modifier = Modifier.size(16.dp))
                        Text(point, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        NavyPrimaryButton(
            text    = "View Plans",
            onClick = onNext,
            enabled = if (type == InsuranceType.VEHICLE) vehicleNo.length >= 8 else true,
            icon    = Icons.Default.ArrowForward
        )
        Spacer(Modifier.height(8.dp))
    }
}


// ─────────────────────────────────────────────
// INSURANCE PLANS STEP
// ─────────────────────────────────────────────

@Composable
fun InsurancePlansStep(
    type:         InsuranceType,
    selectedPlan: InsurancePlan?,
    onPlanSelect: (InsurancePlan) -> Unit,
    onNext:       () -> Unit
) {
    val plans = plansFor(type)
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Available Plans", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold)

        plans.forEach { plan ->
            InsurancePlanCard(
                plan       = plan,
                type       = type,
                isSelected = selectedPlan?.id == plan.id,
                onClick    = { onPlanSelect(plan) }
            )
        }

        NavyPrimaryButton(
            text    = "Continue with Selected Plan",
            onClick = onNext,
            enabled = selectedPlan != null,
            icon    = Icons.Default.ArrowForward
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun InsurancePlanCard(
    plan:       InsurancePlan,
    type:       InsuranceType,
    isSelected: Boolean,
    onClick:    () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        shape    = RoundedCornerShape(16.dp),
        color    = if (isSelected) type.color.copy(alpha = 0.07f) else colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 0.5.dp,
                color = if (isSelected) type.color else colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically) {
                Column {
                    Text(plan.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) type.color else colorScheme.onSurface)
                    Text(plan.provider,
                        style = MaterialTheme.typography.labelSmall, color = colorScheme.outline)
                }
                if (isSelected) {
                    Surface(shape = CircleShape, color = type.color, modifier = Modifier.size(28.dp)) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Default.Check, null,
                                tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Premium" to plan.premium, "Coverage" to plan.coverage, "Tenure" to plan.tenure)
                    .forEach { (label, value) ->
                        Surface(shape = RoundedCornerShape(8.dp),
                            color    = if (isSelected) type.color.copy(alpha = 0.12f)
                            else colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.outline, fontSize = 9.sp)
                                Text(value,
                                    style      = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color      = if (isSelected) type.color else colorScheme.onSurface,
                                    textAlign  = TextAlign.Center)
                            }
                        }
                    }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                plan.features.forEach { feature ->
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Check, null,
                            tint     = if (isSelected) type.color else FintechColors.SuccessGreen,
                            modifier = Modifier.size(13.dp))
                        Text(feature,
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
// APPLICANT STEP
// ─────────────────────────────────────────────

@Composable
fun InsuranceApplicantStep(
    type:           InsuranceType,
    fullName:       String, onNameChange:    (String) -> Unit,
    dob:            String, onDobChange:     (String) -> Unit,
    mobile:         String, onMobileChange:  (String) -> Unit,
    email:          String, onEmailChange:   (String) -> Unit,
    aadhaar:        String, onAadhaarChange: (String) -> Unit,
    onNext:         () -> Unit
) {
    val mobileError  = mobile.isNotEmpty()  && mobile.length  != 10
    val aadhaarError = aadhaar.isNotEmpty() && aadhaar.length != 12
    val isReady      = fullName.isNotBlank() && mobile.length == 10 && aadhaar.length == 12

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(title = "Applicant Details", icon = Icons.Default.Person) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NavyOutlinedField(value = fullName, onValueChange = onNameChange,
                    label = "Full Name *", placeholder = "As per Aadhaar/PAN",
                    leadingIcon = Icons.Default.Person)
                NavyOutlinedField(value = dob, onValueChange = onDobChange,
                    label = "Date of Birth *", placeholder = "DD/MM/YYYY",
                    leadingIcon = Icons.Default.CalendarToday)
                NavyOutlinedField(value = mobile, onValueChange = onMobileChange,
                    label = "Mobile Number *", placeholder = "10-digit mobile",
                    leadingIcon = Icons.Default.Phone, keyboardType = KeyboardType.Phone,
                    maxLength = 10, isError = mobileError,
                    errorMessage = "Enter valid 10-digit number")
                NavyOutlinedField(value = email, onValueChange = onEmailChange,
                    label = "Email Address", placeholder = "your@email.com",
                    leadingIcon = Icons.Default.Email, keyboardType = KeyboardType.Email)
                NavyOutlinedField(value = aadhaar, onValueChange = onAadhaarChange,
                    label = "Aadhaar Number *", placeholder = "12-digit Aadhaar",
                    leadingIcon = Icons.Default.CreditCard, keyboardType = KeyboardType.Number,
                    maxLength = 12, isError = aadhaarError,
                    errorMessage = "Aadhaar must be 12 digits",
                    trailingIcon = if (aadhaar.length == 12) ({
                        Icon(Icons.Default.CheckCircle, null, tint = FintechColors.SuccessGreen)
                    }) else null)
            }
        }

        NavyPrimaryButton(text = "Review Application", onClick = onNext,
            enabled = isReady, icon = Icons.Default.Visibility)
        Spacer(Modifier.height(8.dp))
    }
}


// ─────────────────────────────────────────────
// REVIEW STEP
// ─────────────────────────────────────────────

@Composable
fun InsuranceReviewStep(
    type:      InsuranceType,
    plan:      InsurancePlan,
    fullName:  String,
    mobile:    String,
    aadhaar:   String,
    policyRef: String,
    onConfirm: () -> Unit,
    onEdit:    () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Plan summary
        Surface(shape = RoundedCornerShape(16.dp), color = type.color.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth()
                .border(1.dp, type.color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))) {
            Column(modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(type.icon, null, tint = type.color, modifier = Modifier.size(22.dp))
                    Text(plan.name, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold, color = type.color)
                }
                Text(plan.provider, style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.outline)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Premium: ${plan.premium}", style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold, color = type.color)
                    Text("Cover: ${plan.coverage}", style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.outline)
                }
            }
        }

        SectionCard(title = "Applicant Summary", icon = Icons.Default.Person) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Icons.Default.Person        to ("Name"    to fullName),
                    Icons.Default.Phone         to ("Mobile"  to mobile),
                    Icons.Default.CreditCard    to ("Aadhaar" to "XXXX XXXX ${aadhaar.takeLast(4)}"),
                    Icons.Default.Tag           to ("Ref No"  to policyRef),
                ).forEach { (icon, pair) ->
                    val (label, value) = pair
                    Row(modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(icon, null,
                            tint = FintechColors.NavyDark.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp))
                        Text(label, style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.outline, modifier = Modifier.width(64.dp))
                        Text(value, style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold, color = colorScheme.onSurface,
                            modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        NavyPrimaryButton(text = "Confirm & Book Policy", onClick = onConfirm,
            icon = Icons.Default.CheckCircle)
        OutlinedButton(onClick = onEdit,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape    = RoundedCornerShape(10.dp),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = FintechColors.NavyDark),
            border   = androidx.compose.foundation.BorderStroke(
                1.dp, FintechColors.NavyDark.copy(alpha = 0.4f))
        ) { Text("Edit Details", fontWeight = FontWeight.Medium) }
        Spacer(Modifier.height(8.dp))
    }
}


// ─────────────────────────────────────────────
// RESULT STEP
// ─────────────────────────────────────────────

@Composable
fun InsuranceResultStep(
    isSuccess: Boolean,
    type:      InsuranceType,
    plan:      InsurancePlan,
    policyRef: String,
    fullName:  String,
    onDone:    () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }
    val scale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "scale")
    val colorScheme = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(24.dp))

        Surface(shape = CircleShape,
            color    = if (isSuccess) FintechColors.SuccessGreenLight else colorScheme.errorContainer,
            modifier = Modifier.size(100.dp).scale(scale)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(if (isSuccess) Icons.Default.VerifiedUser else Icons.Default.Cancel, null,
                    tint     = if (isSuccess) FintechColors.SuccessGreen else colorScheme.error,
                    modifier = Modifier.size(54.dp))
            }
        }

        Text(if (isSuccess) "Policy Booked Successfully!" else "Booking Failed",
            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
            color = if (isSuccess) FintechColors.SuccessGreenDark else colorScheme.error,
            textAlign = TextAlign.Center)

        if (isSuccess) {
            Text("Congratulations $fullName! Your ${plan.name} policy has been issued.",
                style = MaterialTheme.typography.bodyMedium, color = colorScheme.outline,
                textAlign = TextAlign.Center)

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(type.icon, null, tint = type.color, modifier = Modifier.size(18.dp))
                        Text("Policy Details", style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold, color = type.color)
                    }
                    HorizontalDivider(color = type.color.copy(alpha = 0.2f))
                    listOf("Plan" to plan.name, "Provider" to plan.provider,
                        "Coverage" to plan.coverage, "Premium" to plan.premium,
                        "Policy Ref" to policyRef, "Status" to "ACTIVE"
                    ).forEach { (label, value) ->
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(label, style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.outline)
                            Text(value, style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (label == "Status") FontWeight.Bold else FontWeight.Medium,
                                color = if (label == "Status") FintechColors.SuccessGreen else colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))
        NavyPrimaryButton(text = "Book Another Policy", onClick = onDone, icon = Icons.Default.Add)
    }
}


// ─────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────

@Preview(name = "BBPS – Light", showBackground = true)
@Preview(name = "BBPS – Dark",  showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewBbpsScreen() {
    MaterialTheme { BbpsScreen() }
}

@Preview(name = "Insurance – Light", showBackground = true)
@Preview(name = "Insurance – Dark",  showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewInsuranceScreen() {
    MaterialTheme { BookingInsuranceScreen() }
}

@Preview(name = "BBPS Category Card", showBackground = true)
@Composable
fun PreviewBbpsCategoryCard() {
    MaterialTheme {
        Box(Modifier.padding(16.dp)) {
            BbpsCategoryCard(category = BbpsCategory.ELECTRICITY, isSelected = true, onClick = {})
        }
    }
}

@Preview(name = "Insurance Type Grid", showBackground = true)
@Composable
fun PreviewInsuranceTypeStep() {
    MaterialTheme {
        InsuranceTypeStep(
            selectedType = InsuranceType.HEALTH,
            onTypeSelect = {},
            onNext       = {}
        )
    }
}