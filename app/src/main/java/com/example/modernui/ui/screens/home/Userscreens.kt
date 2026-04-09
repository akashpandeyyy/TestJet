package com.example.modernui.ui.screens.home

import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.SendToMobile
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.modernui.ui.theme.BannerSlide
import com.example.modernui.ui.theme.ServiceItem
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────
// COLORS (local to this file for now)
// ─────────────────────────────────────────────

private val NavyAlpha  = Color(0xCF0B0E71)          // your original #CF0B0E71
private val NavyDark   = Color(0xFF0B0E71)
private val NavyLight  = Color(0xFF283593)
private val PowerRed   = Color(0xFF8B0000)


// ─────────────────────────────────────────────
// DATA & CONSTANTS
// ─────────────────────────────────────────────

private val serviceItems = listOf(
    ServiceItem("AEPS", Icons.Default.Fingerprint),
    ServiceItem("Cash Deposit",      Icons.Default.AccountBalance),
    ServiceItem("AEPS2",             Icons.Default.Fingerprint),
    ServiceItem("Aadhar Pay",        Icons.Default.Pin),
    ServiceItem("Airtel DMT",        Icons.AutoMirrored.Filled.SendToMobile),
    ServiceItem("Jio DMT",           Icons.AutoMirrored.Filled.SendToMobile),
    ServiceItem("NSDL Pan Apply",    Icons.Default.Badge),
    ServiceItem("Booking Insurance", Icons.Default.Security),
    ServiceItem("Airtel CMS",        Icons.Default.Router),
    ServiceItem("Mobile Recharge",   Icons.Default.Smartphone),
    ServiceItem("DTH Recharge",      Icons.Default.Tv),
    ServiceItem("BBPS",              Icons.AutoMirrored.Filled.ReceiptLong),
    ServiceItem("UTI PAN Apply",     Icons.Default.Badge),
    ServiceItem("Move To Bank",      Icons.AutoMirrored.Filled.TrendingFlat),
)

private val bannerSlides = listOf(
    BannerSlide("Cashback Offer", "Get 10% back on bill payments", Color(0xFF1565C0)),
    BannerSlide("Send Money Free",  "Zero fee transfers this week",  Color(0xFF2E7D32)),
    BannerSlide("New: UPI Autopay", "Set up recurring payments",     Color(0xFF6A1B9A)),
)


// ─────────────────────────────────────────────
// HOME CONTENT  (main screen)
// ─────────────────────────────────────────────

@Composable
fun HomeContent(
    viewModel: HomeViewModel = hiltViewModel(),
    onMenuClick:    () -> Unit       = {},
    onLogout:       () -> Unit       = {},
    onServiceClick: (String) -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val balance by viewModel.balance.collectAsState()
    val aepsBalance by viewModel.aepsBalance.collectAsState()
    val walletBalance by viewModel.walletBalance.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        // ... (Top Bar stays same) ...
        MainHomeTopBar(
            title       = "Home",
            onMenuClick = onMenuClick,
            onLogout    = onLogout
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Wallet Balance with detailed breakdown
            DetailedWalletBalanceCard(
                balance = balance,
                aepsBalance = aepsBalance,
                walletBalance = walletBalance
            )
            // ... (rest of the content) ...

            // Banner Slider
            BannerSlider(slides = bannerSlides)

            Text(
                text       = "Services",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = colorScheme.onBackground
            )

            // 4-column service grid
            // Not using LazyVerticalGrid inside scroll — fixed height grid instead
            ServiceGrid(items = serviceItems, onServiceClick = onServiceClick)

            Spacer(Modifier.height(16.dp))
        }
    }
}


// ─────────────────────────────────────────────
// WALLET BALANCE CARD
// ─────────────────────────────────────────────

@Composable
fun DetailedWalletBalanceCard(
    balance: String,
    aepsBalance: String,
    walletBalance: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(listOf(NavyDark, NavyLight))
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Balance",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelLarge)
                    Text(balance,
                        color      = Color.White,
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold)
                }
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BalanceSubItem(label = "Main Wallet", amount = walletBalance, icon = Icons.Default.Wallet)
                BalanceSubItem(label = "AEPS Wallet", amount = aepsBalance, icon = Icons.Default.Fingerprint)
            }
        }
    }
}

@Composable
private fun BalanceSubItem(label: String, amount: String, icon: ImageVector) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
        }
        Text(amount, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}


// ─────────────────────────────────────────────
// BANNER SLIDER (Auto-scrolling)
// ─────────────────────────────────────────────

@Composable
fun BannerSlider(slides: List<BannerSlide>) {
    // Basic auto-scrolling pager
    var currentPage by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while(true) {
            delay(3000)
            currentPage = (currentPage + 1) % slides.size
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Just showing the current slide for simplicity (or use HorizontalPager from Accompanist/Foundation)
        val slide = slides[currentPage]

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.horizontalGradient(listOf(slide.color, slide.color.copy(alpha = 0.7f))))
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

        // Indicators
        Row(
            modifier              = Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(slides.size) { index ->
                val isSelected = currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 20.dp else 6.dp, 6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) NavyDark else MaterialTheme.colorScheme.outline)
                )
            }
        }
    }
}


// ─────────────────────────────────────────────
// SERVICE GRID
// ─────────────────────────────────────────────

@Composable
fun ServiceGrid(
    items:          List<ServiceItem>,
    onServiceClick: (String) -> Unit = {}
) {
    // We'll use simple Rows since LazyVerticalGrid doesn't play well with VerticalScroll
    Card(
        modifier  = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.chunked(4).forEach { row ->
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { item ->
                        ServiceGridItem(
                            item     = item,
                            modifier = Modifier.weight(1f),
                            onClick  = { onServiceClick(item.title) }
                        )
                    }
                    // Fill empty slots
                    repeat(4 - row.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceGridItem(
    item:     ServiceItem,
    modifier: Modifier   = Modifier,
    onClick:  () -> Unit = {}
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(top = 8.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape    = RoundedCornerShape(16.dp),
            color    = NavyDark.copy(alpha = 0.08f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector        = item.icon,
                    contentDescription = item.title,
                    modifier           = Modifier.size(28.dp),
                    tint               = NavyDark
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text      = item.title,
            style     = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                lineHeight = 14.sp
            ),
            color     = Color.DarkGray,
            textAlign = TextAlign.Center,
            maxLines  = 2,
            modifier  = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )
    }
}


// ─────────────────────────────────────────────
// DRAWER CONTENT
// ─────────────────────────────────────────────

@Composable
fun HomeDrawerContent(
    userName: String = "User",
    onClose: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    ModalDrawerSheet(
        drawerContainerColor = colorScheme.surface
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Brush.verticalGradient(listOf(NavyDark, NavyLight)))
                .padding(20.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape    = CircleShape,
                    modifier = Modifier.size(60.dp),
                    color    = Color.White.copy(alpha = 0.2f)
                ) {
                    Icon(Icons.Default.Person, "", tint = Color.White, modifier = Modifier.padding(12.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Welcome Back", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    Text(userName,    color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Items
        Spacer(Modifier.height(12.dp))
        DrawerItem(Icons.Default.Home,      "Dashboard", true)
        DrawerItem(Icons.Default.AccountCircle, "Profile")
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp), thickness = 0.5.dp)
        DrawerItem(Icons.AutoMirrored.Filled.Help,      "Support & Help")
        DrawerItem(Icons.AutoMirrored.Filled.Logout,    "Logout", textColor = PowerRed)
    }
}

@Composable
private fun DrawerItem(
    icon:      ImageVector,
    label:     String,
    selected:  Boolean = false,
    textColor: Color   = Color.Unspecified
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label, color = if (selected) MaterialTheme.colorScheme.primary else textColor) },
        selected = selected,
        onClick = { },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent
        )
    )
}


// ─────────────────────────────────────────────
// PREVIEW
// ─────────────────────────────────────────────

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun PreviewHomeContent() {
    MaterialTheme {
        HomeContent()
    }
}