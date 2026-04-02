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
import com.example.modernui.Api.LoginData
import com.example.modernui.Api.MyUserData
import com.example.modernui.Api.UserResponse
import com.example.modernui.ui.screens.login.UiState
import com.example.modernui.ui.screens.login.UserViewModel
import com.example.modernui.ui.theme.AppColors

@Composable
fun HistoryScreen(
    viewModel: UserViewModel = hiltViewModel(),
    onMenuClick: () -> Unit = {}
) {
    val uiState by viewModel.state.collectAsState()
    HistoryContent(uiState = uiState, onMenuClick = onMenuClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryContent(
    uiState: UiState,
    onMenuClick: () -> Unit = {}
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
        TxItem("Grocery Store",    "Oct 24, 2023", "-₹1,200.00",  false, "Food"),
        TxItem("Netflix",          "Oct 20, 2023", "-₹649.00",    false, "Entertainment"),
        TxItem("Freelance Payment","Oct 18, 2023", "+₹8,500.00",  true,  "Income"),
        TxItem("Electricity Bill", "Oct 15, 2023", "-₹1,840.00",  false, "Utilities"),
        TxItem("Uber Ride",        "Oct 14, 2023", "-₹320.00",    false, "Transport"),
        TxItem("Amazon Order",     "Oct 12, 2023", "-₹2,399.00",  false, "Shopping"),
        TxItem("Dividend Credit",  "Oct 10, 2023", "+₹1,200.00",  true,  "Investment"),
        TxItem("Rent",             "Oct 01, 2023", "-₹15,000.00", false, "Housing"),
        TxItem("Interest Credit",  "Sep 30, 2023", "+₹220.00",    true,  "Savings")
    )

    val filterOptions = listOf("All", "Credits", "Debits")
    var selectedFilter by remember { mutableIntStateOf(0) }

    val filtered = when (selectedFilter) {
        1 -> transactions.filter { it.isCredit }
        2 -> transactions.filter { !it.isCredit }
        else -> transactions
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .background(AppColors.NavyAlpha)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
            }
            Text(
                text = "History",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {}) {
                Icon(Icons.Default.Notifications, "Notifications", tint = Color.White)
            }
        }

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterOptions.forEachIndexed { index, label ->
                FilterChip(
                    selected = selectedFilter == index,
                    onClick = { selectedFilter = index },
                    label = { Text(label) }
                )
            }
        }

        HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(filtered) { tx ->
                ListItem(
                    headlineContent = {
                        Text(tx.title, fontWeight = FontWeight.Medium)
                    },
                    supportingContent = {
                        Column {
                            Text(tx.date, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(2.dp))
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        tx.category,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.height(22.dp)
                            )
                        }
                    },
                    trailingContent = {
                        Text(
                            tx.amount,
                            color = if (tx.isCredit) colorScheme.primary else colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingContent = {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (tx.isCredit)
                                colorScheme.primaryContainer
                            else
                                colorScheme.secondaryContainer
                        ) {
                            Icon(
                                imageVector = if (tx.isCredit)
                                    Icons.Default.ArrowDownward
                                else
                                    Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (tx.isCredit)
                                    colorScheme.primary
                                else
                                    colorScheme.error,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                )
                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
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
