package com.example.modernui.ui.screens.common


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TwoFAresponce(
    @SerialName("data")
    val `data`: String?,
    @SerialName("errorMessage")
    val errorMessage: Any?,
    @SerialName("message")
    val message: String?,
    @SerialName("status")
    val status: Int?
)