package com.example.modernui.ui.screens.common

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import com.example.modernui.ui.theme.FintechColors
import com.example.modernui.ui.theme.ModernUITheme
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────
// CONFIGURATION DATA CLASS
// ─────────────────────────────────────────────

enum class TwoFaStep { FACE_VERIFICATION }

data class TwoFaConfig(
    val title:         String        = "Two-Factor Authentication",
    val subtitle:      String        = "Verify your identity to continue",
    val serviceName:   String        = "this service",
    val serviceIcon:   ImageVector   = Icons.Default.Security,
    val serviceColor:  Color         = FintechColors.NavyDark,
    val mobile:        String        = "",
    val steps:         List<TwoFaStep> = listOf(TwoFaStep.FACE_VERIFICATION)
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
    onBackClick: () -> Unit   = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    var twoFaState by remember { mutableStateOf(TwoFaState.IDLE) }
    var failMessage by remember { mutableStateOf("") }

    // Launcher to handle the RD service intent result in the same activity
    val faceCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val responseData = result.data?.getStringExtra("res_data") ?: ""
        Log.d("TwoFaScreen", "Face RD Response: $responseData")

        if (result.resultCode == Activity.RESULT_OK) {
            // Refined XML Parsing using Regex to extract errCode and errInfo
            val errCodeMatch = Regex("errCode=\"([^\"]*)\"").find(responseData)
            val errCode = errCodeMatch?.groupValues?.get(1)

            if (errCode == "0") {
                twoFaState = TwoFaState.ALL_DONE
            } else {
                val errInfoMatch = Regex("errInfo=\"([^\"]*)\"").find(responseData)
                val errInfo = errInfoMatch?.groupValues?.get(1) ?: "Verification Failed"
                failMessage = "$errInfo (Code: $errCode)"
                twoFaState = TwoFaState.FAILED
            }
        } else {
            failMessage = "Face verification was cancelled or the RD service failed to respond."
            twoFaState = TwoFaState.FAILED
        }
    }

    LaunchedEffect(twoFaState) {
        if (twoFaState == TwoFaState.ALL_DONE) {
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
            allDone     = twoFaState == TwoFaState.ALL_DONE
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

            // ── Step content ──────────────────
            AnimatedContent(
                targetState    = twoFaState,
                transitionSpec = {
                    fadeIn(tween(250)) togetherWith fadeOut(tween(200))
                },
                label = "twofa_content"
            ) { state ->
                when (state) {
                    TwoFaState.IDLE -> {
                        FaceVerificationPromptCard(
                            onProceed = {
//                                try {
//                                    val intent = Intent("in.gov.uidai.rdservice.face.FACE_CAPTURE")
//                                    intent.setPackage("in.gov.uidai.facerd")
//
//                                    // Standard request data for Face RD
//                                    val requestXml = """
//                                        <FaceCaptureRequest version="1.0">
//                                            <TransactionId>${System.currentTimeMillis()}</TransactionId>
//                                        </FaceCaptureRequest>
//                                    """.trimIndent()
//
//                                    intent.putExtra("request_data", requestXml)
//
//                                    faceCaptureLauncher.launch(intent)
//                                    twoFaState = TwoFaState.VERIFYING
//                                } catch (e: Exception) {
//                                    val str: String = e.message.toString()
//                                    Log.e("TwoFaScreen", "Error launching RD Face Service: $str")
//                                    Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
//                                    failMessage = "RD Face Service not found or failed to launch"
//                                    twoFaState = TwoFaState.FAILED
//                                }
                                try {
//                                    val intent = Intent("in.gov.uidai.rdservice.face.FACE_CAPTURE")
//                                    intent.setPackage("in.gov.uidai.facerd")

                                    val intent = Intent("in.gov.uidai.rdservice.face.FACE_CAPTURE")
                                    intent.`package`="in.gov.uidai.facerd"

// Direct component set karke dekho agar simple setPackage kaam nahi kar raha
                                    // Standard request data
                                    // Tip: TransactionId unique hona chahiye, System.currentTimeMillis() works fine.
                                    val requestXml = """
        <FaceCaptureRequest version="1.0">
            <TransactionId>${System.currentTimeMillis()}</TransactionId>
        </FaceCaptureRequest>
    """.trimIndent()

                                    intent.putExtra("request", requestXml)

                                    // Launching the intent
                                    faceCaptureLauncher.launch(intent)
                                    twoFaState = TwoFaState.VERIFYING

                                } catch (e: Exception) { // Fixed: Added Exception type
                                    val str: String = e.message ?: "Unknown Error"
                                    Log.e("TwoFaScreen", "Error launching RD Face Service: $str", e)

                                    // Better User Feedback
                                    failMessage = if (e is android.content.ActivityNotFoundException) {
                                        "Install Addhar Face App"
                                    } else {
                                        "RD Face Service failed: $str"
                                    }

                                    Toast.makeText(context, failMessage, Toast.LENGTH_SHORT).show()
                                    twoFaState = TwoFaState.FAILED
                                }
                            }
                        )
                    }

                    TwoFaState.VERIFYING -> {
                        LoadingCard(message = "Processing Verification...")
                    }

                    TwoFaState.FAILED -> {
                        FailedCard(
                            message    = failMessage,
                            onRetry    = { twoFaState = TwoFaState.IDLE }
                        )
                    }

                    TwoFaState.ALL_DONE -> {
                        AllDoneCard(config = config)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ─────────────────────────────────────────────
// FACE VERIFICATION PROMPT CARD
// ─────────────────────────────────────────────

@Composable
fun FaceVerificationPromptCard(onProceed: () -> Unit) {
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
                Icons.Default.Face,
                null,
                tint = FintechColors.NavyDark,
                modifier = Modifier.size(64.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Face Verification",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Confirm your identity using face recognition to proceed.",
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
