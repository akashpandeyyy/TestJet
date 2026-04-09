package com.example.modernui.ui.screens.common

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import android.hardware.usb.UsbManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modernui.ui.components.intentpackage.RdHelper
import com.example.modernui.ui.theme.FintechColors
import com.example.modernui.ui.theme.ModernUITheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────
// CONFIGURATION DATA CLASS
// ─────────────────────────────────────────────

enum class TwoFaStep { 
    OTP, 
    BIOMETRIC, 
    FACE_VERIFICATION 
}

data class TwoFaConfig(
    val title:         String        = "Two-Factor Authentication",
    val subtitle:      String        = "Verify your identity to continue",
    val serviceName:   String        = "this service",
    val serviceIcon:   ImageVector   = Icons.Default.Security,
    val serviceColor:  Color         = FintechColors.NavyDark,
    val mobile:        String        = "",
    val steps:         List<TwoFaStep> = listOf(TwoFaStep.FACE_VERIFICATION),
    val selectedDevice: String        = "face" // Added to support multiple devices from RdHelper
)

// ─────────────────────────────────────────────
// INTERNAL STATE MACHINE
// ─────────────────────────────────────────────

enum class TwoFaState {
    IDLE,
    VERIFYING,
    FAILED,
    ALL_DONE
}

// ─────────────────────────────────────────────
// ROOT — UNIVERSAL 2FA SCREEN
// ─────────────────────────────────────────────

@Composable
fun TwoFactorAuthScreen(
    config:      TwoFaConfig  = TwoFaConfig(),
    onVerified:  () -> Unit   = {},
    onBackClick: () -> Unit   = {},
    viewModel:   TwoFaViewModel = hiltViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    
    val uiState by viewModel.uiState.collectAsState()
    val selectedDeviceId by viewModel.selectedDeviceId.collectAsState()

    // Listen for USB device connection
    DisposableEffect(Unit) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context, intent: android.content.Intent) {
                if (UsbManager.ACTION_USB_DEVICE_ATTACHED == intent.action) {
                    Toast.makeText(context, "Biometric Device Connected", Toast.LENGTH_SHORT).show()
                }
            }
        }
        val filter = android.content.IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        context.registerReceiver(receiver, filter)
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    // Launcher to handle the RD service intent result in the same activity
//    val faceCaptureLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        val outputKey = RdHelper.getOutputKey(selectedDeviceId)
//        val responseData = result.data?.getStringExtra("response") ?: ""
//
//        Log.d("TwoFaScreen", "RD Service Result: Code=${result.resultCode}, Key=$outputKey, DataLength=${responseData.length}")
//
//        if (result.resultCode == Activity.RESULT_OK) {
//            if (responseData.isNotEmpty()) {
//                viewModel.handleRdServiceResult(responseData)
//            } else {
//                // Some devices might put data in different extras or as a URI, but PID_DATA is standard for Biometric
//                // and "response" for UIDAI Face RD.
//                Toast.makeText(context, "Device returned empty data ($outputKey)", Toast.LENGTH_SHORT).show()
//                viewModel.resetState()
//            }
//        } else {
//            viewModel.resetState()
//            Toast.makeText(context, "Verification Cancelled", Toast.LENGTH_SHORT).show()
//        }
//    }
//    val faceCaptureLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        // Yahan check karo ki result.data null toh nahi hai
//        val dataIntent = result.data
//
//        // Sabse safe tareeka: Dono keys check kar lo
//        val responseData = dataIntent?.getStringExtra("PID_DATA")
//            ?: dataIntent?.getStringExtra("response")
//            ?: ""
//
//        Log.d("TwoFaScreen", "Final Response Data: $responseData")
//
//        if (result.resultCode == Activity.RESULT_OK && responseData.isNotEmpty()) {
//            viewModel.handleRdServiceResult(responseData)
//        } else {
//            viewModel.resetState()
//            Toast.makeText(context, "Data not captured or device busy", Toast.LENGTH_SHORT).show()
//        }
//    }
    val faceCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val dataIntent = result.data

        // 1. Dynamic Key nikalna: selectedDeviceId ke basis par "response" ya "PID_DATA"
        val outputKey = RdHelper.getOutputKey(selectedDeviceId)

        // 2. Data Fetching (Serialization): Sabhi scenarios handle karne ke liye
        val responseData = dataIntent?.getStringExtra(outputKey) ?: ""

        Log.d("TwoFaScreen", "Capture Result: Code=${result.resultCode}, KeyUsed=$outputKey, DataFound=${responseData.isNotEmpty()}")

        if (result.resultCode == Activity.RESULT_OK) {
            if (responseData.isNotEmpty()) {
                // Biometric captured, ab ViewModel is XML ko parse/process karega
                viewModel.handleRdServiceResult(responseData)
            } else {
                Toast.makeText(context, "Device returned empty data ($outputKey)", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
        } else {
            viewModel.resetState()
            Toast.makeText(context, "Verification Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is TwoFaUiState.Success) {
            delay(800)
            onVerified()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {

        // ── Top bar ───────────────────────────
        TwoFaTopBar(
            title       = config.title,
            onBackClick = onBackClick,
            config      = config,
            allDone     = uiState is TwoFaUiState.Success
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Service context card ──────────
            ServiceContextCard(config = config)

            // ── Device Selection ──────────────
            DeviceSelectionCard(
                selectedId = selectedDeviceId,
                onDeviceSelected = { id ->
                    viewModel.onDeviceSelected(id)
                    val deviceName = RdHelper.SUPPORTED_DEVICES.find { it.id == id }?.name ?: "Device"
                    Toast.makeText(context, "$deviceName selected", Toast.LENGTH_SHORT).show()
                }
            )

            // ── Step content ──────────────────
            AnimatedContent(
                targetState    = uiState,
                transitionSpec = {
                    fadeIn(tween(250)) togetherWith fadeOut(tween(200))
                },
                label = "twofa_content"
            ) { state ->
                when (state) {
                    is TwoFaUiState.Idle -> {
                        val currentDevice = RdHelper.SUPPORTED_DEVICES.find { it.id == selectedDeviceId } 
                            ?: RdHelper.SUPPORTED_DEVICES[0]

                        FaceVerificationPromptCard(
                            label = currentDevice.name,
                            icon = currentDevice.icon,


                            onProceed = {
                                val packageName = RdHelper.getPackage(selectedDeviceId)
                                val action = RdHelper.getAction(selectedDeviceId)
                                val inputKey = RdHelper.getInputKey(selectedDeviceId)

                                val pidType = when(selectedDeviceId) {
                                    "mfs110", "morpho_l1" -> RdHelper.fingType
                                    "mantra_mis100v2" -> RdHelper.irisType
                                    else -> RdHelper.faceType
                                }

                                val pidOptionsXml = RdHelper.makePidXm(selectedDeviceId, pidType)

                                try {
                                    val intent = Intent(action).apply {
                                        setPackage(packageName)
                                        putExtra(inputKey, pidOptionsXml)
                                    }
                                    Log.d("TwoFaScreen", "Launching RD: Device=$selectedDeviceId | Package=$packageName | Key=$inputKey")
                                    faceCaptureLauncher.launch(intent)
                                } catch (e: ActivityNotFoundException) {
                                    Log.e("TwoFaScreen", "RD Service not found: $packageName")
                                    Toast.makeText(context, "RD Service not installed for $selectedDeviceId", Toast.LENGTH_LONG).show()
                                    try {
                                        val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                                        playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(playStoreIntent)
                                    } catch (ex: Exception) {
                                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                                        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(webIntent)
                                    }
                                } catch (e: Exception) {
                                    Log.e("TwoFaScreen", "Error launching RD: ${e.message}")
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }


                        )
                    }

                    is TwoFaUiState.Verifying -> {
                        LoadingCard(message = "Processing Verification...")
                    }

                    is TwoFaUiState.Error -> {
                        FailedCard(
                            message    = state.message,
                            onRetry    = { viewModel.resetState() }
                        )
                    }

                    is TwoFaUiState.Success -> {
                        AllDoneCard(config = config)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ─────────────────────────────────────────────
// DEVICE SELECTION CARD
// ─────────────────────────────────────────────

@Composable
fun DeviceSelectionCard(
    selectedId: String,
    onDeviceSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Select Verification Device",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RdHelper.SUPPORTED_DEVICES.forEach { device ->
                    val isSelected = device.id == selectedId
                    val bgColor = if (isSelected) FintechColors.NavyDark else Color.Transparent
                    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .clickable { onDeviceSelected(device.id) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = device.icon,
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = device.name.split(" ").first(),
                                style = MaterialTheme.typography.labelSmall,
                                color = contentColor,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// FACE VERIFICATION PROMPT CARD
// ─────────────────────────────────────────────

@Composable
fun FaceVerificationPromptCard(
    label: String,
    icon: ImageVector,
    onProceed: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FintechColors.NavyDark,
                modifier = Modifier.size(64.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Confirm your identity using $label to proceed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick  = onProceed,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = FintechColors.NavyDark)
            ) {
                Text("PROCEED TO VERIFY", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────

@Composable
fun TwoFaTopBar(
    title:       String,
    config:      TwoFaConfig,
    allDone:     Boolean,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(FintechColors.NavyDark, FintechColors.NavyLight)
                )
            )
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
        }
        Text(
            text     = if (allDone) "Verification Success" else title,
            color    = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = config.serviceIcon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(end = 16.dp).size(20.dp)
        )
    }
}

// ─────────────────────────────────────────────
// SERVICE CONTEXT CARD
// ─────────────────────────────────────────────

@Composable
fun ServiceContextCard(config: TwoFaConfig) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = FintechColors.NavyDark.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(config.serviceColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = config.serviceIcon,
                    contentDescription = null,
                    tint = config.serviceColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    text  = "Action Required",
                    style = MaterialTheme.typography.labelMedium,
                    color = config.serviceColor
                )
                Text(
                    text  = config.serviceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// SHARED LOADING / SUCCESS / FAIL COMPONENTS
// ─────────────────────────────────────────────

@Composable
fun LoadingCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = FintechColors.NavyDark)
            Text(message, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun FailedCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(24.dp),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.ErrorOutline, null, tint = Color.Red, modifier = Modifier.size(48.dp))
            Text(message, color = Color.Red, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
            
            Button(
                onClick = onRetry,
                colors  = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape   = RoundedCornerShape(12.dp)
            ) {
                Text("Try Again")
            }
        }
    }
}

@Composable
fun AllDoneCard(config: TwoFaConfig) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.VerifiedUser, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(40.dp))
        }
        
        Text("Verification Complete", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Redirecting you to ${config.serviceName}...",
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────

@Preview(showBackground = true, name = "Two-Factor Auth Screen - Face Verification")
@Composable
fun TwoFactorAuthScreenPreview() {
    ModernUITheme {
        TwoFactorAuthScreen(
            config = TwoFaConfig(
                title = "Verify Identity",
                subtitle = "Confirm before proceeding to AEPS",
                serviceName = "AEPS Transaction",
                serviceIcon = Icons.Default.Face,
                steps = listOf(TwoFaStep.FACE_VERIFICATION)
            )
        )
    }
}
