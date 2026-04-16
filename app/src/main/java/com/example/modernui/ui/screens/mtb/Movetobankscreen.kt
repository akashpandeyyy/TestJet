package com.example.modernui.ui.screens.mtb

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.modernui.Api.model.MtbBankData
import com.example.modernui.ui.components.*
import com.example.modernui.ui.theme.FintechColors
import kotlinx.coroutines.delay


// ─────────────────────────────────────────────
// DATA MODELS
// ─────────────────────────────────────────────

enum class PayoutBankStatus { ACTIVE, PENDING, INACTIVE }

enum class TransferMode(val label: String, val desc: String) {
    IMPS("IMPS", "Instant • 24×7 • Up to ₹5 Lakh"),
    NEFT("NEFT", "Batch • Mon–Sat • Up to ₹10 Lakh"),
}

data class PayoutBank(
    val id:        String,

    val name:      String,
    val mobile:    String,
    val accountNo: String,
    val ifsc:      String,
    val bankName:  String,
    val status:    PayoutBankStatus,
    val initials:  String
)

// Dialog flow steps
enum class PayDialogStep {
    AMOUNT_MODE,   // enter amount + pick IMPS/NEFT/RTGS
    PREVIEW,       // show full transaction preview
    CONFIRM,       // "Are you sure?" alert
    PROCESSING,    // spinner while API call
    RESULT         // success or failure
}

//private val mockPayoutBanks = listOf(
//    PayoutBank(
//        id        = "pb1",
//        name      = "Rahul Sharma",
//        mobile    = "9876543210",
//        accountNo = "XXXX XXXX 4291",
//        ifsc      = "SBIN0001234",
//        bankName  = "State Bank of India",
//        status    = PayoutBankStatus.ACTIVE,
//        initials  = "RS"
//    ),
//    PayoutBank(
//        id        = "pb2",
//        name      = "Priya Verma",
//        mobile    = "9999123456",
//        accountNo = "XXXX XXXX 8803",
//        ifsc      = "HDFC0005678",
//        bankName  = "HDFC Bank",
//        status    = PayoutBankStatus.ACTIVE,
//        initials  = "PV"
//    ),
//    PayoutBank(
//        id        = "pb3",
//        name      = "Amit Gupta",
//        mobile    = "8888076543",
//        accountNo = "XXXX XXXX 1147",
//        ifsc      = "ICIC0009876",
//        bankName  = "ICICI Bank",
//        status    = PayoutBankStatus.PENDING,
//        initials  = "AG"
//    ),
//    PayoutBank(
//        id        = "pb4",
//        name      = "Sunita Devi",
//        mobile    = "7777654321",
//        accountNo = "XXXX XXXX 3366",
//        ifsc      = "PUNB0004321",
//        bankName  = "Punjab National Bank",
//        status    = PayoutBankStatus.INACTIVE,
//        initials  = "SD"
//    ),
//    PayoutBank(
//        id        = "pb5",
//        name      = "Vikram Singh",
//        mobile    = "6666987654",
//        accountNo = "XXXX XXXX 7712",
//        ifsc      = "BARB0000123",
//        bankName  = "Bank of Baroda",
//        status    = PayoutBankStatus.ACTIVE,
//        initials  = "VS"
//    ),
//)


// ─────────────────────────────────────────────
// MOVE TO BANK — ROOT SCREEN
// ─────────────────────────────────────────────

@Composable
fun MoveToBankScreen(
    onBackClick: () -> Unit = {},
    viewModel: MtbViewModel = hiltViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme

    // ── State ─────────────────────────────────
    val banks by viewModel.banks.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()

    var showAddSheet    by remember { mutableStateOf(false) }
    var selectedBank: MtbBankData? by remember { mutableStateOf(null) }
    var showPayDialog   by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchBanks()
    }

    // ── Add Bank Sheet ────────────────────────
    if (showAddSheet) {
        AddPayoutBankSheet(
            onDismiss  = { showAddSheet = false },
            onBankAdded = { newBank ->
                viewModel.addBank(
                    name = newBank.name,
                    mobile = newBank.mobile,
                    accountNo = newBank.accountNo,
                    ifsc = newBank.ifsc,
                    bankName = newBank.bankName
                )
                showAddSheet = false
            }
        )
    }

    // ── Pay Dialog ────────────────────────────
    if (showPayDialog && selectedBank != null) {
        PayTransferDialog(
            bank      = selectedBank!!,
            onDismiss = {},
            onPaySubmit = { amount, mode, onResult ->
                viewModel.performTransfer(selectedBank!!, amount, mode, onResult)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        // ── Top Bar ───────────────────────────
        DetailTopBar(
            title       = "Move to Bank",
            onBackClick = onBackClick,
            actions     = {
                Text(
                    text = balance,
                    color = Color.White,
                    modifier = Modifier.padding(end = 8.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Wallet balance strip ──────────
                item {
                    WalletBalanceStrip(balance = balance)
                }

                // ── List header row ───────────────
                item {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            "Payout Bank Accounts",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = colorScheme.onBackground
                        )
                        TextButton(onClick = { showAddSheet = true }) {
                            Icon(Icons.Default.Add, null,
                                tint     = FintechColors.NavyDark,
                                modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Bank",
                                color = FintechColors.NavyDark,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // ── Bank Cards List ────────────────────
                if (banks?.isEmpty()!! && !isLoading) {
                    item {
                        EmptyBankListPlaceholder(onAdd = { showAddSheet = true })
                    }
                } else {
                    items(banks!!) { bank ->
                        PayoutBankCard(
                            bank     = bank,
                            onPayClick = {
                                selectedBank  = bank
                                showPayDialog = true
                            }
                        )
                    }
                }

                // Status legend
                item {
                    StatusLegend()
                }

                item {
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Loading Overlay
            if (isLoading && banks?.isEmpty()!!) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = FintechColors.NavyDark
                )
            }
        }
    }
}


// ─────────────────────────────────────────────
// WALLET BALANCE STRIP
// ─────────────────────────────────────────────

@Composable
fun WalletBalanceStrip(balance: String) {
    Surface(
        shape    = RoundedCornerShape(14.dp),
        color    = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(FintechColors.NavyDark, FintechColors.NavyLight)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text("Available Wallet Balance",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelMedium)
                    Text(balance,
                        color      = Color.White,
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold)
                }
                Surface(
                    shape    = CircleShape,
                    color    = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.AccountBalanceWallet, null,
                            tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
// PAYOUT BANK CARD — table-like layout
// ─────────────────────────────────────────────

@Composable
fun PayoutBankCard(
    bank:      MtbBankData,
    onPayClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Row 1: Avatar + Name + Status badge ──
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Initials avatar
                Surface(
                    shape    = CircleShape,
                    color    = FintechColors.NavyDark.copy(alpha = 0.1f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        bank.name?.let {
                            Text(it,
                                color      = FintechColors.NavyDark,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 14.sp)
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    bank.name?.let {
                        Text(it,
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color      = colorScheme.onSurface)
                    }
                    bank.bankName?.let {
                        Text(it,
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.outline)
                    }
                }
                StatusBadge(status = bank.status!!)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(Modifier.height(12.dp))

            // ── Row 2: Details grid (Bank, A/C, IFSC) ──
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BankDetailItem(Icons.Default.CreditCard, "Account", bank.accountNo!!, Modifier.weight(1.2f))
                BankDetailItem(Icons.Default.Numbers, "IFSC", bank.ifscCode!!, Modifier.weight(0.8f))
                BankDetailItem(Icons.Default.Numbers, "BENE ID", bank.beneId!!, Modifier.weight(0.8f))
            }

            Spacer(Modifier.height(16.dp))

            // ── Row 3: Action button ──
            val isEnabled = bank.status
            Button(
                onClick  = onPayClick,
                enabled  = isEnabled!!,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = FintechColors.NavyDark,
                    disabledContainerColor = colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                Icon(Icons.Default.Send, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (isEnabled) "Transfer Money" else "Verification Pending",
                    fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BankDetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, modifier: Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier              = modifier,
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null,
            tint     = FintechColors.NavyDark.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp))
        Column {
            Text(label,
                style    = MaterialTheme.typography.labelSmall,
                color    = colorScheme.outline)
            Text(value,
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color      = colorScheme.onSurface)
        }
    }
}

@Composable
fun StatusBadge(status: Boolean) {



    val (bgColor, textColor, dotColor, label) = when (status) {
        true   -> arrayOf(
            FintechColors.SuccessGreenLight,
            FintechColors.SuccessGreenDark,
            FintechColors.SuccessGreen,
            "Active"
        )
        false  -> arrayOf(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.error,
            "Inactive"
        )
    }
    Surface(
        shape    = RoundedCornerShape(20.dp),
        color    = bgColor as Color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor as Color)
            )
            Text(
                label as String,
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color      = textColor as Color
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatusLegend() {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        FlowRow(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 3
        ) {
            listOf(
                Pair(FintechColors.SuccessGreen, "Active — Pay enabled"),
                Pair(Color(0xFFFFC107), "Pending — Awaiting approval"),
                Pair(MaterialTheme.colorScheme.error, "Inactive — Cannot pay")
            ).forEach { (color: Color, label: String) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.outline,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyBankListPlaceholder(onAdd: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        shape    = RoundedCornerShape(16.dp),
        color    = colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier            = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.AccountBalance, null,
                tint = colorScheme.outline, modifier = Modifier.size(48.dp))
            Text("No Payout Banks Added",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = colorScheme.outline)
            Text("Add a bank account to start transferring funds",
                style     = MaterialTheme.typography.bodySmall,
                color     = colorScheme.outline.copy(alpha = 0.7f),
                textAlign = TextAlign.Center)
            NavyPrimaryButton(
                text  = "Add Bank Account",
                onClick = onAdd,
                icon    = Icons.Default.Add
            )
        }
    }
}


// ─────────────────────────────────────────────
// ADD PAYOUT BANK BOTTOM SHEET
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPayoutBankSheet(
    onDismiss:   () -> Unit,
    onBankAdded: (PayoutBank) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name      by remember { mutableStateOf("") }
    var mobile    by remember { mutableStateOf("") }
    var accountNo by remember { mutableStateOf("") }
    var ifsc      by remember { mutableStateOf("") }
    var bankName  by remember { mutableStateOf("") }

    val isValid = name.isNotBlank()
            && mobile.length == 10
            && accountNo.isNotBlank()
            && ifsc.isNotBlank()
            && bankName.isNotBlank()

    val bankOptions = listOf(
        "State Bank of India", "Punjab National Bank", "Bank of Baroda",
        "Canara Bank", "HDFC Bank", "ICICI Bank", "Axis Bank",
        "Union Bank of India", "Indian Bank", "Bank of India"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor   = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Sheet header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(FintechColors.NavyDark, FintechColors.NavyLight))
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.AddCard, null,
                        tint = Color.White, modifier = Modifier.size(24.dp))
                    Column {
                        Text("Add Payout Bank",
                            color      = Color.White,
                            fontWeight = FontWeight.Bold,
                            style      = MaterialTheme.typography.titleMedium)
                        Text("New account will be reviewed before activation",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NavyOutlinedField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = "Account Holder Name *",
                    placeholder   = "Full name as per bank",
                    leadingIcon   = Icons.Default.Person
                )
                NavyOutlinedField(
                    value         = mobile,
                    onValueChange = { if (it.all(Char::isDigit)) mobile = it },
                    label         = "Mobile Number *",
                    placeholder   = "10-digit mobile",
                    leadingIcon   = Icons.Default.Phone,
                    keyboardType  = KeyboardType.Phone,
                    maxLength     = 10
                )
                NavyOutlinedField(
                    value         = accountNo,
                    onValueChange = { accountNo = it },
                    label         = "Account Number *",
                    placeholder   = "Enter bank account number",
                    leadingIcon   = Icons.Default.CreditCard,
                    keyboardType  = KeyboardType.Number
                )
                NavyOutlinedField(
                    value         = ifsc,
                    onValueChange = { ifsc = it.uppercase() },
                    label         = "IFSC Code *",
                    placeholder   = "e.g. SBIN0001234",
                    leadingIcon   = Icons.Default.Numbers
                )
                NavyDropdownField(
                    label            = "Bank Name *",
                    leadingIcon      = Icons.Default.AccountBalance,
                    selectedValue    = bankName,
                    options          = bankOptions,
                    onOptionSelected = { bankName = it }
                )

                // Pending notice
                Surface(
                    shape    = RoundedCornerShape(10.dp),
                    color    = Color(0xFFFFF3CD),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, null,
                            tint     = Color(0xFF856404),
                            modifier = Modifier.size(16.dp))
                        Text("Account will be added with Pending status until verified by admin",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF856404))
                    }
                }

                NavyPrimaryButton(
                    text    = "Add Bank Account",
                    onClick = {
                        val initials = name.split(" ")
                            .take(2).mapNotNull { it.firstOrNull()?.toString() }.joinToString("")
                        onBankAdded(
                            PayoutBank(
                                id        = "pb_${System.currentTimeMillis()}",
                                name      = name,
                                mobile    = mobile,
                                accountNo = accountNo,
                                ifsc      = ifsc,
                                bankName  = bankName,
                                status    = PayoutBankStatus.PENDING,
                                initials  = initials.uppercase().ifEmpty { "??" }
                            )
                        )
                    },
                    enabled = isValid,
                    icon    = Icons.Default.Check
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}


// ─────────────────────────────────────────────
// PAY TRANSFER DIALOG  — 5-step internal flow
// ─────────────────────────────────────────────

@Composable
fun PayTransferDialog(
    bank:      MtbBankData,
    onDismiss: () -> Unit,
    onPaySubmit: (String, TransferMode, (Boolean, String) -> Unit) -> Unit = { _, _, _ -> }
) {
    var step         by remember { mutableStateOf(PayDialogStep.AMOUNT_MODE) }
    var amount       by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf(TransferMode.IMPS) }
    var isSuccess    by remember { mutableStateOf(true) }
    val txnRef       = remember { "MTB${System.currentTimeMillis().toString().takeLast(9)}" }
    var resultMessage by remember { mutableStateOf("") }

    val amountError = amount.isNotEmpty() && (amount.toDoubleOrNull() ?: 0.0) <= 0.0
    val isAmountReady = amount.isNotEmpty() && !amountError

    Dialog(
        onDismissRequest = { if (step == PayDialogStep.RESULT) onDismiss() },
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // ── Dialog top bar ────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(FintechColors.NavyDark, FintechColors.NavyLight)),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            when (step) {

                                PayDialogStep.AMOUNT_MODE -> "Transfer Details"
                                PayDialogStep.PREVIEW     -> "Preview Transfer"
                                PayDialogStep.CONFIRM     -> "Confirm Transfer"
                                PayDialogStep.PROCESSING  -> "Processing..."
                                PayDialogStep.RESULT      -> if (isSuccess) "Transfer Successful" else "Transfer Failed"
                            },
                            color      = Color.White,
                            fontWeight = FontWeight.Bold,
                            style      = MaterialTheme.typography.titleMedium
                        )
                        if (step != PayDialogStep.PROCESSING) {
                            IconButton(
                                onClick  = onDismiss,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, "Close", tint = Color.White)
                            }
                        }
                    }
                }

                // ── Step indicator dots ───────
                if (step != PayDialogStep.PROCESSING && step != PayDialogStep.RESULT) {
                    DialogStepDots(current = step)
                }

                // ── Content per step ──────────
                AnimatedContent(
                    targetState    = step,
                    transitionSpec = {
                        (fadeIn(tween(200)) + slideInHorizontally { it / 5 })
                            .togetherWith(fadeOut(tween(150)) + slideOutHorizontally { -it / 5 })
                    },
                    label = "dialog_step"
                ) { currentStep ->
                    when (currentStep) {

                        // STEP 1 ── Amount + Mode
                        PayDialogStep.AMOUNT_MODE -> {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Recipient strip
                                DialogRecipientStrip(bank = bank)

                                // Amount field
                                NavyOutlinedField(
                                    value         = amount,
                                    onValueChange = { amount = it },
                                    label         = "Amount (₹) *",
                                    placeholder   = "Enter transfer amount",
                                    leadingIcon   = Icons.Default.CurrencyRupee,
                                    keyboardType  = KeyboardType.Decimal,
                                    isError       = amountError,
                                    errorMessage  = "Enter a valid amount"
                                )

                                // Quick presets
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("1000", "2000", "5000", "10000").forEach { p ->
                                        FilterChip(
                                            selected = amount == p,
                                            onClick  = { amount = p },
                                            label    = {
                                                Text("₹$p",
                                                    style = MaterialTheme.typography.labelSmall)
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                // Transfer mode selector
                                Text("Transfer Mode",
                                    style      = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = FintechColors.NavyDark)

                                TransferMode.entries.forEach { mode ->
                                    TransferModeCard(
                                        mode       = mode,
                                        isSelected = selectedMode == mode,
                                        onClick    = { selectedMode = mode }
                                    )
                                }

                                NavyPrimaryButton(
                                    text    = "Preview Transfer",
                                    onClick = { step = PayDialogStep.PREVIEW },
                                    enabled = isAmountReady,
                                    icon    = Icons.Default.Visibility
                                )
                            }
                        }

                        // STEP 2 ── Preview
                        PayDialogStep.PREVIEW -> {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                TransferPreviewCard(
                                    bank   = bank,
                                    amount = amount,
                                    mode   = selectedMode,
                                    txnRef = txnRef
                                )

                                NavyPrimaryButton(
                                    text    = "Pay Now",
                                    onClick = { step = PayDialogStep.CONFIRM },
                                    icon    = Icons.Default.Payment
                                )
                                OutlinedButton(
                                    onClick  = { step = PayDialogStep.AMOUNT_MODE },
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                    shape    = RoundedCornerShape(10.dp),
                                    colors   = ButtonDefaults.outlinedButtonColors(
                                        contentColor = FintechColors.NavyDark
                                    ),
                                    border   = androidx.compose.foundation.BorderStroke(
                                        1.dp, FintechColors.NavyDark.copy(alpha = 0.4f))
                                ) {
                                    Text("Edit Details", fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        // STEP 3 ── Confirm alert
                        PayDialogStep.CONFIRM -> {
                            Column(
                                modifier            = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Surface(
                                    shape    = CircleShape,
                                    color    = FintechColors.NavyDark.copy(alpha = 0.1f),
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(Icons.Default.HelpOutline, null,
                                            tint     = FintechColors.NavyDark,
                                            modifier = Modifier.size(34.dp))
                                    }
                                }
                                Text("Confirm Transfer?",
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = FintechColors.NavyDark,
                                    textAlign  = TextAlign.Center)
                                Text("You are about to transfer ₹$amount to ${bank.name} via ${selectedMode.label}. This action cannot be undone.",
                                    style     = MaterialTheme.typography.bodySmall,
                                    color     = MaterialTheme.colorScheme.outline,
                                    textAlign = TextAlign.Center)

                                Row(
                                    modifier              = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick  = { step = PayDialogStep.PREVIEW },
                                        modifier = Modifier.weight(1f).height(44.dp),
                                        shape    = RoundedCornerShape(10.dp),
                                        colors   = ButtonDefaults.outlinedButtonColors(
                                            contentColor = FintechColors.NavyDark),
                                        border   = androidx.compose.foundation.BorderStroke(
                                            1.dp, FintechColors.NavyDark.copy(alpha = 0.4f))
                                    ) { Text("Cancel") }

                                    Button(
                                        onClick  = {
                                            step = PayDialogStep.PROCESSING
                                            onPaySubmit(amount, selectedMode) { success, msg ->
                                                isSuccess = success
                                                resultMessage = msg
                                                step = PayDialogStep.RESULT
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(44.dp),
                                        shape    = RoundedCornerShape(10.dp),
                                        colors   = ButtonDefaults.buttonColors(
                                            containerColor = FintechColors.NavyDark)
                                    ) {
                                        Text("OK, Transfer",
                                            color      = Color.White,
                                            fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                        }

                        // STEP 4 ── Processing
                        PayDialogStep.PROCESSING -> {
                            Column(
                                modifier            = Modifier.padding(40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                CircularProgressIndicator(
                                    color       = FintechColors.NavyDark,
                                    modifier    = Modifier.size(56.dp),
                                    strokeWidth = 4.dp
                                )
                                Text("Processing Transfer...",
                                    style      = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color      = FintechColors.NavyDark,
                                    textAlign  = TextAlign.Center)
                                Text("Please do not close this window",
                                    style     = MaterialTheme.typography.bodySmall,
                                    color     = MaterialTheme.colorScheme.outline,
                                    textAlign = TextAlign.Center)
                            }
                        }

                        // STEP 5 ── Result
                        PayDialogStep.RESULT -> {
                            TransferResultStep(
                                isSuccess = isSuccess,
                                bank      = bank,
                                amount    = amount,
                                mode      = selectedMode,
                                txnRef    = txnRef,
                                onDone    = onDismiss
                            )
                        }
                    }
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
// DIALOG STEP DOTS INDICATOR
// ─────────────────────────────────────────────

@Composable
fun DialogStepDots(current: PayDialogStep) {
    val steps = listOf(PayDialogStep.AMOUNT_MODE, PayDialogStep.PREVIEW, PayDialogStep.CONFIRM)
    Row(
        modifier              = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isActive = current == step
            val isDone   = steps.indexOf(current) > index
            Box(
                modifier = Modifier
                    .size(if (isActive) 24.dp else 8.dp, 8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        when {
                            isDone   -> FintechColors.SuccessGreen
                            isActive -> FintechColors.NavyDark
                            else     -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        }
                    )
            )
            if (index < steps.lastIndex) Spacer(Modifier.width(6.dp))
        }
    }
}


// ─────────────────────────────────────────────
// DIALOG SUB-COMPONENTS
// ─────────────────────────────────────────────

@Composable
fun DialogRecipientStrip(bank: MtbBankData) {
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
            Surface(shape = CircleShape,
                color    = FintechColors.NavyDark.copy(alpha = 0.12f),
                modifier = Modifier.size(38.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(bank.name!!,
                        color = FintechColors.NavyDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(bank.name!!,
                    style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold,
                    color = FintechColors.NavyDark)
                Text("${bank.bankName}  •  ${bank.accountNo}",
                    style = MaterialTheme.typography.labelSmall, color = colorScheme.outline)
            }
        }
    }
}

@Composable
fun TransferModeCard(
    mode:       TransferMode,
    isSelected: Boolean,
    onClick:    () -> Unit
) {
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = if (isSelected) FintechColors.NavyDark.copy(alpha = 0.07f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) FintechColors.NavyDark else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick  = onClick,
                colors   = RadioButtonDefaults.colors(
                    selectedColor   = FintechColors.NavyDark,
                    unselectedColor = MaterialTheme.colorScheme.outline
                )
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(mode.label,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color      = if (isSelected) FintechColors.NavyDark
                    else MaterialTheme.colorScheme.onSurface)
                Text(mode.desc,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline)
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, null,
                    tint = FintechColors.NavyDark, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun TransferPreviewCard(
    bank:   MtbBankData,
    amount: String,
    mode:   TransferMode,
    txnRef: String
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

        // Big amount display
        Surface(
            shape    = RoundedCornerShape(14.dp),
            color    = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(FintechColors.NavyDark, FintechColors.NavyLight)),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Transfer Amount",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelMedium)
                    Text("₹$amount",
                        color      = Color.White,
                        style      = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold)
                    Text("via ${mode.label}",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Detail table
        Surface(
            shape    = RoundedCornerShape(12.dp),
            color    = colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Icons.Default.Person        to ("To"       to bank.name),
                    Icons.Default.CreditCard    to ("Account"  to bank.accountNo),
                    Icons.Default.AccountBalance to ("Bank"    to bank.bankName),
                    Icons.Default.Numbers       to ("IFSC"     to bank.ifscCode),
                    Icons.Default.SwapHoriz     to ("Mode"     to mode.label),
                    Icons.Default.Tag           to ("Ref No"   to txnRef),
                ).forEach { (icon, pair) ->
                    val (label, value) = pair
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(icon, null,
                            tint     = FintechColors.NavyDark.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp))
                        Text(label,
                            style    = MaterialTheme.typography.labelSmall,
                            color    = colorScheme.outline,
                            modifier = Modifier.width(60.dp))
                        Text(value!!,
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color      = colorScheme.onSurface,
                            modifier   = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun TransferResultStep(
    isSuccess: Boolean,
    bank:      MtbBankData,
    amount:    String,
    mode:      TransferMode,
    txnRef:    String,
    onDone:    () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }

    val scale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "result_scale"
    )

    Column(
        modifier            = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape    = CircleShape,
            color    = if (isSuccess) FintechColors.SuccessGreenLight
            else MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.size(72.dp).scale(scale)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    null,
                    tint     = if (isSuccess) FintechColors.SuccessGreen
                    else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Text(
            if (isSuccess) "Transfer Successful!" else "Transfer Failed",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = if (isSuccess) FintechColors.SuccessGreenDark
            else MaterialTheme.colorScheme.error,
            textAlign  = TextAlign.Center
        )

        if (isSuccess) {
            Text("₹$amount transferred to ${bank.name} via ${mode.label}",
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center)

            Surface(
                shape    = RoundedCornerShape(10.dp),
                color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier              = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Txn Reference",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline)
                    Text(txnRef,
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color      = FintechColors.NavyDark)
                }
            }
        } else {
            Text("Your transfer could not be processed. No amount was deducted.",
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center)
        }

        NavyPrimaryButton(
            text    = "Done",
            onClick = onDone,
            icon    = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Refresh
        )
        Spacer(Modifier.height(4.dp))
    }
}


// ─────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────

