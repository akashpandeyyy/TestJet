package com.example.modernui.ui.screens.dmt.jiomodel
import kotlinx.serialization.*

@Serializable
data class SendOtpRequest(
    val mobile: String,
    val email: String,
    val latitude: String?,
    val longitude: String?
)