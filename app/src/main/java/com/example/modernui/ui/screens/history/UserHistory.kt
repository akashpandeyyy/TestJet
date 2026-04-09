package com.example.modernui.ui.screens.history

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.modernui.Api.model.LoginData
import com.example.modernui.Api.model.MyUserData
import com.example.modernui.Api.model.UserResponse
import com.example.modernui.ui.screens.login.UiState
import com.example.modernui.ui.screens.login.UserViewModel
import com.example.modernui.ui.theme.AppColors

@Composable
fun HistoryScreen(
    viewModel: UserViewModel = hiltViewModel(),
    onMenuClick: () -> Unit = {},
    initialCategoryFilter: String? = null
) {
    val uiState by viewModel.state.collectAsState()
    HistoryContent(
        uiState = uiState, 
        onMenuClick = onMenuClick,
        initialCategoryFilter = initialCategoryFilter
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryContent(
    uiState: UiState,
    onMenuClick: () -> Unit = {},
    initialCategoryFilter: String? = null
) {
    val colorScheme = MaterialTheme.colorScheme

    data class TxItem(
        val title: String,
        val date: String,
        val amount: String,
        val isCredit: Boolean,
        val category: String
    )

    val transactions = listOf(
        TxItem("Salary Credit",    "Oct 22, 2023", "+₹50,000.00", true,  "Income"),
        TxItem("Mobile Recharge",  "Oct 25, 2023", "-₹499.00",    false, "Recharge"),
        TxItem("Grocery Store",    "Oct 24, 2023", "-₹1,200.00",  false, "Food"),
        TxItem("DTH Recharge",     "Oct 23, 2023", "-₹350.00",    false, "Recharge"),
        TxItem("Netflix",          "Oct 20, 2023", "-₹649.00",    false, "Entertainment"),
        TxItem("Freelance Payment","Oct 18, 2023", "+₹8,500.00",  true,  "Income"),
        TxItem("Electricity Bill", "Oct 15, 2023", "-₹1,840.00",  false, "Utilities"),
        TxItem("Uber Ride",        "Oct 14, 2023", "-₹320.00",    false, "Transport"),
        TxItem("Amazon Order",     "Oct 12, 2023", "-₹2,399.00",  false, "Shopping"),
        TxItem("Dividend Credit",  "Oct 10, 2023", "+₹1,200.00",  true,  "Investment"),
        TxItem("Rent",             "Oct 01, 2023", "-₹15,000.00", false, "Housing"),
        TxItem("Interest Credit",  "Sep 30, 2023", "+₹220.00",    true,  "Savings")
    )

    val filterOptions = listOf("All", "Credits", "Debits", "Recharge", "DMT", "AEPS")
    var selectedFilter by remember { mutableStateOf(initialCategoryFilter ?: "All") }

    // If initialCategoryFilter changes, update selectedFilter
    LaunchedEffect(initialCategoryFilter) {
        if (initialCategoryFilter != null) {
            selectedFilter = initialCategoryFilter
        }
    }

    val filtered = when (selectedFilter) {
        "Credits" -> transactions.filter { it.isCredit }
        "Debits"  -> transactions.filter { !it.isCredit }
        "All"     -> transactions
        else      -> transactions.filter { it.category.equals(selectedFilter, ignoreCase = true) }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(AppColors.NavyAlpha)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
            }
            Text(
                text = "History",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp)
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {}) {
                Icon(Icons.Default.Notifications, "Notifications", tint = Color.White)
            }
        }

        // Filter chips - Scrollable
        ScrollableTabRow(
            selectedTabIndex = filterOptions.indexOf(selectedFilter).coerceAtLeast(0),
            edgePadding = 16.dp,
            containerColor = Color.Transparent,
            divider = {},
            indicator = {},
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            filterOptions.forEach { label ->
                FilterChip(
                    selected = selectedFilter == label,
                    onClick = { selectedFilter = label },
                    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.3f))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(filtered) { tx ->
                ListItem(
                    modifier = Modifier.fillMaxWidth(),
                    headlineContent = {
                        Text(tx.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                    },
                    supportingContent = {
                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            Text(tx.date, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = tx.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    color = colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    },
                    trailingContent = {
                        Text(
                            tx.amount,
                            color = if (tx.isCredit) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    leadingContent = {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (tx.isCredit)
                                Color(0xFFE8F5E9)
                            else
                                Color(0xFFFFEBEE)
                        ) {
                            Icon(
                                imageVector = if (tx.isCredit)
                                    Icons.Default.ArrowDownward
                                else
                                    Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (tx.isCredit)
                                    Color(0xFF2E7D32)
                                else
                                    Color(0xFFD32F2F),
                                modifier = Modifier.padding(10.dp).size(24.dp)
                            )
                        }
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Preview(name = "History – Light", showBackground = true)
@Preview(name = "History – Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewHistoryContent() {
    val dummyUiState = UiState.Success(
        userResponse = UserResponse(
            status = 1,
            message = "Success",
            errorMessage = null,
            data = LoginData(
                token = "mock_token",
                userData = MyUserData(
                    userId = "1", name = "Jane Doe", email = "jane@example.com",
                    mobile = null, roleId = null, address = null,
                    id = 1, phone = null, role = null
                )
            )
        )
    )
    MaterialTheme { HistoryContent(uiState = dummyUiState) }
}
