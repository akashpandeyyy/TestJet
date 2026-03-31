package com.example.modernui

import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.SendToMobile
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


// ─────────────────────────────────────────────
// THEME COLORS  (matched to your #CF0B0E71 navy)
// ─────────────────────────────────────────────

private val NavyDark   = Color(0xFF0B0E71)          // solid navy
private val NavyAlpha  = Color(0xCF0B0E71)          // your original #CF0B0E71
private val NavyLight  = Color(0xFF1A1F8F)
private val PowerRed   = Color(0xFF8B0000)


// ─────────────────────────────────────────────
// DATA
// ─────────────────────────────────────────────

private val serviceItems = listOf(
    ServiceItem("Cash Deposit", Icons.Default.AccountBalance),
    ServiceItem("Send Money",   Icons.AutoMirrored.Filled.Send),
    ServiceItem("Withdraw",     Icons.Default.Money),
    ServiceItem("Move To Bank", Icons.Default.AccountBalanceWallet),
    ServiceItem("DMT",          Icons.AutoMirrored.Filled.SendToMobile),
    ServiceItem("AEPS",         Icons.Default.Fingerprint),
    ServiceItem("Micro ATM",    Icons.Default.Atm),
    ServiceItem("Aadhar Pay",   Icons.Default.Pin),
    ServiceItem("Bill Payment", Icons.AutoMirrored.Filled.ReceiptLong),
    ServiceItem("Recharge",     Icons.Default.PhoneAndroid),
    ServiceItem("Airtel CMS",   Icons.Default.Payments),
    ServiceItem("Broadband",    Icons.Default.Router),
    ServiceItem("History",      Icons.Default.History),
    ServiceItem("Investments",  Icons.AutoMirrored.Filled.TrendingUp),
    ServiceItem("Insurance",    Icons.Default.Shield),
    ServiceItem("Loans",        Icons.Default.CreditCard),
    ServiceItem("UPI",          Icons.Default.QrCode),
    ServiceItem("Settings",     Icons.Default.Settings),
    ServiceItem("Profile",      Icons.Default.Person),
)

private val bannerSlides = listOf(
    BannerSlide("Cashback Offer",    "Get 10% back on bill payments", Color(0xFF1565C0)),
    BannerSlide("Send Money Free",   "Zero fee transfers this week",  Color(0xFF2E7D32)),
    BannerSlide("New: UPI Autopay",  "Set up recurring payments",     Color(0xFF6A1B9A)),
)


// ─────────────────────────────────────────────
// MAIN SCREEN  (DrawerLayout equivalent)
// ─────────────────────────────────────────────

@Composable
fun HomeScreen(
    onLogout: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope       = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState   = drawerState,
        drawerContent = { HomeDrawerContent(onClose = { scope.launch { drawerState.close() } }) }
    ) {
        HomeContent(
            onMenuClick = { scope.launch { drawerState.open() } },
            onLogout    = onLogout
        )
    }
}


// ─────────────────────────────────────────────
// HOME CONTENT  (main screen)
// ─────────────────────────────────────────────

@Composable
fun HomeContent(
    onMenuClick: () -> Unit = {},
    onLogout:    () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {

        // ── TOP BAR ──────────────────────────────────
        HomeTopBar(
            title       = "Home",
            onMenuClick = onMenuClick,
            onLogout    = onLogout
        )

        // ── SCROLLABLE BODY ───────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Wallet balance card
            WalletBalanceCard(balance = "₹932.95")

            // Image slider / banner
            BannerSlider(slides = bannerSlides)

            // Services grid header
            Text(
                text       = "Services",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = colorScheme.onBackground
            )

            // 4-column service grid
            // Not using LazyVerticalGrid inside scroll — fixed height grid instead
            ServiceGrid(items = serviceItems)

            Spacer(Modifier.height(16.dp))
        }
    }
}


// ─────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────

@Composable
fun HomeTopBar(
    title:       String,
    onMenuClick: () -> Unit,
    onLogout:    () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .background(NavyAlpha)
            .padding(horizontal = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Hamburger menu
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
        }

        Text(
            text       = title,
            color      = Color.White,
            fontSize   = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.padding(start = 8.dp)
        )

        Spacer(Modifier.weight(1f))

        // QR scanner
        IconButton(onClick = {}) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = "QR", tint = Color.White)
        }

        // Notifications
        IconButton(onClick = {}) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
        }

        // Power / logout — red tint like your XML
        IconButton(onClick = onLogout) {
            Icon(Icons.Default.PowerSettingsNew, contentDescription = "Logout", tint = PowerRed)
        }

        // Three-dot menu
        IconButton(onClick = {}) {
            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
        }
    }
}


// ─────────────────────────────────────────────
// WALLET BALANCE CARD
// ─────────────────────────────────────────────

@Composable
fun WalletBalanceCard(balance: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(NavyDark, NavyLight)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text  = "Wallet Balance",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text       = balance,
                    color      = Color.White,
                    style      = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.buttonColors(containerColor = Color.White),
                    ) {
                        Text("Add Money", color = NavyDark, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick  = {},
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border   = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
                    ) {
                        Text("Transfer")
                    }
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
// BANNER SLIDER  (replaces ImageSlider library)
// ─────────────────────────────────────────────

@Composable
fun BannerSlider(slides: List<BannerSlide>) {
    val pagerState = rememberPagerState(pageCount = { slides.size })

    // Auto-scroll every 2 seconds — matches app:iss_period="2000"
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            val next = (pagerState.currentPage + 1) % slides.size
            pagerState.animateScrollToPage(next)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalPager(
            state    = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(20.dp))
        ) { page ->
            val slide = slides[page]
            Box(
                modifier        = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(slide.color, slide.color.copy(alpha = 0.7f))
                        )
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    Text(
                        text       = slide.title,
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text  = slide.subtitle,
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Dot indicators
        Row(
            modifier              = Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(slides.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 20.dp else 6.dp, 6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isSelected) NavyDark
                            else MaterialTheme.colorScheme.outline
                        )
                )
            }
        }
    }
}


// ─────────────────────────────────────────────
// SERVICE GRID  (4-column, replaces RecyclerView GridLayoutManager 4)
// ─────────────────────────────────────────────

@Composable
fun ServiceGrid(items: List<ServiceItem>) {
    val colorScheme = MaterialTheme.colorScheme

    // Fixed grid — avoids nested scroll conflict
    val rows = items.chunked(4)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { item ->
                    ServiceGridItem(
                        item     = item,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining slots in last row
                repeat(4 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ServiceGridItem(
    item:     ServiceItem,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier             = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { }
            .padding(vertical = 10.dp),
        horizontalAlignment  = Alignment.CenterHorizontally,
        verticalArrangement  = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            shape  = CircleShape,
            color  = colorScheme.primaryContainer,
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector        = item.icon,
                    contentDescription = item.title,
                    tint               = colorScheme.onPrimaryContainer,
                    modifier           = Modifier.size(26.dp)
                )
            }
        }
        Text(
            text      = item.title,
            style     = MaterialTheme.typography.labelSmall,
            color     = colorScheme.onBackground,
            maxLines  = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}


// ─────────────────────────────────────────────
// DRAWER CONTENT  (replaces menu_drawer layout)
// ─────────────────────────────────────────────

@Composable
fun HomeDrawerContent(onClose: () -> Unit = {}) {
    val colorScheme = MaterialTheme.colorScheme

    ModalDrawerSheet(
        drawerContainerColor = colorScheme.surface
    ) {
        // Drawer header — navy gradient like top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(NavyDark, NavyLight)
                    )
                )
                .padding(20.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    shape    = CircleShape,
                    color    = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            Icons.Default.Person, null,
                            tint     = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Text("Jane Doe",         color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("jane@example.com", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Drawer items
        listOf(
            Triple(Icons.Default.Home,           "Home",       true),
            Triple(Icons.Default.AccountBalance, "My Account", false),
            Triple(Icons.Default.History,        "History",    false),
            Triple(Icons.Default.Receipt,        "Statements", false),
            Triple(Icons.Default.Help,           "Support",    false),
            Triple(Icons.Default.Settings,       "Settings",   false),
        ).forEach { (icon, label, selected) ->
            NavigationDrawerItem(
                icon     = { Icon(icon, null) },
                label    = { Text(label) },
                selected = selected,
                onClick  = { onClose() },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }

        Spacer(Modifier.weight(1f))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))

        NavigationDrawerItem(
            icon     = { Icon(Icons.Default.PowerSettingsNew, null, tint = PowerRed) },
            label    = { Text("Logout", color = PowerRed) },
            selected = false,
            onClick  = { onClose() },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        Spacer(Modifier.height(16.dp))
    }
}


// ─────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────

@Preview(name = "Home – Light", showBackground = true)
@Preview(name = "Home – Dark",  showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewHomeContent() {
    MaterialTheme {
        HomeContent()
    }
}

@Preview(name = "Drawer – Light", showBackground = true)
@Preview(name = "Drawer – Dark",  showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewHomeDrawer() {
    MaterialTheme {
        HomeDrawerContent()
    }
}

@Preview(name = "Top Bar", showBackground = true, backgroundColor = 0xFF0B0E71)
@Composable
fun PreviewHomeTopBar() {
    MaterialTheme {
        HomeTopBar(title = "Home", onMenuClick = {}, onLogout = {})
    }
}