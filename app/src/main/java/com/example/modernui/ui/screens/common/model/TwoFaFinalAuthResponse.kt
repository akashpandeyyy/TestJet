package com.example.modernui.ui.screens.common.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TwoFaFinalAuthResponse(
    @SerialName("status")
    val status: Int,
    @SerialName("message")
    val message: String?,
    @SerialName("errorMessage")
    val errorMessage: String?,
    @SerialName("data")
    val data: String? = null
)