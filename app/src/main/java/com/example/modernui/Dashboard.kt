package com.example.modernui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.modernui.Api.LoginData
import com.example.modernui.Api.MyUserData
import com.example.modernui.Api.UserResponse


@Composable
fun FintechDashboardM3(
    viewModel: UserViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    FintechDashboardContent(uiState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FintechDashboardContent(uiState: UiState) {
    val colorScheme = MaterialTheme.colorScheme

    // Safely extract user data from the Success state
    val response = (uiState as? UiState.Success)?.userResponse
    val userData = response?.data?.userData

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Welcome, ${userData?.name ?: "User"}", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.Notifications, "Notifications") }
                    IconButton(onClick = { }) { Icon(Icons.Default.Report, "Profile") }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = colorScheme.surface,
                    titleContentColor = colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, null) },
                    label = { Text("Wallet") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Default.FileCopy, null) },
                    label = { Text("Report") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Default.AccountCircle, null) },
                    label = { Text("Profile") }
                )
            }
        }
//        bottomBar = {
//            Row(
//                modifier = Modifier.horizontalScroll(rememberScrollState())
//            ) {
//                NavigationBarItem(
//                    selected = true,
//                    onClick = { },
//                    icon = { Icon(Icons.Default.Home, null) },
//                    label = { Text("Home") }
//                )
//                NavigationBarItem(
//                    selected = false,
//                    onClick = { },
//                    icon = { Icon(Icons.Default.AccountBalanceWallet, null) },
//                    label = { Text("Wallet") }
//                )
//                NavigationBarItem(
//                    selected = false,
//                    onClick = { },
//                    icon = { Icon(Icons.Default.FileCopy, null) },
//                    label = { Text("Report") }
//                )
//                NavigationBarItem(
//                    selected = false,
//                    onClick = { },
//                    icon = { Icon(Icons.Default.AccountCircle, null) },
//                    label = { Text("Profile") }
//                )
//            }
//        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
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

            repeat(3) {
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

// Preview functions
@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewFintechDashboardM3() {
    // Create dummy data that matches the project's real classes
    val dummyUserData = MyUserData(
        userId = "1",
        name = "Jane Doe",
        email = "jane@example.com",
        mobile = null,
        roleId = null,
        address = null,
        id = 1,
        phone = null,
        role = null
    )
    val dummyLoginData = LoginData(token = "mock_token", userData = dummyUserData)
    val dummyUserResponse = UserResponse(
        status = 1,
        message = "Success",
        errorMessage = null,
        data = dummyLoginData
    )
    val dummyUiState = UiState.Success(userResponse = dummyUserResponse)

    MaterialTheme {
        FintechDashboardContent(uiState = dummyUiState)
    }
}
