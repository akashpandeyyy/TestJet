package com.example.modernui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WalletScreen(viewModel: UserViewModel) {
    val uiState by viewModel.state.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
