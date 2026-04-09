package com.example.modernui.ui.screens.common


import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TwoFAresponce(

    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String?,
    @SerializedName("errorMessage") val errorMessage: String?,
    /**
     * Can be a string token (status=17) OR an object containing fields like fing1..fing4 (status=18).
     */
    @SerializedName("data") val data: JsonElement?
)
