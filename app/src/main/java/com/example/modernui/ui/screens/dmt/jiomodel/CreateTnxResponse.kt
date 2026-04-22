package com.example.modernui.ui.screens.dmt.jiomodel

import com.google.gson.JsonElement
import kotlinx.serialization.*


@Serializable
data class CreateTnxResponse (
    val status: Int,
    val message: String,
    val errorMessage: JsonElement? = null, // This can be JsonElement or JsonObject
    val data: Datta
)

@Serializable
data class Datta (
    val mobile: String,

    @SerialName("requestId")
    val requestId: String,

    val partnerRef: String
)