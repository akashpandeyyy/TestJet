package com.example.modernui.ui.screens.aeps


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AepsModel(
    @SerialName("aadhaar")
    val aadhaar: String?,
    @SerialName("amount")
    val amount: String?,
    @SerialName("biometric")
    val biometric: String?,
    @SerialName("iin")
    val iin: String?,
    @SerialName("latitude")
    val latitude: String?,
    @SerialName("longitude")
    val longitude: String?,
    @SerialName("mobile")
    val mobile: String?,
    @SerialName("source")
    val source: String?,
    @SerialName("type")
    val type: String?,
    @SerialName("fingerIndex")
    val fingerIndex: String? = null
)