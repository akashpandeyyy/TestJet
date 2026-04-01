package com.example.modernui.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.modernui.ui.components.*
import com.example.modernui.ui.theme.FintechColors


@Composable
fun AepsScreen(
    onBackClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    // ── Form state ────────────────────────────
    var aadhaarNumber   by remember { mutableStateOf("") }
    var mobileNumber    by remember { mutableStateOf("") }
    var amount          by remember { mutableStateOf("") }
    var selectedBank    by remember { mutableStateOf("") }
    var selectedTxnType by remember { mutableStateOf("") }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        // ── Top bar ───────────────────────────
        DetailTopBar(title = "AEPS", onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

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
                        onValueChange = { if (it.all(Char::isDigit)) mobileNumber = it },
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
                        onOptionSelected = { selectedTxnType = it }
                    )

                    NavyDropdownField(
                        label            = "Bank Name *",
                        leadingIcon      = Icons.Default.AccountBalance,
                        selectedValue    = selectedBank,
                        options          = banks,
                        onOptionSelected = { selectedBank = it }
                    )

                    // Amount — only for Withdrawal / Deposit
                    if (needsAmount) {
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

                        // Quick amount chips
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("500", "1000", "2000", "5000").forEach { preset ->
                                FilterChip(
                                    selected = amount == preset,
                                    onClick  = { amount = preset },
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
                    Icon(
                        Icons.Default.Fingerprint, null,
                        tint     = FintechColors.SuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
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

            // ── Submit button ─────────────────
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
// PREVIEWS
// ─────────────────────────────────────────────

@Preview(name = "AEPS – Light", showBackground = true)
@Preview(name = "AEPS – Dark",  showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewAepsScreen() {
    MaterialTheme { AepsScreen() }
}