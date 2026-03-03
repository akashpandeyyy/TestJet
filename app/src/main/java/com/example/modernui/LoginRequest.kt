package com.example.modernui

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("otpMode") val otpMode: String = "VIA_TEXT",
    @SerializedName("source") val source: String = "APP"
)
