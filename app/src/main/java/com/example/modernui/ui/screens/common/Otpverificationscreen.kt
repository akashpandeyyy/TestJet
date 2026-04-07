package com.example.modernui.ui.screens.common

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modernui.ui.theme.FintechColors
import kotlinx.coroutines.delay


// ══════════════════════════════════════════════════════════
//  UNIVERSAL OTP VERIFICATION SCREEN
//
//  Fully standalone — call from any screen:
//
//  OtpVerificationScreen(
//      config      = OtpConfig(
//          title        = "Verify OTP",
//          subtitle     = "Confirm your mobile number",
//          purpose      = "AEPS Transaction",
//          purposeIcon  = Icons.Default.Fingerprint,
//          mobile       = "98XXXXX210",
//          otpLength    = 6,              // 4 or 6
//          autoVerify   = true,           // verify as soon as length reached
//          maxAttempts  = 3
//      ),
//      onVerified  = { /* OTP passed — proceed */ },
//      onBackClick = { navController.popBackStack() }
//  )
// ══════════════════════════════════════════════════════════


// ─────────────────────────────────────────────
// CONFIG
// ─────────────────────────────────────────────

data class OtpConfig(
    val title:       String       = "OTP Verification",
    val subtitle:    String       = "Enter the OTP sent to your mobile",
    val purpose:     String       = "this action",
    val purposeIcon: ImageVector  = Icons.Default.Security,
    val purposeColor: Color       = FintechColors.NavyDark,
    val mobile:      String       = "",           // shown in UI (can be masked)
    val otpLength:   Int          = 6,            // 4 or 6 digits
    val autoVerify:  Boolean      = true,         // verify on last digit
    val resendDelay: Int          = 30,           // seconds before resend
    val maxAttempts: Int          = 3
)


// ─────────────────────────────────────────────
// OTP SCREEN STATE
// ─────────────────────────────────────────────

private enum class OtpScreenState {
    SENDING,    // OTP being dispatched
    INPUT,      // waiting for user to type
    VERIFYING,  // API call in progress
    SUCCESS,    // OTP correct
    FAILED,     // wrong OTP
    LOCKED      // maxAttempts exhausted
}


// ─────────────────────────────────────────────
// ROOT — UNIVERSAL OTP SCREEN
// ─────────────────────────────────────────────

@Composable
fun OtpVerificationScreen(
    config:      OtpConfig = OtpConfig(),
    onVerifyOtp: (String) -> Unit = {},
    isVerifying: Boolean = false,
    verificationError: String? = null,
    isSuccess:   Boolean = false,
    onVerified:  () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    var state        by remember { mutableStateOf(OtpScreenState.SENDING) }
    var otp          by remember { mutableStateOf("") }
    var resendTimer  by remember { mutableIntStateOf(config.resendDelay) }
    var attempts     by remember { mutableIntStateOf(0) }
    var errorMsg     by remember { mutableStateOf("") }
    var resendCount  by remember { mutableIntStateOf(0) }

    // ── Auto-send OTP on launch ───────────────
    LaunchedEffect(Unit) {
        delay(1000)
        state       = OtpScreenState.INPUT
        resendTimer = config.resendDelay
    }

    // ── Resend countdown ──────────────────────
    LaunchedEffect(state, resendTimer) {
        if (state == OtpScreenState.INPUT && resendTimer > 0) {
            delay(1000)
            resendTimer--
        }
    }

    // ── Auto-verify when OTP full ─────────────
    LaunchedEffect(otp) {
        if (config.autoVerify && otp.length == config.otpLength
            && state == OtpScreenState.INPUT) {
            delay(200) // brief pause so user sees last digit
            onVerifyOtp(otp)
        }
    }

    // ── Sync internal state with external status ─
    LaunchedEffect(isVerifying, verificationError, isSuccess) {
        if (isVerifying) {
            state = OtpScreenState.VERIFYING
        } else if (isSuccess) {
            state = OtpScreenState.SUCCESS
            delay(900)
            onVerified()
        } else if (verificationError != null) {
            attempts++
            errorMsg = verificationError
            state = if (attempts >= config.maxAttempts) OtpScreenState.LOCKED
                    else OtpScreenState.FAILED
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {

        // ── Top bar ───────────────────────────
        OtpTopBar(
            config      = config,
            state       = state,
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            Spacer(Modifier.height(32.dp))

            // ── Animated icon ─────────────────
            OtpHeaderIcon(state = state, config = config)

            Spacer(Modifier.height(24.dp))

            // ── Title / subtitle ──────────────
            AnimatedContent(
                targetState    = state,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label          = "otp_title"
            ) { s ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        when (s) {
                            OtpScreenState.SENDING    -> "Sending OTP..."
                            OtpScreenState.INPUT      -> config.title
                            OtpScreenState.VERIFYING  -> "Verifying..."
                            OtpScreenState.SUCCESS    -> "Verified!"
                            OtpScreenState.FAILED     -> "Wrong OTP"
                            OtpScreenState.LOCKED     -> "Account Locked"
                        },
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color      = when (s) {
                            OtpScreenState.SUCCESS -> FintechColors.SuccessGreen
                            OtpScreenState.FAILED  -> colorScheme.error
                            OtpScreenState.LOCKED  -> colorScheme.error
                            else                   -> colorScheme.onBackground
                        },
                        textAlign = TextAlign.Center
                    )
                    Text(
                        when (s) {
                            OtpScreenState.SENDING   -> "We're sending an OTP to your mobile"
                            OtpScreenState.INPUT     -> if (config.mobile.isNotEmpty())
                                "Enter the ${config.otpLength}-digit OTP sent to +91 ${config.mobile}"
                            else "Enter the ${config.otpLength}-digit OTP sent to your mobile"
                            OtpScreenState.VERIFYING -> "Please wait while we verify your OTP"
                            OtpScreenState.SUCCESS   -> "OTP verified successfully! Redirecting..."
                            OtpScreenState.FAILED    -> errorMsg
                            OtpScreenState.LOCKED    -> "Too many wrong attempts. Please try after 30 minutes or contact support."
                        },
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = when (s) {
                            OtpScreenState.SUCCESS -> FintechColors.SuccessGreen.copy(alpha = 0.8f)
                            OtpScreenState.FAILED  -> colorScheme.error.copy(alpha = 0.8f)
                            OtpScreenState.LOCKED  -> colorScheme.error.copy(alpha = 0.8f)
                            else                   -> colorScheme.outline
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            // ── OTP boxes ─────────────────────
            AnimatedVisibility(
                visible = state == OtpScreenState.INPUT
                        || state == OtpScreenState.VERIFYING
                        || state == OtpScreenState.FAILED,
                enter   = fadeIn() + slideInVertically { it / 4 },
                exit    = fadeOut()
            ) {
                OtpBoxRow(
                    otp       = otp,
                    length    = config.otpLength,
                    state     = state,
                    onOtpChange = { new ->
                        if (new.all(Char::isDigit) && new.length <= config.otpLength
                            && state != OtpScreenState.VERIFYING) {
                            otp = new
                            // Reset to INPUT if user edits after failure
                            if (state == OtpScreenState.FAILED) {
                                state = OtpScreenState.INPUT
                            }
                        }
                    }
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── State-specific bottom content ─
            AnimatedContent(
                targetState    = state,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                label          = "otp_bottom"
            ) { s ->
                when (s) {

                    OtpScreenState.SENDING -> {
                        OtpSendingIndicator()
                    }

                    OtpScreenState.INPUT -> {
                        OtpInputControls(
                            config      = config,
                            otp         = otp,
                            resendTimer = resendTimer,
                            resendCount = resendCount,
                            onVerify    = { onVerifyOtp(otp) },
                            onResend    = {
                                otp         = ""
                                resendCount++
                                resendTimer = config.resendDelay
                                state       = OtpScreenState.SENDING
                            },
                            onChangeNumber = onBackClick
                        )
                    }

                    OtpScreenState.VERIFYING -> {
                        OtpVerifyingIndicator()
                    }

                    OtpScreenState.SUCCESS -> {
                        OtpSuccessBanner()
                    }

                    OtpScreenState.FAILED -> {
                        OtpFailedControls(
                            attempts    = attempts,
                            maxAttempts = config.maxAttempts,
                            onRetry     = {
                                otp   = ""
                                state = OtpScreenState.INPUT
                            },
                            onResend = {
                                otp         = ""
                                resendCount++
                                resendTimer = config.resendDelay
                                state       = OtpScreenState.SENDING
                            }
                        )
                    }

                    OtpScreenState.LOCKED -> {
                        OtpLockedBanner(onBack = onBackClick)
                    }
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────

@Composable
private fun OtpTopBar(
    config:      OtpConfig,
    state:       OtpScreenState,
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
        IconButton(onClick = {
            if (state != OtpScreenState.VERIFYING && state != OtpScreenState.SUCCESS) {
                onBackClick()
            }
        }) {
            Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
        }

        Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
            Text(config.title,
                color      = Color.White,
                fontSize   = 17.sp,
                fontWeight = FontWeight.Bold)
            if (config.purpose.isNotEmpty()) {
                Text("for ${config.purpose}",
                    color  = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp)
            }
        }

        // Purpose icon badge in top-right
        Surface(
            shape    = CircleShape,
            color    = Color.White.copy(alpha = 0.15f),
            modifier = Modifier
                .padding(end = 12.dp)
                .size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(config.purposeIcon, null,
                    tint     = Color.White,
                    modifier = Modifier.size(18.dp))
            }
        }
    }
}


// ─────────────────────────────────────────────
// ANIMATED HEADER ICON
// Changes per state with spring animation
// ─────────────────────────────────────────────

@Composable
private fun OtpHeaderIcon(state: OtpScreenState, config: OtpConfig) {
    val (icon, bgColor, iconColor) = when (state) {
        OtpScreenState.SENDING   -> Triple(Icons.Default.Sms,          FintechColors.NavyDark.copy(alpha = 0.1f), FintechColors.NavyDark)
        OtpScreenState.INPUT     -> Triple(Icons.Default.Sms,          FintechColors.NavyDark.copy(alpha = 0.1f), FintechColors.NavyDark)
        OtpScreenState.VERIFYING -> Triple(Icons.Default.HourglassTop, FintechColors.NavyDark.copy(alpha = 0.1f), FintechColors.NavyDark)
        OtpScreenState.SUCCESS   -> Triple(Icons.Default.CheckCircle,  FintechColors.SuccessGreenLight,            FintechColors.SuccessGreen)
        OtpScreenState.FAILED    -> Triple(Icons.Default.ErrorOutline,  MaterialTheme.colorScheme.errorContainer,   MaterialTheme.colorScheme.error)
        OtpScreenState.LOCKED    -> Triple(Icons.Default.Lock,          MaterialTheme.colorScheme.errorContainer,   MaterialTheme.colorScheme.error)
    }

    var visible by remember(state) { mutableStateOf(false) }
    LaunchedEffect(state) { visible = false; delay(50); visible = true }

    val scale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0.6f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "icon_scale"
    )

    // Pulsing ring for SCANNING / SENDING states
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.05f,
        targetValue   = 0.18f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOut), RepeatMode.Reverse),
        label         = "ring_alpha"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.scale(scale)) {
        // Outer pulse ring — only when SENDING or VERIFYING
        if (state == OtpScreenState.SENDING || state == OtpScreenState.VERIFYING) {
            Surface(
                shape    = CircleShape,
                color    = FintechColors.NavyDark.copy(alpha = ringAlpha),
                modifier = Modifier.size(104.dp)
            ) {}
            Surface(
                shape    = CircleShape,
                color    = FintechColors.NavyDark.copy(alpha = ringAlpha * 0.6f),
                modifier = Modifier.size(88.dp)
            ) {}
        }

        // Main icon circle
        Surface(
            shape    = CircleShape,
            color    = bgColor,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                // Spinner overlay for VERIFYING
                if (state == OtpScreenState.VERIFYING) {
                    CircularProgressIndicator(
                        color       = FintechColors.NavyDark.copy(alpha = 0.3f),
                        modifier    = Modifier.size(64.dp),
                        strokeWidth = 2.dp
                    )
                }
                Icon(icon, null,
                    tint     = iconColor,
                    modifier = Modifier.size(34.dp))
            }
        }
    }
}


// ─────────────────────────────────────────────
// OTP BOX ROW — beautiful 4 or 6 digit boxes
// ─────────────────────────────────────────────

@Composable
private fun OtpBoxRow(
    otp:         String,
    length:      Int,
    state:       OtpScreenState,
    onOtpChange: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    val borderColor = when (state) {
        OtpScreenState.FAILED -> colorScheme.error
        OtpScreenState.SUCCESS -> FintechColors.SuccessGreen
        else -> FintechColors.NavyDark
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {

        // Visible OTP boxes
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            repeat(length) { index ->
                val char      = otp.getOrNull(index)?.toString() ?: ""
                val isFilled  = char.isNotEmpty()
                val isCurrent = otp.length == index && state == OtpScreenState.INPUT

                // Subtle shake animation on FAILED
                val shake by animateFloatAsState(
                    targetValue   = if (state == OtpScreenState.FAILED && isFilled) 0f else 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessHigh),
                    label         = "shake_$index"
                )

                Box(
                    modifier = Modifier
                        .size(if (length == 4) 60.dp else 48.dp)
                        .background(
                            when {
                                state == OtpScreenState.SUCCESS -> FintechColors.SuccessGreenLight
                                state == OtpScreenState.FAILED && isFilled -> colorScheme.errorContainer
                                isFilled -> FintechColors.NavyDark.copy(alpha = 0.07f)
                                else     -> colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = when {
                                isCurrent -> 2.dp
                                isFilled  -> 1.5.dp
                                else      -> 1.dp
                            },
                            color = when {
                                state == OtpScreenState.SUCCESS -> FintechColors.SuccessGreen
                                state == OtpScreenState.FAILED && isFilled -> colorScheme.error
                                isCurrent -> FintechColors.NavyDark
                                isFilled  -> FintechColors.NavyDark.copy(alpha = 0.5f)
                                else      -> colorScheme.outline.copy(alpha = 0.25f)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Cursor blink for current position
                    if (isCurrent) {
                        val blinkAlpha by rememberInfiniteTransition(label = "blink").animateFloat(
                            initialValue  = 1f,
                            targetValue   = 0f,
                            animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
                            label         = "cursor_alpha"
                        )
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(22.dp)
                                .background(
                                    FintechColors.NavyDark.copy(alpha = blinkAlpha),
                                    RoundedCornerShape(1.dp)
                                )
                        )
                    } else {
                        // Show dot for filled, icon for success
                        when {
                            state == OtpScreenState.SUCCESS && isFilled -> {
                                Icon(Icons.Default.Check, null,
                                    tint     = FintechColors.SuccessGreen,
                                    modifier = Modifier.size(20.dp))
                            }
                            isFilled -> {
                                Text("•",
                                    fontSize   = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = when (state) {
                                        OtpScreenState.FAILED -> colorScheme.error
                                        else -> FintechColors.NavyDark
                                    })
                            }
                        }
                    }
                }
            }
        }

        // Hidden text field captures actual keyboard input
        OutlinedTextField(
            value         = otp,
            onValueChange = onOtpChange,
            modifier      = Modifier
                .fillMaxWidth()
                .height(if (length == 4) 60.dp else 48.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = Color.Transparent,
                unfocusedBorderColor    = Color.Transparent,
                focusedContainerColor   = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor        = Color.Transparent,
                unfocusedTextColor      = Color.Transparent,
                cursorColor             = Color.Transparent
            ),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword
            ),
            singleLine = true
        )
    }
}


// ─────────────────────────────────────────────
// SENDING INDICATOR
// ─────────────────────────────────────────────

@Composable
fun OtpSendingIndicator() {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LinearProgressIndicator(
            modifier   = Modifier
                .fillMaxWidth(0.55f)
                .clip(RoundedCornerShape(4.dp)),
            color      = FintechColors.NavyDark,
            trackColor = FintechColors.NavyDark.copy(alpha = 0.12f)
        )
        Text("Sending OTP, please wait...",
            style     = MaterialTheme.typography.labelSmall,
            color     = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center)
    }
}


// ─────────────────────────────────────────────
// INPUT CONTROLS
// ─────────────────────────────────────────────

@Composable
fun OtpInputControls(
    config:         OtpConfig,
    otp:            String,
    resendTimer:    Int,
    resendCount:    Int,
    onVerify:       () -> Unit,
    onResend:       () -> Unit,
    onChangeNumber: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isReady     = otp.length == config.otpLength

    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // Verify button — only if not auto-verify
        if (!config.autoVerify) {
            Button(
                onClick  = onVerify,
                enabled  = isReady,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = FintechColors.NavyDark,
                    contentColor           = Color.White,
                    disabledContainerColor = FintechColors.NavyDark.copy(alpha = 0.3f),
                    disabledContentColor   = Color.White.copy(alpha = 0.5f)
                )
            ) {
                Icon(Icons.Default.VerifiedUser, null,
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Verify OTP",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp)
            }
        } else {
            // Auto-verify hint
            Surface(
                shape    = RoundedCornerShape(10.dp),
                color    = FintechColors.NavyDark.copy(alpha = 0.06f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AutoMode, null,
                        tint     = FintechColors.NavyDark,
                        modifier = Modifier.size(15.dp))
                    Text("OTP will be verified automatically once you enter all ${config.otpLength} digits",
                        style = MaterialTheme.typography.labelSmall,
                        color = FintechColors.NavyDark)
                }
            }
        }

        HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))

        // Resend row
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            if (resendTimer > 0) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Countdown ring
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress      = { resendTimer.toFloat() / config.resendDelay.toFloat() },
                            modifier      = Modifier.size(22.dp),
                            color         = FintechColors.NavyDark,
                            trackColor    = FintechColors.NavyDark.copy(alpha = 0.12f),
                            strokeWidth   = 2.5.dp
                        )
                        Text("$resendTimer",
                            fontSize   = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color      = FintechColors.NavyDark)
                    }
                    Text("Resend OTP in ${resendTimer}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.outline)
                }
            } else {
                TextButton(onClick = onResend) {
                    Icon(Icons.Default.Refresh, null,
                        tint     = FintechColors.NavyDark,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Resend OTP",
                        color      = FintechColors.NavyDark,
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Resent count feedback
        if (resendCount > 0) {
            Text("OTP resent $resendCount time${if (resendCount > 1) "s" else ""}",
                style     = MaterialTheme.typography.labelSmall,
                color     = colorScheme.outline,
                textAlign = TextAlign.Center)
        }

        // Change number link
        if (config.mobile.isNotEmpty()) {
            TextButton(onClick = onChangeNumber) {
                Icon(Icons.Default.Edit, null,
                    tint     = colorScheme.outline,
                    modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
                Text("Change number",
                    color = colorScheme.outline,
                    style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}


// ─────────────────────────────────────────────
// VERIFYING INDICATOR
// ─────────────────────────────────────────────

@Composable
fun OtpVerifyingIndicator() {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LinearProgressIndicator(
            modifier   = Modifier
                .fillMaxWidth(0.55f)
                .clip(RoundedCornerShape(4.dp)),
            color      = FintechColors.NavyDark,
            trackColor = FintechColors.NavyDark.copy(alpha = 0.12f)
        )
        Text("Verifying your OTP...",
            style     = MaterialTheme.typography.labelSmall,
            color     = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center)
    }
}


// ─────────────────────────────────────────────
// SUCCESS BANNER
// ─────────────────────────────────────────────

@Composable
fun OtpSuccessBanner() {
    Surface(
        shape    = RoundedCornerShape(14.dp),
        color    = FintechColors.SuccessGreenLight,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FintechColors.SuccessGreen.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape    = CircleShape,
                color    = FintechColors.SuccessGreen,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Default.CheckCircle, null,
                        tint     = Color.White,
                        modifier = Modifier.size(22.dp))
                }
            }
            Column {
                Text("OTP Verified Successfully!",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = FintechColors.SuccessGreenDark)
                Text("Redirecting you now...",
                    style = MaterialTheme.typography.labelSmall,
                    color = FintechColors.SuccessGreen)
            }
        }
    }
}


// ─────────────────────────────────────────────
// FAILED CONTROLS
// ─────────────────────────────────────────────

@Composable
fun OtpFailedControls(
    attempts:    Int,
    maxAttempts: Int,
    onRetry:     () -> Unit,
    onResend:    () -> Unit
) {
    val colorScheme  = MaterialTheme.colorScheme
    val attemptsLeft = maxAttempts - attempts

    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Attempt dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            repeat(maxAttempts) { index ->
                val isUsed = index < attempts
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (isUsed) colorScheme.error
                            else colorScheme.outline.copy(alpha = 0.25f)
                        )
                )
            }
            Spacer(Modifier.width(6.dp))
            Text("$attemptsLeft attempt${if (attemptsLeft != 1) "s" else ""} left",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.error)
        }

        // Retry button
        Button(
            onClick  = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = colorScheme.error,
                contentColor   = Color.White
            )
        ) {
            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Try Again",
                fontWeight = FontWeight.Bold,
                fontSize   = 15.sp)
        }

        // Resend link
        TextButton(onClick = onResend) {
            Icon(Icons.Default.Sms, null,
                tint     = FintechColors.NavyDark,
                modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("Resend a new OTP",
                color      = FintechColors.NavyDark,
                fontWeight = FontWeight.Medium,
                style      = MaterialTheme.typography.bodySmall)
        }
    }
}


// ─────────────────────────────────────────────
// LOCKED BANNER
// ─────────────────────────────────────────────

@Composable
fun OtpLockedBanner(onBack: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape    = RoundedCornerShape(14.dp),
            color    = colorScheme.errorContainer,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colorScheme.error.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier            = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.Lock, null,
                    tint     = colorScheme.error,
                    modifier = Modifier.size(28.dp))
                Text("Verification Locked",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = colorScheme.error,
                    textAlign  = TextAlign.Center)
                Text("Maximum attempts reached. For security, OTP verification has been temporarily disabled.",
                    style     = MaterialTheme.typography.bodySmall,
                    color     = colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center)
            }
        }

        // Contact support
        Surface(
            shape    = RoundedCornerShape(12.dp),
            color    = FintechColors.NavyDark.copy(alpha = 0.06f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.SupportAgent, null,
                    tint     = FintechColors.NavyDark,
                    modifier = Modifier.size(20.dp))
                Column {
                    Text("Need help?",
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color      = FintechColors.NavyDark)
                    Text("Call 1800-XXX-XXXX or visit your nearest branch",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        OutlinedButton(
            onClick  = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.outlinedButtonColors(
                contentColor = FintechColors.NavyDark
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp, FintechColors.NavyDark.copy(alpha = 0.4f)
            )
        ) {
            Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Go Back", fontWeight = FontWeight.Medium)
        }
    }
}


// ══════════════════════════════════════════════════════════
//  PREVIEWS
// ══════════════════════════════════════════════════════════

@Preview(name = "OTP – Light (6-digit)", showBackground = true, showSystemUi = true)
@Preview(name = "OTP – Dark (6-digit)",  showBackground = true,
    showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewOtpScreen6() {
    MaterialTheme {
        OtpVerificationScreen(
            config = OtpConfig(
                title        = "OTP Verification",
                subtitle     = "Confirm your mobile number",
                purpose      = "AEPS Transaction",
                purposeIcon  = Icons.Default.Fingerprint,
                mobile       = "98XXXXX210",
                otpLength    = 6,
                autoVerify   = true
            )
        )
    }
}

@Preview(name = "OTP – 4-digit (Payment)", showBackground = true, showSystemUi = true)
@Composable
fun PreviewOtpScreen4() {
    MaterialTheme {
        OtpVerificationScreen(
            config = OtpConfig(
                title        = "Confirm Payment",
                subtitle     = "Enter the 4-digit OTP to confirm",
                purpose      = "DMT Transfer",
                purposeIcon  = Icons.Default.Send,
                purposeColor = Color(0xFF2E7D32),
                mobile       = "99XXXXX123",
                otpLength    = 4,
                autoVerify   = true,
                maxAttempts  = 5
            )
        )
    }
}

@Preview(name = "OTP Input Controls", showBackground = true)
@Composable
fun PreviewOtpInputControls() {
    MaterialTheme {
        Box(Modifier.padding(20.dp)) {
            OtpInputControls(
                config      = OtpConfig(mobile = "98XXXXX210", autoVerify = false),
                otp         = "1234",
                resendTimer = 18,
                resendCount = 0,
                onVerify    = {},
                onResend    = {},
                onChangeNumber = {}
            )
        }
    }
}

@Preview(name = "OTP Failed Controls", showBackground = true)
@Composable
fun PreviewOtpFailed() {
    MaterialTheme {
        Box(Modifier.padding(20.dp)) {
            OtpFailedControls(
                attempts    = 1,
                maxAttempts = 3,
                onRetry     = {},
                onResend    = {}
            )
        }
    }
}

@Preview(name = "OTP Locked Banner", showBackground = true)
@Composable
fun PreviewOtpLocked() {
    MaterialTheme {
        Box(Modifier.padding(20.dp)) {
            OtpLockedBanner(onBack = {})
        }
    }
}
