package com.example.modernui.ui.screens.recharge.fetchmodel

import com.google.gson.JsonElement
import kotlinx.serialization.*


@Serializable
data class FetchPlanResponse (
    val status: Int,
    val message: String,
    val errorMessage: JsonElement? = null,
    val data: Data
)