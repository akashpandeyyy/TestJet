package com.example.modernui.ui.screens.common.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TwoFaAuthrequest(
    @SerialName("biometric")
    val biometric: String,
    @SerialName("orderId")
    val orderId: String?,
    @SerialName("fing")
    val fing: String,
    @SerialName("latitude")
val latitude: String?,

@SerialName("longitude")
val longitude: String?,

@SerialName("source")
val source: String? = "APP"
)
