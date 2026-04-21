package com.example.modernui.ui.screens.dmt.jiomodel
import com.google.gson.annotations.SerializedName

data class SendOtpRequest(
    @SerializedName("mobile") val mobile: String,
    @SerializedName("email") val email: String,
    @SerializedName("latitude") val latitude: String,
    @SerializedName("longitude") val longitude: String
)