package com.example.modernui.ui.screens.cms

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.res.Configuration
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.modernui.ui.components.*
import com.example.modernui.ui.theme.FintechColors
import kotlinx.coroutines.delay
import com.example.modernui.Api.model.Cmsresponce
import com.example.modernui.Api.model.InsuranceResponse


// ══════════════════════════════════════════════════════════
//  AIRTEL CMS SCREEN
//  Opens Airtel CMS URL in browser via Android Intent
// ══════════════════════════════════════════════════════════

// ─────────────────────────────────────────────
// CMS URL — change to actual Airtel CMS endpoint
// ─────────────────────────────────────────────

private const val AIRTEL_CMS_URL = "https://www.airtel.in/cms"

// ─────────────────────────────────────────────
// AIRTEL BRAND COLORS
// ─────────────────────────────────────────────

private val AirtelRed    = Color(0xFFE40000)
private val AirtelRedDark = Color(0xFFB71C1C)
private val AirtelRedLight = Color(0xFFFFEBEE)


@Composable
fun AirtelCmsScreen(
    onBackClick: () -> Unit = {},
    viewModel: CmsViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val cmsData by viewModel.cmsData.collectAsState()

    // Fetch data on load
    LaunchedEffect(Unit) {
        viewModel.fetchCmsData()
    }

    AirtelCmsContent(
        isLoading = isLoading,
        errorMessage = errorMessage,
        cmsData = cmsData,
        onBackClick = onBackClick,
        onResetError = { viewModel.resetError() }
    )
}

@Composable
fun AirtelCmsContent(
    isLoading: Boolean,
    errorMessage: String?,
    cmsData: Cmsresponce?,
    onBackClick: () -> Unit,
    onResetError: () -> Unit
) {
    val context     = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    // Animate the redirect button
    var showRedirectHint by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(400); showRedirectHint = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {

        // ── Top bar ───────────────────────────
        DetailTopBar(
            title       = "Airtel CMS",
            onBackClick = onBackClick
        )

        if (errorMessage != null) {
            ErrorMessageBanner(message = errorMessage, onDismiss = onResetError)
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AirtelRed)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // ── Airtel branded header ─────────
                AirtelHeaderCard()

                // ── What is Airtel CMS ────────────
                SectionCard(title = "About Airtel CMS", icon = Icons.Default.Info) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Airtel CMS (Content Management System) is the official Airtel portal for retailers and B2B partners to manage transactions, view commission reports, and access exclusive business tools.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurface.copy(alpha = 0.85f),
                            lineHeight = 20.sp
                        )
                    }
                }

                // ── Features ─────────────────────
                SectionCard(title = "CMS Features", icon = Icons.Default.Dashboard) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        val features = listOf(
                            Icons.Default.ReceiptLong   to "View & download commission statements",
                            Icons.Default.AccountBalance to "Manage B2B wallet & payouts",
                            Icons.Default.Inventory      to "Track SIM & data card inventory",
                            Icons.Default.Assessment     to "Sales reports & analytics",
                            Icons.Default.SupportAgent   to "Raise & track support tickets",
                            Icons.Default.Notifications  to "Notifications & scheme alerts",
                        )
                        features.forEach { (icon, label) ->
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape    = CircleShape,
                                    color    = AirtelRed.copy(alpha = 0.1f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()) {
                                        Icon(icon, null,
                                            tint     = AirtelRed,
                                            modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text(label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurface.copy(alpha = 0.85f))
                            }
                        }
                    }
                }

                // ── Redirect CTA ──────────────────
                val currentCmsUrl = cmsData?.data?.cmsLink ?: AIRTEL_CMS_URL
                AnimatedVisibility(
                    visible = showRedirectHint,
                    enter   = fadeIn() + slideInVertically { it / 3 }
                ) {
                    CmsRedirectCard(
                        url     = currentCmsUrl,
                        context = context
                    )
                }

                // ── Note ─────────────────────────
                Surface(
                    shape    = RoundedCornerShape(10.dp),
                    color    = colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, null,
                            tint     = colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp))
                        Text(
                            "You will be redirected to the official Airtel CMS portal in your default browser. Please ensure you have your Airtel credentials ready.",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}


// ─────────────────────────────────────────────
// AIRTEL HEADER CARD
// ─────────────────────────────────────────────

@Composable
fun AirtelHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(18.dp),
        colors   = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(AirtelRedDark, AirtelRed)
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Airtel logo placeholder circle
                Surface(
                    shape    = CircleShape,
                    color    = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()) {
                        Text(
                            "Airtel",
                            color      = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 11.sp,
                            textAlign  = TextAlign.Center
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Airtel CMS Portal",
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography.titleMedium)
                    Text("Content & Business Management",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text("B2B Partner Portal",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color    = Color.White,
                            style    = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
// CMS REDIRECT CARD
// The main "Click here" CTA that opens the browser
// ─────────────────────────────────────────────

@Composable
fun CmsRedirectCard(
    url:     String,
    context: Context
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(listOf(AirtelRedDark, AirtelRed)),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                modifier            = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape    = CircleShape,
                    color    = AirtelRed.copy(alpha = 0.1f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.OpenInBrowser, null,
                            tint     = AirtelRed,
                            modifier = Modifier.size(30.dp))
                    }
                }

                Text("Access Airtel CMS Portal",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    textAlign  = TextAlign.Center)

                Text("Tap the button below to open the Airtel CMS portal in your browser",
                    style     = MaterialTheme.typography.bodySmall,
                    color     = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center)

                // URL preview chip
                Surface(
                    shape    = RoundedCornerShape(8.dp),
                    color    = AirtelRedLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Link, null,
                            tint     = AirtelRed,
                            modifier = Modifier.size(16.dp))
                        Text(
                            url,
                            style          = MaterialTheme.typography.labelSmall,
                            color          = AirtelRed,
                            fontWeight     = FontWeight.Medium,
                            textDecoration = TextDecoration.Underline,
                            modifier       = Modifier.weight(1f)
                        )
                    }
                }

                // Main CTA button — fires Intent
                Button(
                    onClick  = { openUrlInBrowser(context, url) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AirtelRed,
                        contentColor   = Color.White
                    )
                ) {
                    Icon(Icons.Default.OpenInNew, null,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Click Here to Open CMS",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp)
                }

                // Secondary text link
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier              = Modifier.clickable { openUrlInBrowser(context, url) }
                ) {
                    Icon(Icons.Default.Launch, null,
                        tint     = AirtelRed,
                        modifier = Modifier.size(14.dp))
                    Text(
                        "or click here to redirect",
                        style          = MaterialTheme.typography.labelSmall,
                        color          = AirtelRed,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
// INTENT HELPER — opens URL in default browser
// ─────────────────────────────────────────────

fun openUrlInBrowser(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback: try with chooser if no default browser
        val chooser = Intent.createChooser(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)),
            "Open with"
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}


// ─────────────────────────────────────────────
// INSURANCE BRAND COLORS
// ─────────────────────────────────────────────

private val InsuranceNavyBlue  = Color(0xFF0D47A1)
private val InsuranceNavyDark  = Color(0xFF002171)
private val InsuranceBlueLight = Color(0xFFE3F2FD)


@Composable
fun InsuranceHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(18.dp),
        colors   = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(InsuranceNavyDark, InsuranceNavyBlue)
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape    = CircleShape,
                    color    = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Security, null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Insurance Services",
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography.titleMedium)
                    Text("Comprehensive Protection Plans",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}


@Composable
fun InsuranceDetailScreen(
    onBackClick: () -> Unit = {},
    viewModel: CmsViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val insuranceData by viewModel.insuranceData.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()


    // Screen load hote hi link generate karne ka trigger
    LaunchedEffect(Unit) {
        viewModel.fetchInsuranceLead()
    }

    InsuranceDetailContent(
        isLoading = isLoading,
        insuranceData = insuranceData,
        errorMessage = errorMessage,
        onBackClick = onBackClick,
        onResetError = { viewModel.resetError() }
    )
}

@Composable
fun InsuranceDetailContent(
    isLoading: Boolean,
    insuranceData: InsuranceResponse?,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onResetError: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top Bar ───────────────────────────
        DetailTopBar(title = "Insurance Portal", onBackClick = onBackClick)

        if (errorMessage != null) {
            ErrorMessageBanner(message = errorMessage, onDismiss = onResetError)
        }

        Box(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                // Jab tak link generate ho raha hai
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = InsuranceNavyBlue)
                    Spacer(Modifier.height(12.dp))
                    Text("Generating secure link...", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                // Link generate hone ke baad direct Redirect Card
                val currentUrl = insuranceData?.data?.url ?: ""

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header (Chota aur clean)
                    InsuranceHeaderCard()

                    // Main Redirector (Airtel CMS wala style)
                    InsuranceRedirectCard(
                        url = currentUrl,
                        context = context
                    )

                    Text(
                        "Please click the button above to continue to the insurance portal.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun InsuranceRedirectCard(url: String, context: Context) {
    // Border wala clean card jaise CMS mein tha
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(listOf(InsuranceNavyDark, InsuranceNavyBlue)),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.OpenInBrowser,
                null,
                tint = InsuranceNavyBlue,
                modifier = Modifier.size(48.dp)
            )

            Text(
                "Insurance Link Ready",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // CTA Button
            Button(
                onClick = { if (url.isNotEmpty()) openUrlInBrowser(context, url) },
                enabled = url.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = InsuranceNavyBlue)
            ) {
                Text("OPEN PORTAL NOW", fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}
// ─────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────

@Preview(name = "Airtel CMS – Light", showBackground = true)
@Preview(name = "Airtel CMS – Dark",  showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewAirtelCmsScreen() {
    MaterialTheme {
        AirtelCmsContent(
            isLoading = false,
            errorMessage = null,
            cmsData = null,
            onBackClick = {},
            onResetError = {}
        )
    }
}

@Preview(name = "Insurance Detail – Light", showBackground = true)
@Preview(name = "Insurance Detail – Dark",  showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewInsuranceDetailScreen() {
    MaterialTheme {
        InsuranceDetailContent(
            isLoading = false,
            insuranceData = null,
            errorMessage = null,
            onBackClick = {},
            onResetError = {}
        )
    }
}

