package com.example.modernui.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modernui.ui.screens.login.UserViewModel
import com.example.modernui.ui.theme.AppColors

@Composable
fun WalletScreen(
    viewModel: UserViewModel,
    onMenuClick: () -> Unit = {}
) {
    val uiState by viewModel.state.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

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
                text = "Wallet",
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

        // --- CONTENT ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Total Balance", style = MaterialTheme.typography.labelMedium)
                    Text("₹24,500.00", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("Send") }
                        FilledTonalButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("Receive") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Recent Transactions", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            repeat(5) {
                ListItem(
                    headlineContent = { Text("Grocery Store") },
                    supportingContent = { Text("Oct 24, 2023") },
                    trailingContent = { Text("-₹45.00", color = colorScheme.error) },
                    leadingContent = {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colorScheme.secondaryContainer
                        ) {
                            Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.padding(8.dp))
                        }
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}
