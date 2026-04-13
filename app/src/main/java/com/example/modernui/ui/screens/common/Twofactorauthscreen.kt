//package com.example.modernui.ui.screens.common
//
//import android.app.Activity
//import android.content.ActivityNotFoundException
//import android.content.ComponentName
//import android.content.Intent
//import android.net.Uri
//import android.util.Log
//import android.widget.Toast
//import android.hardware.usb.UsbManager
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.animation.*
//import androidx.compose.animation.core.*
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.modernui.ui.components.intentpackage.RdHelper
//import com.example.modernui.ui.theme.FintechColors
//import com.example.modernui.ui.theme.ModernUITheme
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.hilt.navigation.compose.hiltViewModel
//import kotlinx.coroutines.delay
//
//// ─────────────────────────────────────────────
//// CONFIGURATION DATA CLASS
//// ─────────────────────────────────────────────
//
//enum class TwoFaStep {
//    OTP,
//    BIOMETRIC,
//    FACE_VERIFICATION
//}
//
//data class TwoFaConfig(
//    val title:         String        = "Two-Factor Authentication",
//    val subtitle:      String        = "Verify your identity to continue",
//    val serviceName:   String        = "this service",
//    val serviceIcon:   ImageVector   = Icons.Default.Security,
//    val serviceColor:  Color         = FintechColors.NavyDark,
//    val mobile:        String        = "",
//    val steps:         List<TwoFaStep> = listOf(TwoFaStep.FACE_VERIFICATION),
//    val selectedDevice: String        = "face" // Added to support multiple devices from RdHelper
//)
//
//// ─────────────────────────────────────────────
//// INTERNAL STATE MACHINE
//// ─────────────────────────────────────────────
//
//enum class TwoFaState {
//    IDLE,
//    VERIFYING,
//    FAILED,
//    ALL_DONE
//}
//
//// ─────────────────────────────────────────────
//// ROOT — UNIVERSAL 2FA SCREEN
//// ─────────────────────────────────────────────
//
//@Composable
//fun TwoFactorAuthScreen(
//    config:      TwoFaConfig  = TwoFaConfig(),
//    onVerified:  () -> Unit   = {},
//    onBackClick: () -> Unit   = {},
//    viewModel:   TwoFaViewModel = hiltViewModel()
//) {
//    val colorScheme = MaterialTheme.colorScheme
//    val context = LocalContext.current
//
//    val uiState by viewModel.uiState.collectAsState()
//    val selectedDeviceId by viewModel.selectedDeviceId.collectAsState()
//
//    // Listen for USB device connection
//    DisposableEffect(Unit) {
//        val receiver = object : android.content.BroadcastReceiver() {
//            override fun onReceive(context: android.content.Context, intent: android.content.Intent) {
//                if (UsbManager.ACTION_USB_DEVICE_ATTACHED == intent.action) {
//                    Toast.makeText(context, "Biometric Device Connected", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//        val filter = android.content.IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED)
//        context.registerReceiver(receiver, filter)
//        onDispose {
//            context.unregisterReceiver(receiver)
//        }
//    }
//
//    // Launcher to handle the RD service intent result in the same activity
////    val faceCaptureLauncher = rememberLauncherForActivityResult(
////        contract = ActivityResultContracts.StartActivityForResult()
////    ) { result ->
////        val outputKey = RdHelper.getOutputKey(selectedDeviceId)
////        val responseData = result.data?.getStringExtra("response") ?: ""
////
////        Log.d("TwoFaScreen", "RD Service Result: Code=${result.resultCode}, Key=$outputKey, DataLength=${responseData.length}")
////
////        if (result.resultCode == Activity.RESULT_OK) {
////            if (responseData.isNotEmpty()) {
////                viewModel.handleRdServiceResult(responseData)
////            } else {
////                // Some devices might put data in different extras or as a URI, but PID_DATA is standard for Biometric
////                // and "response" for UIDAI Face RD.
////                Toast.makeText(context, "Device returned empty data ($outputKey)", Toast.LENGTH_SHORT).show()
////                viewModel.resetState()
////            }
////        } else {
////            viewModel.resetState()
////            Toast.makeText(context, "Verification Cancelled", Toast.LENGTH_SHORT).show()
////        }
////    }
////    val faceCaptureLauncher = rememberLauncherForActivityResult(
////        contract = ActivityResultContracts.StartActivityForResult()
////    ) { result ->
////        // Yahan check karo ki result.data null toh nahi hai
////        val dataIntent = result.data
////
////        // Sabse safe tareeka: Dono keys check kar lo
////        val responseData = dataIntent?.getStringExtra("PID_DATA")
////            ?: dataIntent?.getStringExtra("response")
////            ?: ""
////
////        Log.d("TwoFaScreen", "Final Response Data: $responseData")
////
////        if (result.resultCode == Activity.RESULT_OK && responseData.isNotEmpty()) {
////            viewModel.handleRdServiceResult(responseData)
////        } else {
////            viewModel.resetState()
////            Toast.makeText(context, "Data not captured or device busy", Toast.LENGTH_SHORT).show()
////        }
////    }
//    val faceCaptureLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        val dataIntent = result.data
//
//        // 1. Dynamic Key nikalna: selectedDeviceId ke basis par "response" ya "PID_DATA"
//        val outputKey = RdHelper.getOutputKey(selectedDeviceId)
//
//        // 2. Data Fetching (Serialization): Sabhi scenarios handle karne ke liye
//        val responseData = dataIntent?.getStringExtra(outputKey) ?: ""
//
//        Log.d("TwoFaScreen", "Capture Result: Code=${result.resultCode}, KeyUsed=$outputKey, DataFound=${responseData.isNotEmpty()}")
//
//        if (result.resultCode == Activity.RESULT_OK) {
//            if (responseData.isNotEmpty()) {
//                // Biometric captured, ab ViewModel is XML ko parse/process karega
//                viewModel.handleRdServiceResult(responseData)
//            } else {
//                Toast.makeText(context, "Device returned empty data ($outputKey)", Toast.LENGTH_SHORT).show()
//                viewModel.resetState()
//            }
//        } else {
//            viewModel.resetState()
//            Toast.makeText(context, "Verification Cancelled", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    LaunchedEffect(uiState) {
//        if (uiState is TwoFaUiState.Success) {
//            delay(800)
//            onVerified()
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(colorScheme.background)
//    ) {
//
//        // ── Top bar ───────────────────────────
//        TwoFaTopBar(
//            title       = config.title,
//            onBackClick = onBackClick,
//            config      = config,
//            allDone     = uiState is TwoFaUiState.Success
//        )
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .verticalScroll(rememberScrollState())
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//
//            // ── Service context card ──────────
//            ServiceContextCard(config = config)
//
//            // ── Device Selection ──────────────
//            DeviceSelectionCard(
//                selectedId = selectedDeviceId,
//                onDeviceSelected = { id ->
//                    viewModel.onDeviceSelected(id)
//                    val deviceName = RdHelper.SUPPORTED_DEVICES.find { it.id == id }?.name ?: "Device"
//                    Toast.makeText(context, "$deviceName selected", Toast.LENGTH_SHORT).show()
//                }
//            )
//
//            // ── Step content ──────────────────
//            AnimatedContent(
//                targetState    = uiState,
//                transitionSpec = {
//                    fadeIn(tween(250)) togetherWith fadeOut(tween(200))
//                },
//                label = "twofa_content"
//            ) { state ->
//                when (state) {
//                    is TwoFaUiState.Idle -> {
//                        val currentDevice = RdHelper.SUPPORTED_DEVICES.find { it.id == selectedDeviceId }
//                            ?: RdHelper.SUPPORTED_DEVICES[0]
//
//                        FaceVerificationPromptCard(
//                            label = currentDevice.name,
//                            icon = currentDevice.icon,
//
//
//                            onProceed = {
//                                val packageName = RdHelper.getPackage(selectedDeviceId)
//                                val action = RdHelper.getAction(selectedDeviceId)
//                                val inputKey = RdHelper.getInputKey(selectedDeviceId)
//
//                                val pidType = when(selectedDeviceId) {
//                                    "mfs110", "morpho_l1" -> RdHelper.fingType
//                                    "mantra_mis100v2" -> RdHelper.irisType
//                                    else -> RdHelper.faceType
//                                }
//
//                                val pidOptionsXml = RdHelper.makePidXm(selectedDeviceId, pidType)
//
//                                try {
//                                    val intent = Intent(action).apply {
//                                        setPackage(packageName)
//                                        putExtra(inputKey, pidOptionsXml)
//                                    }
//                                    Log.d("TwoFaScreen", "Launching RD: Device=$selectedDeviceId | Package=$packageName | Key=$inputKey")
//                                    faceCaptureLauncher.launch(intent)
//                                } catch (e: ActivityNotFoundException) {
//                                    Log.e("TwoFaScreen", "RD Service not found: $packageName")
//                                    Toast.makeText(context, "RD Service not installed for $selectedDeviceId", Toast.LENGTH_LONG).show()
//                                    try {
//                                        val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
//                                        playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                                        context.startActivity(playStoreIntent)
//                                    } catch (ex: Exception) {
//                                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
//                                        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                                        context.startActivity(webIntent)
//                                    }
//                                } catch (e: Exception) {
//                                    Log.e("TwoFaScreen", "Error launching RD: ${e.message}")
//                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//                                }
//                            }
//
//
//                        )
//                    }
//
//                    is TwoFaUiState.Verifying -> {
//                        LoadingCard(message = "Processing Verification...")
//                    }
//
//                    is TwoFaUiState.Error -> {
//                        FailedCard(
//                            message    = state.message,
//                            onRetry    = { viewModel.resetState() }
//                        )
//                    }
//
//                    is TwoFaUiState.Success -> {
//                        AllDoneCard(config = config)
//                    }
//                }
//            }
//
//            Spacer(Modifier.height(8.dp))
//        }
//    }
//}
//
//// ─────────────────────────────────────────────
//// DEVICE SELECTION CARD
//// ─────────────────────────────────────────────
//
//@Composable
//fun DeviceSelectionCard(
//    selectedId: String,
//    onDeviceSelected: (String) -> Unit
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
//        )
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text(
//                text = "Select Verification Device",
//                style = MaterialTheme.typography.labelLarge,
//                color = MaterialTheme.colorScheme.primary,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.padding(bottom = 12.dp)
//            )
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                RdHelper.SUPPORTED_DEVICES.forEach { device ->
//                    val isSelected = device.id == selectedId
//                    val bgColor = if (isSelected) FintechColors.NavyDark else Color.Transparent
//                    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
//
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .clip(RoundedCornerShape(12.dp))
//                            .background(bgColor)
//                            .clickable { onDeviceSelected(device.id) }
//                            .padding(vertical = 12.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                            Icon(
//                                imageVector = device.icon,
//                                contentDescription = null,
//                                tint = contentColor,
//                                modifier = Modifier.size(24.dp)
//                            )
//                            Text(
//                                text = device.name.split(" ").first(),
//                                style = MaterialTheme.typography.labelSmall,
//                                color = contentColor,
//                                modifier = Modifier.padding(top = 4.dp)
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//// ─────────────────────────────────────────────
//// FACE VERIFICATION PROMPT CARD
//// ─────────────────────────────────────────────
//
//@Composable
//fun FaceVerificationPromptCard(
//    label: String,
//    icon: ImageVector,
//    onProceed: () -> Unit
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape    = RoundedCornerShape(24.dp),
//        elevation = CardDefaults.cardElevation(4.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(24.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(20.dp)
//        ) {
//            Icon(
//                imageVector = icon,
//                contentDescription = null,
//                tint = FintechColors.NavyDark,
//                modifier = Modifier.size(64.dp)
//            )
//
//            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                Text(
//                    text = label,
//                    style = MaterialTheme.typography.titleLarge,
//                    fontWeight = FontWeight.Bold
//                )
//                Text(
//                    text = "Confirm your identity using $label to proceed.",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = Color.Gray,
//                    textAlign = TextAlign.Center
//                )
//            }
//
//            Button(
//                onClick  = onProceed,
//                modifier = Modifier.fillMaxWidth().height(50.dp),
//                shape    = RoundedCornerShape(12.dp),
//                colors   = ButtonDefaults.buttonColors(containerColor = FintechColors.NavyDark)
//            ) {
//                Text("PROCEED TO VERIFY", fontWeight = FontWeight.Bold)
//            }
//        }
//    }
//}
//
//// ─────────────────────────────────────────────
//// TOP BAR
//// ─────────────────────────────────────────────
//
//@Composable
//fun TwoFaTopBar(
//    title:       String,
//    config:      TwoFaConfig,
//    allDone:     Boolean,
//    onBackClick: () -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(55.dp)
//            .background(
//                Brush.horizontalGradient(
//                    listOf(FintechColors.NavyDark, FintechColors.NavyLight)
//                )
//            )
//            .padding(horizontal = 4.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        IconButton(onClick = onBackClick) {
//            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
//        }
//        Text(
//            text     = if (allDone) "Verification Success" else title,
//            color    = Color.White,
//            fontSize = 18.sp,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier.weight(1f)
//        )
//        Icon(
//            imageVector = config.serviceIcon,
//            contentDescription = null,
//            tint = Color.White.copy(alpha = 0.7f),
//            modifier = Modifier.padding(end = 16.dp).size(20.dp)
//        )
//    }
//}
//
//// ─────────────────────────────────────────────
//// SERVICE CONTEXT CARD
//// ─────────────────────────────────────────────
//
//@Composable
//fun ServiceContextCard(config: TwoFaConfig) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape    = RoundedCornerShape(16.dp),
//        colors   = CardDefaults.cardColors(
//            containerColor = FintechColors.NavyDark.copy(alpha = 0.05f)
//        )
//    ) {
//        Row(
//            modifier = Modifier.padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(48.dp)
//                    .clip(CircleShape)
//                    .background(config.serviceColor.copy(alpha = 0.1f)),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = config.serviceIcon,
//                    contentDescription = null,
//                    tint = config.serviceColor,
//                    modifier = Modifier.size(24.dp)
//                )
//            }
//
//            Spacer(Modifier.width(16.dp))
//
//            Column {
//                Text(
//                    text  = "Action Required",
//                    style = MaterialTheme.typography.labelMedium,
//                    color = config.serviceColor
//                )
//                Text(
//                    text  = config.serviceName,
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//        }
//    }
//}
//
//// ─────────────────────────────────────────────
//// SHARED LOADING / SUCCESS / FAIL COMPONENTS
//// ─────────────────────────────────────────────
//
//@Composable
//fun LoadingCard(message: String) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape    = RoundedCornerShape(24.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(32.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            CircularProgressIndicator(color = FintechColors.NavyDark)
//            Text(message, fontWeight = FontWeight.Medium)
//        }
//    }
//}
//
//@Composable
//fun FailedCard(message: String, onRetry: () -> Unit) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape    = RoundedCornerShape(24.dp),
//        colors   = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3))
//    ) {
//        Column(
//            modifier = Modifier.padding(24.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            Icon(Icons.Default.ErrorOutline, null, tint = Color.Red, modifier = Modifier.size(48.dp))
//            Text(message, color = Color.Red, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
//
//            Button(
//                onClick = onRetry,
//                colors  = ButtonDefaults.buttonColors(containerColor = Color.Red),
//                shape   = RoundedCornerShape(12.dp)
//            ) {
//                Text("Try Again")
//            }
//        }
//    }
//}
//
//@Composable
//fun AllDoneCard(config: TwoFaConfig) {
//    Column(
//        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        Box(
//            modifier = Modifier
//                .size(80.dp)
//                .clip(CircleShape)
//                .background(Color(0xFFE8F5E9)),
//            contentAlignment = Alignment.Center
//        ) {
//            Icon(Icons.Default.VerifiedUser, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(40.dp))
//        }
//
//        Text("Verification Complete", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
//        Text(
//            "Redirecting you to ${config.serviceName}...",
//            color = Color.Gray,
//            textAlign = TextAlign.Center
//        )
//    }
//}
//
//// ─────────────────────────────────────────────
//// PREVIEWS
//// ─────────────────────────────────────────────
//
//@Preview(showBackground = true, name = "Two-Factor Auth Screen - Face Verification")
//@Composable
//fun TwoFactorAuthScreenPreview() {
//    ModernUITheme {
//        TwoFactorAuthScreen(
//            config = TwoFaConfig(
//                title = "Verify Identity",
//                subtitle = "Confirm before proceeding to AEPS",
//                serviceName = "AEPS Transaction",
//                serviceIcon = Icons.Default.Face,
//                steps = listOf(TwoFaStep.FACE_VERIFICATION)
//            )
//        )
//    }
//}
package com.example.modernui.ui.screens.common

import android.app.Activity
import android.content.ActivityNotFoundException
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.modernui.ui.components.intentpackage.RdHelper
import com.example.modernui.ui.theme.FintechColors
import com.example.modernui.ui.theme.ModernUITheme
import kotlinx.coroutines.delay


// ─────────────────────────────────────────────
// FINGER DATA MODEL
// Represents each finger from API: fing1-fing4
// ─────────────────────────────────────────────

data class FingerOption(
    val key:       String,   // "fing1", "fing2", "fing3", "fing4"
    val label:     String,   // "Right Index", etc.
    val subLabel:  String,   // "Finger 1"
    val isAvailable: Boolean // from API: true = registered, false = not registered
)

// ─────────────────────────────────────────────
// MAPS API RESPONSE fing1-fing4 TO FingerOption
// ─────────────────────────────────────────────

fun buildFingerOptions(
    fing1: Boolean,
    fing2: Boolean,
    fing3: Boolean,
    fing4: Boolean
): List<FingerOption> = listOf(
    FingerOption("fing1", "AIRTEL",  "Finger 1", fing1),
    FingerOption("fing2", "NSDL", "Finger 2", fing2),
    FingerOption("fing3", "FINO",   "Finger 3", fing3),
    FingerOption("fing4", "JIO",  "Finger 4", fing4),

)

// ─────────────────────────────────────────────
// DEFAULT PREVIEW OPTIONS (before API call)
// ─────────────────────────────────────────────

val defaultFingerOptions = buildFingerOptions(
    fing1 = true,
    fing2 = false,
    fing3 = false,
    fing4 = true
)


// ─────────────────────────────────────────────
// CONFIG — unchanged, stays compatible
// ─────────────────────────────────────────────

enum class TwoFaStep {
    OTP,
    BIOMETRIC,
    FACE_VERIFICATION
}

data class TwoFaConfig(
    val title:          String        = "Two-Factor Authentication",
    val subtitle:       String        = "Verify your identity to continue",
    val serviceName:    String        = "this service",
    val serviceIcon:    ImageVector   = Icons.Default.Security,
    val serviceColor:   Color         = FintechColors.NavyDark,
    val mobile:         String        = "",
    val steps:          List<TwoFaStep> = listOf(TwoFaStep.FACE_VERIFICATION),
    val selectedDevice: String        = "face",
    // Finger availability from API response
    val fing1: Boolean  = true,
    val fing2: Boolean  = false,
    val fing3: Boolean  = false,
    val fing4: Boolean  = true
)


// ─────────────────────────────────────────────
// ROOT — UNIVERSAL 2FA SCREEN
// ─────────────────────────────────────────────

@Composable
fun TwoFactorAuthScreen(
    config:      TwoFaConfig    = TwoFaConfig(),
    onVerified:  () -> Unit     = {},
    onBackClick: () -> Unit     = {},
    viewModel:   TwoFaViewModel = hiltViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val context     = LocalContext.current

    val uiState        by viewModel.uiState.collectAsState()
    val selectedDeviceId by viewModel.selectedDeviceId.collectAsState()

    // Finger options built from API response passed via config
    val fingerOptions = remember(config) {
        buildFingerOptions(config.fing1, config.fing2, config.fing3, config.fing4)
    }

    // Selected finger — default to first available
    var selectedFinger by remember {
        mutableStateOf(fingerOptions.firstOrNull { it.isAvailable }?.key ?: "fing1")
    }


    // USB device connection listener
    DisposableEffect(Unit) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: android.content.Context, intent: android.content.Intent) {
                if (UsbManager.ACTION_USB_DEVICE_ATTACHED == intent.action) {
                    Toast.makeText(ctx, "Biometric Device Connected", Toast.LENGTH_SHORT).show()
                }
            }
        }
        val filter = android.content.IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        context.registerReceiver(receiver, filter)
        onDispose { context.unregisterReceiver(receiver) }
    }

    // Activity result launcher for RD service
    val faceCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val dataIntent  = result.data
        val outputKey   = RdHelper.getOutputKey(selectedDeviceId)
        val responseData = dataIntent?.getStringExtra(outputKey) ?: ""

        Log.d("TwoFaScreen", "Capture: Code=${result.resultCode}, Key=$outputKey, HasData=${responseData.isNotEmpty()}")

        if (result.resultCode == Activity.RESULT_OK) {
            if (responseData.isNotEmpty()) {
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

    // Navigate on success
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

            // ── Device selection ──────────────
            DeviceSelectionCard(
                selectedId       = selectedDeviceId,
                onDeviceSelected = { id ->
                    viewModel.onDeviceSelected(id)
                    val name = RdHelper.SUPPORTED_DEVICES.find { it.id == id }?.name ?: "Device"
                    Toast.makeText(context, "$name selected", Toast.LENGTH_SHORT).show()
                }
            )

            // ── Finger selection (from API fing1-fing4) ──
            FingerSelectionCard(
                fingerOptions    = fingerOptions,
                selectedFinger   = selectedFinger,
                onFingerSelected = { selectedFinger = it }
            )

            // ── Main action content ───────────
            AnimatedContent(
                targetState    = uiState,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                label          = "twofa_content"
            ) { state ->
                when (state) {

                    is TwoFaUiState.Idle -> {
                        val currentDevice = RdHelper.SUPPORTED_DEVICES
                            .find { it.id == selectedDeviceId }
                            ?: RdHelper.SUPPORTED_DEVICES[0]

                        FaceVerificationPromptCard(
                            label          = currentDevice.name,
                            icon           = currentDevice.icon,
                            selectedFinger = fingerOptions.find { it.key == selectedFinger },
                            onProceed      = {
                                val packageName  = RdHelper.getPackage(selectedDeviceId)
                                val action       = RdHelper.getAction(selectedDeviceId)
                                val inputKey     = RdHelper.getInputKey(selectedDeviceId)
                                val pidType      = when (selectedDeviceId) {
                                    "mfs110", "morpho_l1" -> RdHelper.fingType
                                    "mantra_mis100v2"     -> RdHelper.irisType
                                    else                  -> RdHelper.faceType
                                }
                                val pidOptionsXml = RdHelper.makePidXm(selectedDeviceId, pidType)

                                try {
                                    val intent = Intent(action).apply {
                                        setPackage(packageName)
                                        putExtra(inputKey, pidOptionsXml)
                                        // Pass selected finger index to RD service if supported
                                        putExtra("finger_index", selectedFinger.removePrefix("fing"))
                                    }
                                    Log.d("TwoFaScreen",
                                        "Launching RD: Device=$selectedDeviceId Finger=$selectedFinger")
                                    faceCaptureLauncher.launch(intent)
                                } catch (e: ActivityNotFoundException) {
                                    Log.e("TwoFaScreen", "RD not found: $packageName")
                                    Toast.makeText(context,
                                        "RD Service not installed for $selectedDeviceId",
                                        Toast.LENGTH_LONG).show()
                                    try {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW,
                                                Uri.parse("market://details?id=$packageName"))
                                                .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                                    } catch (ex: Exception) {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW,
                                                Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                                                .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                                    }
                                } catch (e: Exception) {
                                    Log.e("TwoFaScreen", "Launch error: ${e.message}")
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
                            message = state.message,
                            onRetry = { viewModel.resetState() }
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


// ═════════════════════════════════════════════
// FINGER SELECTION CARD
// Shows fing1–fing4 as radio buttons
// Available (true) → selectable with navy style
// Unavailable (false) → greyed out, disabled
// ═════════════════════════════════════════════

@Composable
fun FingerSelectionCard(
    fingerOptions:    List<FingerOption>,
    selectedFinger:   String,
    onFingerSelected: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Left navy accent bar
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(FintechColors.NavyDark, FintechColors.NavyLight)
                        ),
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Section header
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape    = CircleShape,
                        color    = FintechColors.NavyDark.copy(alpha = 0.1f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Default.Fingerprint, null,
                                tint     = FintechColors.NavyDark,
                                modifier = Modifier.size(18.dp))
                        }
                    }
                    Column {
                        Text("Select Finger",
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color      = FintechColors.NavyDark)
                        Text("Choose a registered finger for verification",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.outline)
                    }
                }

                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Legend row
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(FintechColors.SuccessGreen)
                        )
                        Text("Registered",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.outline,
                            fontSize = 10.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colorScheme.outline.copy(alpha = 0.35f))
                        )
                        Text("Not Registered",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.outline,
                            fontSize = 10.sp)
                    }
                }

                // 2×2 grid of finger radio buttons
                val rows = fingerOptions.chunked(2)
                rows.forEach { row ->
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { finger ->
                            FingerOptionItem(
                                finger           = finger,
                                isSelected       = selectedFinger == finger.key,
                                onSelect         = {
                                    if (finger.isAvailable) onFingerSelected(finger.key)
                                },
                                modifier         = Modifier.weight(1f)
                            )
                        }
                        // Fill empty slot if odd count
                        if (row.size < 2) Spacer(Modifier.weight(1f))
                    }
                }

                // No finger registered warning
                val availableCount = fingerOptions.count { it.isAvailable }
                if (availableCount == 0) {
                    Surface(
                        shape    = RoundedCornerShape(10.dp),
                        color    = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, null,
                                tint     = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp))
                            Text("No fingers registered for this account",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
// INDIVIDUAL FINGER OPTION ITEM
// ─────────────────────────────────────────────

@Composable
fun FingerOptionItem(
    finger:    FingerOption,
    isSelected: Boolean,
    onSelect:   () -> Unit,
    modifier:   Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    val containerColor = when {
        !finger.isAvailable -> colorScheme.surfaceVariant.copy(alpha = 0.4f)
        isSelected          -> FintechColors.NavyDark.copy(alpha = 0.08f)
        else                -> colorScheme.surface
    }
    val borderColor = when {
        !finger.isAvailable -> colorScheme.outline.copy(alpha = 0.2f)
        isSelected          -> FintechColors.NavyDark
        else                -> colorScheme.outlineVariant.copy(alpha = 0.5f)
    }
    val borderWidth = if (isSelected && finger.isAvailable) 1.5.dp else 0.5.dp

    Surface(
        shape    = RoundedCornerShape(14.dp),
        color    = containerColor,
        modifier = modifier
            .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
            .clickable(enabled = finger.isAvailable) { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Radio button
            RadioButton(
                selected = isSelected && finger.isAvailable,
                onClick  = if (finger.isAvailable) onSelect else null,
                colors   = RadioButtonDefaults.colors(
                    selectedColor   = FintechColors.NavyDark,
                    unselectedColor = if (finger.isAvailable)
                        colorScheme.outline
                    else
                        colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier.size(18.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    finger.label,
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected && finger.isAvailable) FontWeight.Bold else FontWeight.Normal,
                    color      = when {
                        !finger.isAvailable -> colorScheme.outline.copy(alpha = 0.5f)
                        isSelected          -> FintechColors.NavyDark
                        else                -> colorScheme.onSurface
                    }
                )
                Text(
                    finger.subLabel,
                    style    = MaterialTheme.typography.labelSmall,
                    color    = colorScheme.outline.copy(alpha = if (finger.isAvailable) 0.8f else 0.4f),
                    fontSize = 10.sp
                )
            }

            // Status dot
            Surface(
                shape    = RoundedCornerShape(20.dp),
                color    = if (finger.isAvailable)
                    FintechColors.SuccessGreenLight
                else
                    colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(
                                if (finger.isAvailable) FintechColors.SuccessGreen
                                else colorScheme.outline.copy(alpha = 0.35f)
                            )
                    )
                    Text(
                        if (finger.isAvailable) "✓" else "✗",
                        style    = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color    = if (finger.isAvailable) FintechColors.SuccessGreenDark
                        else colorScheme.outline.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
// Fing VERIFICATION PROMPT CARD
// Updated to show selected finger summary
// ─────────────────────────────────────────────




@Composable
fun FingerSelectionScreen(viewModel: TwoFaViewModel) {
    val fingers by viewModel.availableFingers.collectAsState()
    val selectedFinger by viewModel.selectedFinger.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Select Service for 2FA", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        // FlowRow use kar sakte ho agar buttons zyada hain
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            fingers.forEach { finger ->
                val isSelected = selectedFinger == finger

                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.onFingerSelected(finger) },
                    label = { Text(finger.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* Call RD Service based on selectedFinger.value */ },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedFinger != null
        ) {
            Text("Capture Biometric (${selectedFinger?.label ?: ""})")
        }
    }
}

// ─────────────────────────────────────────────
// FACE VERIFICATION PROMPT CARD
// Updated to show selected finger summary
// ─────────────────────────────────────────────

@Composable
fun FaceVerificationPromptCard(
    label:          String,
    icon:           ImageVector,
    selectedFinger: FingerOption?,
    onProceed:      () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier            = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Device icon
            Surface(
                shape    = CircleShape,
                color    = FintechColors.NavyDark.copy(alpha = 0.08f),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(icon, null,
                        tint     = FintechColors.NavyDark,
                        modifier = Modifier.size(38.dp))
                }
            }

            // Device + Finger summary
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(label,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = FintechColors.NavyDark)
                Text("Confirm your identity using $label to proceed.",
                    style     = MaterialTheme.typography.bodySmall,
                    color     = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center)
            }

            // Selected finger chip — shows what will be scanned
            selectedFinger?.let { finger ->
                Surface(
                    shape    = RoundedCornerShape(20.dp),
                    color    = FintechColors.NavyDark.copy(alpha = 0.08f),
                    modifier = Modifier.border(
                        1.dp,
                        FintechColors.NavyDark.copy(alpha = 0.3f),
                        RoundedCornerShape(20.dp)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Fingerprint, null,
                            tint     = FintechColors.NavyDark,
                            modifier = Modifier.size(16.dp))
                        Text("Using: ${finger.label}",
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color      = FintechColors.NavyDark)
                        Text("(${finger.subLabel})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            // Proceed button
            Button(
                onClick  = onProceed,
                enabled  = selectedFinger?.isAvailable == true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = FintechColors.NavyDark,
                    contentColor           = Color.White,
                    disabledContainerColor = FintechColors.NavyDark.copy(alpha = 0.35f),
                    disabledContentColor   = Color.White.copy(alpha = 0.5f)
                )
            ) {
                Icon(Icons.Default.Fingerprint, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("PROCEED TO VERIFY",
                    fontWeight   = FontWeight.Bold,
                    letterSpacing = 0.5.sp)
            }
        }
    }
}


// ─────────────────────────────────────────────
// DEVICE SELECTION CARD — unchanged from original
// ─────────────────────────────────────────────

@Composable
fun DeviceSelectionCard(
    selectedId:       String,
    onDeviceSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Select Verification Device",
                style      = MaterialTheme.typography.labelLarge,
                color      = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(bottom = 12.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RdHelper.SUPPORTED_DEVICES.forEach { device ->
                    val isSelected   = device.id == selectedId
                    val bgColor      = if (isSelected) FintechColors.NavyDark else Color.Transparent
                    val contentColor = if (isSelected) Color.White
                    else MaterialTheme.colorScheme.onSurfaceVariant
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
                            Icon(device.icon, null,
                                tint     = contentColor,
                                modifier = Modifier.size(24.dp))
                            Text(device.name.split(" ").first(),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = contentColor,
                                modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
// TOP BAR — unchanged from original
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
                Brush.horizontalGradient(listOf(FintechColors.NavyDark, FintechColors.NavyLight))
            )
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
        }
        Text(
            text       = if (allDone) "Verification Success" else title,
            color      = Color.White,
            fontSize   = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.weight(1f)
        )
        Icon(config.serviceIcon, null,
            tint     = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(end = 16.dp).size(20.dp))
    }
}


// ─────────────────────────────────────────────
// SERVICE CONTEXT CARD — unchanged
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
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(config.serviceColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(config.serviceIcon, null,
                    tint     = config.serviceColor,
                    modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Action Required",
                    style = MaterialTheme.typography.labelMedium,
                    color = config.serviceColor)
                Text(config.serviceName,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
            }
        }
    }
}


// ─────────────────────────────────────────────
// LOADING / FAILED / ALL DONE — unchanged
// ─────────────────────────────────────────────

@Composable
fun LoadingCard(message: String) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(
            modifier            = Modifier.padding(32.dp),
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
            modifier            = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.ErrorOutline, null,
                tint = Color.Red, modifier = Modifier.size(48.dp))
            Text(message,
                color      = Color.Red,
                textAlign  = TextAlign.Center,
                fontWeight = FontWeight.Medium)
            Button(
                onClick = onRetry,
                colors  = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape   = RoundedCornerShape(12.dp)
            ) { Text("Try Again") }
        }
    }
}

@Composable
fun AllDoneCard(config: TwoFaConfig) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(vertical = 24.dp),
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
            Icon(Icons.Default.VerifiedUser, null,
                tint     = Color(0xFF2E7D32),
                modifier = Modifier.size(40.dp))
        }
        Text("Verification Complete",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("Redirecting you to ${config.serviceName}...",
            color     = Color.Gray,
            textAlign = TextAlign.Center)
    }
}


// ─────────────────────────────────────────────
// USAGE HELPER — parse API response into config
// ─────────────────────────────────────────────

/**
 * Call this after your API returns the 2FA response:
 *
 * val config = twoFaConfigFromApiResponse(
 *     fing1       = response.data.fing1,
 *     fing2       = response.data.fing2,
 *     fing3       = response.data.fing3,
 *     fing4       = response.data.fing4,
 *     serviceName = "AEPS"
 * )
 * navController.navigate("two_fa") // pass config via ViewModel or SavedStateHandle
 */
fun twoFaConfigFromApiResponse(
    fing1:       Boolean,
    fing2:       Boolean,
    fing3:       Boolean,
    fing4:       Boolean,
    serviceName: String      = "this service",
    serviceIcon: ImageVector = Icons.Default.Security
): TwoFaConfig = TwoFaConfig(
    title       = "Two-Factor Authentication",
    subtitle    = "Verify your identity to continue",
    serviceName = serviceName,
    serviceIcon = serviceIcon,
    steps       = listOf(TwoFaStep.FACE_VERIFICATION),
    fing1       = fing1,
    fing2       = fing2,
    fing3       = fing3,
    fing4       = fing4
)


// ─────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────

@Preview(showBackground = true, name = "2FA – fing1+fing4 active")
@Composable
fun Preview2FaFingerSelection() {
    ModernUITheme {
        // Simulates API response: fing1=true, fing2=false, fing3=false, fing4=true
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FingerSelectionCard(
                fingerOptions    = buildFingerOptions(true, false, false, true),
                selectedFinger   = "fing1",
                onFingerSelected = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "2FA – all fingers active")
@Composable
fun Preview2FaAllFingers() {
    ModernUITheme {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FingerSelectionCard(
                fingerOptions    = buildFingerOptions(true, true, true, true),
                selectedFinger   = "fing2",
                onFingerSelected = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "2FA – no fingers registered")
@Composable
fun Preview2FaNoFingers() {
    ModernUITheme {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FingerSelectionCard(
                fingerOptions    = buildFingerOptions(false, false, false, false),
                selectedFinger   = "fing1",
                onFingerSelected = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Full 2FA Screen Preview")
@Composable
fun TwoFactorAuthScreenPreview() {
    ModernUITheme {
        TwoFactorAuthScreen(
            config = TwoFaConfig(
                title       = "Verify Identity",
                subtitle    = "Confirm before proceeding to AEPS",
                serviceName = "AEPS Transaction",
                serviceIcon = Icons.Default.Face,
                steps       = listOf(TwoFaStep.FACE_VERIFICATION),
                fing1       = true,
                fing2       = false,
                fing3       = false,
                fing4       = true
            )
        )
    }
}