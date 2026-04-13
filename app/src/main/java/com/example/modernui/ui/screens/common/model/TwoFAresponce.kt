package com.example.modernui.ui.screens.common.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName


data class TwoFAresponce(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String?,
    @SerializedName("errorMessage") val errorMessage: String?,
    @SerializedName("data") val data: JsonElement?
)