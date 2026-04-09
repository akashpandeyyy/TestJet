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
    val context     = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

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
            ErrorMessageBanner(message = errorMessage!!, onDismiss = {})
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
                AnimatedVisibility(
                    visible = showRedirectHint,
                    enter   = fadeIn() + slideInVertically { it / 3 }
                ) {
                    CmsRedirectCard(
                        url     = AIRTEL_CMS_URL,
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


// ══════════════════════════════════════════════════════════
//  INSURANCE DETAIL SCREEN
//  Standalone screen separate from BookingInsuranceScreen
//  Shows insurance products, allows enquiry/quote booking
// ══════════════════════════════════════════════════════════

private val InsuranceNavyBlue = Color(0xFF0D47A1)
private val InsuranceBlueLight = Color(0xFFE3F2FD)

data class InsuranceProduct(
    val id:         String,
    val name:       String,
    val type:       String,
    val provider:   String,
    val premium:    String,
    val coverage:   String,
    val icon:       ImageVector,
    val color:      Color,
    val features:   List<String>,
    val claimRatio: String
)

private val insuranceProducts = listOf(
    InsuranceProduct(
        id         = "ip1",
        name       = "Star Health Assure",
        type       = "Health Insurance",
        provider   = "Star Health",
        premium    = "₹4,500/yr",
        coverage   = "₹3 Lakh",
        icon       = Icons.Default.HealthAndSafety,
        color      = Color(0xFF0277BD),
        features   = listOf("Cashless at 10,000+ hospitals", "No pre-policy medical test up to 45 yrs", "Day care procedures covered", "Free annual health check-up"),
        claimRatio = "92%"
    ),
    InsuranceProduct(
        id         = "ip2",
        name       = "LIC Jeevan Umang",
        type       = "Life Insurance",
        provider   = "LIC of India",
        premium    = "₹12,000/yr",
        coverage   = "₹50 Lakh",
        icon       = Icons.Default.Favorite,
        color      = Color(0xFFC62828),
        features   = listOf("Whole life plan with survival benefit", "Annual survival benefits from 100th year", "Bonus declared annually", "Tax benefit under 80C & 10(10D)"),
        claimRatio = "98%"
    ),
    InsuranceProduct(
        id         = "ip3",
        name       = "Bajaj Allianz Vehicle Shield",
        type       = "Vehicle Insurance",
        provider   = "Bajaj Allianz",
        premium    = "₹8,500/yr",
        coverage   = "IDV of Vehicle",
        icon       = Icons.Default.DirectionsCar,
        color      = Color(0xFF2E7D32),
        features   = listOf("Comprehensive own-damage cover", "Zero depreciation add-on", "24×7 roadside assistance", "Cashless at 4,000+ garages"),
        claimRatio = "88%"
    ),
    InsuranceProduct(
        id         = "ip4",
        name       = "Tata AIG Travel Guard",
        type       = "Travel Insurance",
        provider   = "Tata AIG",
        premium    = "₹1,200/trip",
        coverage   = "\$1 Lakh",
        icon       = Icons.Default.Flight,
        color      = Color(0xFF6A1B9A),
        features   = listOf("Medical emergency abroad", "Trip cancellation & delay", "Baggage & passport loss", "Adventure sports coverage"),
        claimRatio = "95%"
    ),
)

enum class InsuranceDetailStep {
    LIST,       // product cards
    DETAIL,     // selected product full detail
    ENQUIRY,    // fill name + mobile for quote
    SUCCESS     // enquiry submitted
}

@Composable
fun InsuranceDetailScreen(
    onBackClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    var step            by remember { mutableStateOf(InsuranceDetailStep.LIST) }
    var selectedProduct by remember { mutableStateOf<InsuranceProduct?>(null) }
    var enquiryName     by remember { mutableStateOf("") }
    var enquiryMobile   by remember { mutableStateOf("") }
    var enquiryEmail    by remember { mutableStateOf("") }
    var isSubmitting    by remember { mutableStateOf(false) }

    val mobileError  = enquiryMobile.isNotEmpty() && enquiryMobile.length != 10
    val enquiryReady = enquiryName.isNotBlank() && enquiryMobile.length == 10

    LaunchedEffect(isSubmitting) {
        if (isSubmitting) { delay(1800); isSubmitting = false; step = InsuranceDetailStep.SUCCESS }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {

        // ── Top bar ───────────────────────────
        DetailTopBar(
            title = when (step) {
                InsuranceDetailStep.LIST    -> "Insurance Products"
                InsuranceDetailStep.DETAIL  -> selectedProduct?.type ?: "Insurance"
                InsuranceDetailStep.ENQUIRY -> "Get a Free Quote"
                InsuranceDetailStep.SUCCESS -> "Enquiry Submitted"
            },
            onBackClick = {
                when (step) {
                    InsuranceDetailStep.DETAIL  -> step = InsuranceDetailStep.LIST
                    InsuranceDetailStep.ENQUIRY -> step = InsuranceDetailStep.DETAIL
                    InsuranceDetailStep.SUCCESS -> {
                        step            = InsuranceDetailStep.LIST
                        selectedProduct = null
                        enquiryName     = ""
                        enquiryMobile   = ""
                        enquiryEmail    = ""
                    }
                    else -> onBackClick()
                }
            }
        )

        AnimatedContent(
            targetState    = step,
            transitionSpec = {
                (fadeIn(tween(220)) + slideInHorizontally { it / 6 })
                    .togetherWith(fadeOut(tween(180)) + slideOutHorizontally { -it / 6 })
            },
            label = "insurance_detail_step"
        ) { currentStep ->
            when (currentStep) {

                // ── STEP 1: Product list ──────
                InsuranceDetailStep.LIST -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header
                        InsuranceListHeader()

                        Text("Our Products",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = colorScheme.onBackground)

                        insuranceProducts.forEach { product ->
                            InsuranceProductListCard(
                                product  = product,
                                onClick  = {
                                    selectedProduct = product
                                    step            = InsuranceDetailStep.DETAIL
                                }
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }

                // ── STEP 2: Product detail ────
                InsuranceDetailStep.DETAIL -> {
                    val product = selectedProduct!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InsuranceProductDetailCard(product = product)

                        // Features
                        SectionCard(title = "Key Features", icon = Icons.Default.Star) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                product.features.forEach { feature ->
                                    Row(
                                        verticalAlignment     = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            shape    = CircleShape,
                                            color    = product.color.copy(alpha = 0.12f),
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize()) {
                                                Icon(Icons.Default.Check, null,
                                                    tint     = product.color,
                                                    modifier = Modifier.size(12.dp))
                                            }
                                        }
                                        Text(feature,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colorScheme.onSurface.copy(alpha = 0.85f))
                                    }
                                }
                            }
                        }

                        // Claim ratio
                        SectionCard(title = "Claim Settlement", icon = Icons.Default.Verified) {
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Claim Settlement Ratio",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorScheme.outline)
                                    Text(product.claimRatio,
                                        style      = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color      = FintechColors.SuccessGreen)
                                }
                                Surface(
                                    shape    = CircleShape,
                                    color    = FintechColors.SuccessGreenLight,
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()) {
                                        Icon(Icons.Default.VerifiedUser, null,
                                            tint     = FintechColors.SuccessGreen,
                                            modifier = Modifier.size(28.dp))
                                    }
                                }
                            }
                        }

                        NavyPrimaryButton(
                            text    = "Get Free Quote",
                            onClick = { step = InsuranceDetailStep.ENQUIRY },
                            icon    = Icons.Default.RequestQuote
                        )

                        Spacer(Modifier.height(8.dp))
                    }
                }

                // ── STEP 3: Enquiry form ──────
                InsuranceDetailStep.ENQUIRY -> {
                    val product = selectedProduct!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Product summary strip
                        ProductSummaryStrip(product = product)

                        SectionCard(title = "Your Details", icon = Icons.Default.Person) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                NavyOutlinedField(
                                    value         = enquiryName,
                                    onValueChange = { enquiryName = it },
                                    label         = "Full Name *",
                                    placeholder   = "Enter your full name",
                                    leadingIcon   = Icons.Default.Person
                                )
                                NavyOutlinedField(
                                    value         = enquiryMobile,
                                    onValueChange = {
                                        if (it.all(Char::isDigit)) enquiryMobile = it
                                    },
                                    label         = "Mobile Number *",
                                    placeholder   = "10-digit mobile number",
                                    leadingIcon   = Icons.Default.Phone,
                                    keyboardType  = androidx.compose.ui.text.input.KeyboardType.Phone,
                                    maxLength     = 10,
                                    isError       = mobileError,
                                    errorMessage  = "Enter valid 10-digit number",
                                    trailingIcon  = if (enquiryMobile.length == 10) ({
                                        Icon(Icons.Default.CheckCircle, null,
                                            tint = FintechColors.SuccessGreen)
                                    }) else null
                                )
                                NavyOutlinedField(
                                    value         = enquiryEmail,
                                    onValueChange = { enquiryEmail = it },
                                    label         = "Email (optional)",
                                    placeholder   = "your@email.com",
                                    leadingIcon   = Icons.Default.Email,
                                    keyboardType  = androidx.compose.ui.text.input.KeyboardType.Email
                                )
                            }
                        }

                        // Consent note
                        Surface(
                            shape    = RoundedCornerShape(10.dp),
                            color    = colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment     = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Info, null,
                                    tint     = colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(16.dp))
                                Text("By submitting, you agree to be contacted by our insurance advisor. No spam — just one call.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSecondaryContainer)
                            }
                        }

                        NavyPrimaryButton(
                            text    = if (isSubmitting) "Submitting..." else "Submit Enquiry",
                            onClick = { isSubmitting = true },
                            enabled = enquiryReady && !isSubmitting,
                            icon    = Icons.Default.Send
                        )

                        Spacer(Modifier.height(8.dp))
                    }
                }

                // ── STEP 4: Success ───────────
                InsuranceDetailStep.SUCCESS -> {
                    InsuranceEnquirySuccess(
                        name    = enquiryName,
                        mobile  = enquiryMobile,
                        product = selectedProduct!!,
                        onDone  = {
                            step            = InsuranceDetailStep.LIST
                            selectedProduct = null
                            enquiryName     = ""
                            enquiryMobile   = ""
                            enquiryEmail    = ""
                        }
                    )
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
// INSURANCE LIST HEADER
// ─────────────────────────────────────────────

@Composable
fun InsuranceListHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(InsuranceNavyBlue, Color(0xFF1565C0))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape    = CircleShape,
                    color    = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Security, null,
                            tint     = Color.White,
                            modifier = Modifier.size(30.dp))
                    }
                }
                Column {
                    Text("Insurance Products",
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography.titleMedium)
                    Text("Protect what matters most",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Health", "Life", "Vehicle", "Travel").forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.18f)
                            ) {
                                Text(tag,
                                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    color      = Color.White,
                                    style      = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
// INSURANCE PRODUCT LIST CARD
// ─────────────────────────────────────────────

@Composable
fun InsuranceProductListCard(
    product: InsuranceProduct,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icon
            Surface(
                shape    = RoundedCornerShape(14.dp),
                color    = product.color.copy(alpha = 0.1f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(product.icon, null,
                        tint     = product.color,
                        modifier = Modifier.size(28.dp))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(product.name,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color      = colorScheme.onSurface)
                Text("${product.type} • ${product.provider}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.outline)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Premium chip
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = product.color.copy(alpha = 0.1f)
                    ) {
                        Text(product.premium,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color      = product.color)
                    }
                    // Coverage chip
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ) {
                        Text("Cover: ${product.coverage}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = colorScheme.outline)
                    }
                }
            }

            Icon(Icons.Default.ChevronRight, null,
                tint     = colorScheme.outline,
                modifier = Modifier.size(20.dp))
        }
    }
}


// ─────────────────────────────────────────────
// PRODUCT DETAIL CARD (top of detail screen)
// ─────────────────────────────────────────────

@Composable
fun InsuranceProductDetailCard(product: InsuranceProduct) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(product.color, product.color.copy(alpha = 0.75f))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape    = CircleShape,
                        color    = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()) {
                            Icon(product.icon, null,
                                tint     = Color.White,
                                modifier = Modifier.size(28.dp))
                        }
                    }
                    Column {
                        Text(product.name,
                            color      = Color.White,
                            fontWeight = FontWeight.Bold,
                            style      = MaterialTheme.typography.titleMedium)
                        Text(product.provider,
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf(
                        "Premium"  to product.premium,
                        "Coverage" to product.coverage,
                        "Claim"    to product.claimRatio
                    ).forEach { (label, value) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label,
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelSmall)
                            Text(value,
                                color      = Color.White,
                                fontWeight = FontWeight.Bold,
                                style      = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
// PRODUCT SUMMARY STRIP
// ─────────────────────────────────────────────

@Composable
fun ProductSummaryStrip(product: InsuranceProduct) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = product.color.copy(alpha = 0.08f),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, product.color.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape    = CircleShape,
                color    = product.color.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(product.icon, null,
                        tint     = product.color,
                        modifier = Modifier.size(22.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name,
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color      = product.color)
                Text("${product.premium} • ${product.coverage}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.outline)
            }
        }
    }
}


// ─────────────────────────────────────────────
// ENQUIRY SUCCESS SCREEN
// ─────────────────────────────────────────────

@Composable
fun InsuranceEnquirySuccess(
    name:    String,
    mobile:  String,
    product: InsuranceProduct,
    onDone:  () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }

    val scale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "enquiry_scale"
    )
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier            = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(28.dp))

        Surface(
            shape    = CircleShape,
            color    = FintechColors.SuccessGreenLight,
            modifier = Modifier.size(100.dp).scale(scale)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Default.MarkEmailRead, null,
                    tint     = FintechColors.SuccessGreen,
                    modifier = Modifier.size(54.dp))
            }
        }

        Text("Enquiry Submitted!",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color      = FintechColors.SuccessGreenDark,
            textAlign  = TextAlign.Center)

        Text("Thank you $name! Our advisor will call you on $mobile within 24 hours with the best quote for ${product.name}.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = colorScheme.outline,
            textAlign = TextAlign.Center)

        // Summary card
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(14.dp),
            colors    = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(product.icon, null,
                        tint = product.color, modifier = Modifier.size(18.dp))
                    Text("Enquiry Summary",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = product.color)
                }
                HorizontalDivider(color = product.color.copy(alpha = 0.15f))
                listOf(
                    "Product" to product.name,
                    "Provider" to product.provider,
                    "Name"    to name,
                    "Mobile"  to mobile,
                    "Status"  to "Under Review"
                ).forEach { (label, value) ->
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(label,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.outline)
                        Text(value,
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color      = if (label == "Status") Color(0xFFF57C00)
                            else colorScheme.onSurface)
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        NavyPrimaryButton(
            text    = "Explore More Plans",
            onClick = onDone,
            icon    = Icons.Default.ArrowBack
        )
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
    MaterialTheme { AirtelCmsScreen() }
}

@Preview(name = "Insurance Detail – Light", showBackground = true)
@Preview(name = "Insurance Detail – Dark",  showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewInsuranceDetailScreen() {
    MaterialTheme { InsuranceDetailScreen() }
}

@Preview(name = "CMS Redirect Card", showBackground = true)
@Composable
fun PreviewCmsRedirectCard() {
    MaterialTheme {
        Box(Modifier.padding(16.dp)) {
            CmsRedirectCard(url = AIRTEL_CMS_URL, context = LocalContext.current)
        }
    }
}

