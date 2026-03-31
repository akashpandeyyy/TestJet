package com.example.modernui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.SendToMobile
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NavyDark   = Color(0xFF0B0E71)
private val NavyLight  = Color(0xFF1A1F8F)

@Composable
fun AlternativeHomeScreen() {
    val serviceItems = listOf(
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

    val bannerSlides = listOf(
        BannerSlide("Cashback Offer",    "Get 10% back on bill payments", Color(0xFF1565C0)),
        BannerSlide("Send Money Free",   "Zero fee transfers this week",  Color(0xFF2E7D32)),
        BannerSlide("New: UPI Autopay",  "Set up recurring payments",     Color(0xFF6A1B9A)),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WalletBalanceCard(balance = "₹932.95")

        Text(
            text = "Offers",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NavyDark
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            bannerSlides.forEach { slide ->
                Card(
                    modifier = Modifier.width(280.dp).height(100.dp),
                    colors = CardDefaults.cardColors(containerColor = slide.color),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(slide.title, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(slide.subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }
            }
        }

        Text(
            text = "Services",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NavyDark
        )

        ServiceGrid(items = serviceItems)
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}
