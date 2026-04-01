package com.example.modernui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// Shared Data Models
data class ServiceItem(val title: String, val icon: ImageVector)
data class BannerSlide(val title: String, val subtitle: String, val color: Color)

// Shared Branding Colors
object AppColors {
    val NavyDark   = Color(0xFF1B1E9B)
    val NavyAlpha  = Color(0xCF0B0E71)
    val NavyLight  = Color(0xFF1A1F8F)
    val PowerRed   = Color(0xFF8B0000)
}