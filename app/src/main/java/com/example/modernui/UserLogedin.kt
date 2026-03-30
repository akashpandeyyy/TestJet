package com.example.modernui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.modernui.Api.LoginData
import com.example.modernui.Api.MyUserData
import com.example.modernui.Api.UserResponse

@Composable
fun UserDetailScreenM3(
    viewModel: UserViewModel,
    onBackClick: () -> Unit,
    onContinueToDashboard: () -> Unit
) {
    val uiState by viewModel.state.collectAsState()

    when (val state = uiState) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is UiState.Success -> {
            UserDetailContent(
                response = state.userResponse,
                onBackClick = onBackClick,
                onContinueToDashboard = onContinueToDashboard
            )
        }
        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBackClick) {
                        Text("Go Back")
                    }
                }
            }
        }
        else -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No session found. Please login again.")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailContent(
    response: UserResponse,
    onBackClick: () -> Unit,
    onContinueToDashboard: () -> Unit
) {
    val userData = response.data?.userData ?: return
    val colorScheme = MaterialTheme.colorScheme



    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Account Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Profile Circle
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = colorScheme.primaryContainer,
                tonalElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = userData.name?.take(1)?.uppercase() ?: "?",
                        style = MaterialTheme.typography.displayMedium,
                        color = colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = userData.name ?: "Unknown User",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = colorScheme.onSurface
            )

            Surface(
                color = colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = if (userData.roleId != null) "#Level-${userData.roleId}" else "Member",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    InfoRowItem(icon = Icons.Default.LocationOn, label = "Address", value = userData.address ?: "Not Provided")
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                    InfoRowItem(icon = Icons.Default.Phone, label = "Mobile Number", value = userData.mobile ?: "Not Provided")
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                    InfoRowItem(icon = Icons.Default.VerifiedUser, label = "User ID", value = "#SM-${userData.userId ?: "0"}")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onContinueToDashboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Proceed to DashBoard", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(10.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun InfoRowItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "User Detail Preview")
@Composable
fun UserDetailPreview() {
    val fakeUser = MyUserData(
        id = 101,
        name = "Rahul Sharma",
        address = "rahul.sharma@fintech.com",
        phone = "+91 98765 43210",
        role = "Premium Member",
        email = "rahul.sharma@fintech.com",
        userId ="12345",
        mobile = "9876543210",
        roleId = "1"
    )

    val fakeResponse = UserResponse(
        status = 200,
        message = "Success",
        errorMessage = null,
        data = LoginData(token = "sample_token_123", userData = fakeUser)
    )

    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            UserDetailContent(
                response = fakeResponse,
                onBackClick = {},
                onContinueToDashboard = {}
            )
        }
    }
}
