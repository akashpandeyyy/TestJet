package com.example.modernui.ui.screens.common.model

// for validate the token in po.sofmintindia
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class TwoFaValidationRequest(
    @SerializedName("token") val token: JsonElement?
)