package com.example.modernui.ui.screens.aeps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modernui.Api.model.BankItem
import com.example.modernui.ui.theme.FintechColors
import androidx.compose.ui.tooling.preview.Preview
import com.example.modernui.ui.theme.ModernUITheme

// ══════════════════════════════════════════════════════════
//  BANK SELECTION SHEET
//
//  Opens as a ModalBottomSheet.
//  Shows a searchable list of banks with name + IIN.
//  Tapping a bank returns it via [onBankSelected] and closes.
//
//  Usage:
//    BankSelectionSheet(
//        banks          = banks,           // List<BankItem> from ViewModel
//        selectedBank   = selectedBank,    // BankItem? currently selected
//        onBankSelected = { bank ->
//            viewModel.onBankSelected(bank)
//        },
//        onDismiss      = { showBankSheet = false }
//    )
// ══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankSelectionSheet(
    banks:          List<BankItem>,
    selectedBank:   BankItem?,
    onBankSelected: (BankItem) -> Unit,
    onDismiss:      () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor   = MaterialTheme.colorScheme.surface
    ) {
        BankSelectionSheetContent(
            banks          = banks,
            selectedBank   = selectedBank,
            onBankSelected = onBankSelected,
            onDismiss      = onDismiss
        )
    }
}

@Composable
fun BankSelectionSheetContent(
    banks:          List<BankItem>,
    selectedBank:   BankItem?,
    onBankSelected: (BankItem) -> Unit,
    onDismiss:      () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(searchQuery, banks) {
        if (searchQuery.isBlank()) banks
        else banks.filter {
            (it.bankname ?: "").contains(searchQuery, ignoreCase = true) ||
                    (it.iin ?: "").contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        // ── Sheet header ──────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(FintechColors.NavyDark, FintechColors.NavyLight)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.AccountBalance, null,
                        tint     = Color.White,
                        modifier = Modifier.size(22.dp))
                    Column {
                        Text("Select Bank",
                            color      = Color.White,
                            fontWeight = FontWeight.Bold,
                            style      = MaterialTheme.typography.titleMedium)
                        Text("${banks.size} banks available",
                            color = Color.White.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null,
                        tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }

        // ── Search bar ────────────────────
        OutlinedTextField(
            value         = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder   = { Text("Search by bank name or IIN") },
            leadingIcon   = { Icon(Icons.Default.Search, null,
                tint = FintechColors.NavyDark) },
            trailingIcon  = if (searchQuery.isNotEmpty()) ({
                IconButton(onClick = { searchQuery = "" }) {
                    Icon(Icons.Default.Clear, null,
                        tint = MaterialTheme.colorScheme.outline)
                }
            }) else null,
            singleLine    = true,
            shape         = RoundedCornerShape(12.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = FintechColors.NavyDark,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                focusedLabelColor    = FintechColors.NavyDark
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )

        // ── Empty state ───────────────────
        if (filtered.isEmpty()) {
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.SearchOff, null,
                        tint     = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(40.dp))
                    Text("No banks found for \"$searchQuery\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            // ── Bank list ─────────────────
            LazyColumn(
                modifier            = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                contentPadding      = PaddingValues(
                    start  = 16.dp,
                    end    = 16.dp,
                    bottom = 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Currently selected — pinned at top
                selectedBank?.let { selected ->
                    // Use a unique key with a prefix to avoid collisions
                    item(key = "selected_${selected.bankid ?: selected.iin ?: selected.id ?: selected.hashCode()}") {
                        BankListItem(
                            bank       = selected,
                            isSelected = true,
                            isPinned   = true,
                            onClick    = { onBankSelected(selected); onDismiss() }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )
                    }
                }

                items(
                    items = filtered.filter { it != selectedBank },
                    // Use a composite key to ensure uniqueness even if IINs or other fields are duplicated
                    key   = { bank -> "list_${bank.bankid ?: ""}_${bank.iin ?: ""}_${bank.id ?: ""}_${bank.bankname ?: ""}" }
                ) { bank ->
                    BankListItem(
                        bank       = bank,
                        isSelected = false,
                        isPinned   = false,
                        onClick    = { onBankSelected(bank); onDismiss() }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
// BANK LIST ITEM
// ─────────────────────────────────────────────

@Composable
fun BankListItem(
    bank:       BankItem,
    isSelected: Boolean,
    isPinned:   Boolean,
    onClick:    () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val bankName    = bank.bankname ?: "Unknown Bank"
    val iin         = bank.iin ?: "—"
    val initials    = bankName.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifEmpty { "??" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) FintechColors.NavyDark.copy(alpha = 0.06f)
                else Color.Transparent
            )
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        Surface(
            shape    = CircleShape,
            color    = if (isSelected)
                FintechColors.NavyDark.copy(alpha = 0.12f)
            else
                colorScheme.surfaceVariant,
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    initials,
                    color      = if (isSelected) FintechColors.NavyDark
                    else colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 13.sp
                )
            }
        }

        // Name + IIN
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    bankName,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color      = if (isSelected) FintechColors.NavyDark
                    else colorScheme.onSurface
                )
                if (isPinned) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = FintechColors.NavyDark.copy(alpha = 0.1f)
                    ) {
                        Text("Selected",
                            modifier   = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            style      = MaterialTheme.typography.labelSmall,
                            color      = FintechColors.NavyDark,
                            fontSize   = 9.sp,
                            fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text("IIN: $iin",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.outline)
        }

        // Checkmark when selected
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
        } else {
            Icon(Icons.Default.ChevronRight, null,
                tint     = colorScheme.outline,
                modifier = Modifier.size(18.dp))
        }
    }
}


// ─────────────────────────────────────────────
// SELECTED BANK DISPLAY ROW
// Shown in the form after a bank is picked
// ─────────────────────────────────────────────

@Composable
fun SelectedBankRow(
    bank:     BankItem,
    onClick:  () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val bankName    = bank.bankname ?: "Unknown Bank"
    val iin         = bank.iin ?: "—"

    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = FintechColors.NavyDark.copy(alpha = 0.06f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
                    Icon(Icons.Default.AccountBalance, null,
                        tint     = FintechColors.NavyDark,
                        modifier = Modifier.size(20.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(bankName,
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color      = FintechColors.NavyDark)
                Text("IIN: $iin",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.outline)
            }
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Change",
                    style      = MaterialTheme.typography.labelSmall,
                    color      = FintechColors.NavyDark,
                    fontWeight = FontWeight.Bold)
                Icon(Icons.Default.SwapHoriz, null,
                    tint     = FintechColors.NavyDark,
                    modifier = Modifier.size(14.dp))
            }
        }
    }
}

private val sampleBanks = listOf(
    BankItem(id = 1, bankname = "State Bank of India", bankid = "1", iin = "607094"),
    BankItem(id = 2, bankname = "HDFC Bank", bankid = "2", iin = "607152"),
    BankItem(id = 3, bankname = "ICICI Bank", bankid = "3", iin = "508534"),
    BankItem(id = 4, bankname = "Axis Bank", bankid = "4", iin = "607153"),
    BankItem(id = 5, bankname = "Punjab National Bank", bankid = "5", iin = "607027")
)

@Preview(showBackground = true)
@Composable
fun PreviewBankSelectionSheet() {
    ModernUITheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            BankSelectionSheetContent(
                banks = sampleBanks,
                selectedBank = sampleBanks[0],
                onBankSelected = {},
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBankListItem() {
    ModernUITheme {
        Column(modifier = Modifier.padding(16.dp)) {
            BankListItem(
                bank = sampleBanks[0],
                isSelected = true,
                isPinned = true,
                onClick = {}
            )
            BankListItem(
                bank = sampleBanks[1],
                isSelected = false,
                isPinned = false,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSelectedBankRow() {
    ModernUITheme {
        Box(modifier = Modifier.padding(16.dp)) {
            SelectedBankRow(
                bank = sampleBanks[0],
                onClick = {}
            )
        }
    }
}
