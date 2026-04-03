package com.example.modernui.ui.screens.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modernui.ui.theme.AppColors
import com.example.modernui.ui.theme.ModernUITheme

@Composable
fun ReportScreen(
    onMenuClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .background(AppColors.NavyAlpha)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
            }
            Text(
                text = "Reports",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {}) {
                Icon(Icons.Default.Notifications, "Notifications", tint = Color.White)
            }
        }

        // --- CONTENT ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Spending Analysis", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Monthly Summary", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val bars = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f)
                        bars.forEach { heightFraction ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                                    .fillMaxHeight(heightFraction)
                                    .background(colorScheme.primary, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            )
                        }
                    }
                }
            }

            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Top Categories", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    CategoryItem("Recharge Report", "₹4,200", 0.4f, Color(0xFFFFB74D))
                    CategoryItem("DMT Report", "₹3,100", 0.3f, Color(0xFF64B5F6))
                    CategoryItem("AEPS Report", "₹1,800", 0.2f, Color(0xFF81C784))
                }
            }
        }
    }
}

@Composable
fun CategoryItem(name: String, amount: String, progress: Float, color: Color) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(name, fontSize = 14.sp)
            Text(amount, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReportScreenPreview() {
    ModernUITheme {
        ReportScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryItemPreview() {
    ModernUITheme {
        CategoryItem(
            name = "Sample Category",
            amount = "₹1,234",
            progress = 0.6f,
            color = Color.Blue
        )
    }
}
