package com.example.modernui.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// Shared Data Models
data class BannerSlide(val title: String, val subtitle: String, val color: Color)

data class ServiceItem(val title: String, val icon: ImageVector)

data class FingerprintDevice(
    val id:           String,
    val name:         String,
    val model:        String,
    val manufacturer: String,
    val isConnected:  Boolean,
    val icon:         ImageVector = Icons.Default.Fingerprint
)

// Shared Branding Colors
object AppColors {
    val NavyDark   = Color(0xFF1B1E9B)
    val NavyAlpha  = Color(0xCF0B0E71)
    val NavyLight  = Color(0xFF1A1F8F)
    val PowerRed   = Color(0xFF8B0000)
}

val fingerprintDevices = listOf(
    FingerprintDevice(
        id           = "mantra_mfs100",
        name         = "Mantra MFS100",
        model        = "MFS100",
        manufacturer = "Mantra Softech",
        isConnected  = true
    ),
    FingerprintDevice(
        id           = "mantra_mfs110",
        name         = "Mantra MFS110",
        model        = "MFS110",
        manufacturer = "Mantra Softech",
        isConnected  = false
    ),
    FingerprintDevice(
        id           = "morpho_l1",
        name         = "Morpho - L1",
        model        = "MSO 1300 E3",
        manufacturer = "IDEMIA (Morpho)",
        isConnected  = false
    ),
    FingerprintDevice(
        id           = "mantra_iris",
        name         = "Mantra IRIS",
        model        = "MIS100V2",
        manufacturer = "Mantra Softech",
        isConnected  = false
    ),
    FingerprintDevice(
        id           = "face_scan",
        name         = "Face Scan",
        model        = "Face",
        manufacturer = "Generic",
        isConnected  = false
    )
)
