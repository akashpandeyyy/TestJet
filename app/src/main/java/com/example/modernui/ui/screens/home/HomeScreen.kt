package com.example.modernui.ui.screens.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
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
import com.example.modernui.ui.theme.AppColors
import com.example.modernui.ui.theme.BannerSlide
import com.example.modernui.ui.theme.ServiceItem
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────
// DATA & CONSTANTS
// ─────────────────────────────────────────────

private val serviceItems = listOf(
    ServiceItem("AEPS",              Icons.Default.Fingerprint),
    ServiceItem("Cash Deposit",      Icons.Default.AccountBalance),
    ServiceItem("AEPS2",             Icons.Default.Fingerprint),
    ServiceItem("Aadhar Pay",        Icons.Default.Pin),
    ServiceItem("Airtel DMT", Icons.AutoMirrored.Filled.SendToMobile),
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
// HOME CONTENT
// ─────────────────────────────────────────────

@Composable
fun MainHomeContent(
    onMenuClick:    () -> Unit       = {},
    onLogout:       () -> Unit       = {},
    onServiceClick: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val balance by viewModel.balance.collectAsState()
    val aepsBalance by viewModel.aepsBalance.collectAsState()
    val walletBalance by viewModel.walletBalance.collectAsState()
    val userName by viewModel.userName.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        MainHomeTopBar(title = "Home", onMenuClick = onMenuClick, onLogout = onLogout)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MainWalletBalanceCard(
                balance = balance,
                aepsBalance = aepsBalance,
                walletBalance = walletBalance
            )
            MainBannerSlider(slides = bannerSlides)

            Text(
                text       = "Services",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = colorScheme.onBackground
            )

            MainServiceGrid(items = serviceItems, onServiceClick = onServiceClick)

            Spacer(Modifier.height(16.dp))
        }
    }
}


// ─────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────

@Composable
fun MainHomeTopBar(
    title:       String,
    onMenuClick: () -> Unit,
    onLogout:    () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .background(AppColors.NavyAlpha)
            .padding(horizontal = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
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

        IconButton(onClick = {}) {
            Icon(Icons.Default.Notifications, "Notifications", tint = Color.White)
        }
    }
}


// ─────────────────────────────────────────────
// WALLET BALANCE CARD
// ─────────────────────────────────────────────

@Composable
fun MainWalletBalanceCard(
    balance: String,
    aepsBalance: String = "₹0.00",
    walletBalance: String = "₹0.00"
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
                    brush = Brush.horizontalGradient(listOf(AppColors.NavyDark, AppColors.NavyLight))
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
                BalanceItem(label = "Wallet", amount = walletBalance, icon = Icons.Default.Wallet)
                BalanceItem(label = "AEPS", amount = aepsBalance, icon = Icons.Default.Fingerprint)
            }
        }
    }
}

@Composable
private fun BalanceItem(label: String, amount: String, icon: ImageVector) {
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
// BANNER SLIDER
// ─────────────────────────────────────────────

@Composable
fun MainBannerSlider(slides: List<BannerSlide>) {
    val pagerState = rememberPagerState(pageCount = { slides.size })
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            pagerState.animateScrollToPage((pagerState.currentPage + 1) % slides.size)
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalPager(
            state    = pagerState,
            modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(20.dp))
        ) { page ->
            val slide = slides[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(listOf(slide.color, slide.color.copy(alpha = 0.7f)))
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    Text(slide.title,    color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(slide.subtitle, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
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
                        .background(if (isSelected) AppColors.NavyDark else MaterialTheme.colorScheme.outline)
                )
            }
        }
    }
}


// ─────────────────────────────────────────────
// SERVICE GRID
// ─────────────────────────────────────────────

@Composable
fun MainServiceGrid(
    items:          List<ServiceItem>,
    onServiceClick: (String) -> Unit = {}
) {
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
                        MainServiceGridItem(
                            item     = item,
                            modifier = Modifier.weight(1f),
                            onClick  = { onServiceClick(item.title) }
                        )
                    }
                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
// SERVICE GRID ITEM
// ─────────────────────────────────────────────

@Composable
fun MainServiceGridItem(
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
            color    = AppColors.NavyDark.copy(alpha = 0.08f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector        = item.icon,
                    contentDescription = item.title,
                    modifier           = Modifier.size(28.dp),
                    tint               = AppColors.NavyDark
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
fun MainHomeDrawerContent(
    onClose: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val userName by viewModel.userName.collectAsState()

    ModalDrawerSheet(drawerContainerColor = colorScheme.surface) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Brush.verticalGradient(listOf(AppColors.NavyDark, AppColors.NavyLight)))
                .padding(20.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(56.dp)) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
                Text(userName,         color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("jane@example.com", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(8.dp))
        listOf(
            Triple(Icons.Default.Home,           "Home",       true),
            Triple(Icons.Default.AccountBalance, "My Account", false),
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
            icon     = { Icon(Icons.Default.PowerSettingsNew, null, tint = AppColors.PowerRed) },
            label    = { Text("Logout", color = AppColors.PowerRed) },
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
fun MainPreviewHomeContent() {
    MaterialTheme { MainHomeContent() }
}

@Preview(name = "Drawer – Light", showBackground = true)
@Composable
fun MainPreviewHomeDrawer() {
    MaterialTheme { MainHomeDrawerContent() }
}

@Preview(name = "Top Bar", showBackground = true, backgroundColor = 0xFF0B0E71)
@Composable
fun MainPreviewHomeTopBar() {
    MaterialTheme { MainHomeTopBar(title = "Home", onMenuClick = {}) }
}
