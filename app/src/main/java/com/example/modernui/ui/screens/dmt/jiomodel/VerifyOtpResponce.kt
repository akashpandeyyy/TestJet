package com.example.modernui.ui.screens.dmt.jiomodel

import com.google.gson.annotations.SerializedName

data class VerifyOtpResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("errorMessage") val errorMessage: String?,
    @SerializedName("data") val data: String?
)