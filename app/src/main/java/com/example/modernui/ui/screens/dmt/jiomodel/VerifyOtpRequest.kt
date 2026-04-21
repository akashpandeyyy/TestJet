package com.example.modernui.ui.screens.dmt.jiomodel

import com.google.gson.annotations.SerializedName

data class VerifyOtpRequest(
    @SerializedName("mobile") val mobile: String,
    @SerializedName("otp") val otp: String,
    @SerializedName("latitude") val latitude: String,
    @SerializedName("longitude") val longitude: String
)
