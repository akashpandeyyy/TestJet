package com.example.modernui.ui.screens.aeps

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AepsModel(

    @SerialName("aadhaar")
    val aadhaar: String,

    @SerialName("mobile")
    val mobile: String,

    @SerialName("amount")
    val amount: String,

    @SerialName("latitude")
    val latitude: String,

    @SerialName("longitude")
    val longitude: String,

    @SerialName("type")
    val type: String, // CW, BE, etc.

    @SerialName("iin")
    val iin: String, // "100011,ICICI BANK"

    @SerialName("source")
    val source: String = "APP",

    @SerialName("biometric")
    val biometric: String,

    @SerialName("fingerIndex")
    val fingerIndex: String? = null
)