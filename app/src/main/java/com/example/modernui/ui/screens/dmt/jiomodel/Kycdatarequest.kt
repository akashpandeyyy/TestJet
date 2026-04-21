package com.example.modernui.ui.screens.dmt.jiomodel
import com.google.gson.annotations.SerializedName

data class KycDataRequest(
    @SerializedName("mobile") val mobile: String,
    @SerializedName("aadhaar") val aadhaar: String,
    @SerializedName("biometric") val biometric: String,
    @SerializedName("latitude") val latitude: String?,
    @SerializedName("longitude") val longitude: String?
)