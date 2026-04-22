package com.example.modernui.ui.screens.dmt.jiomodel

import com.google.gson.JsonElement
import kotlinx.serialization.*

@Serializable
data class ReValidateTnxResponce (
    val status: Int,
    val message: String,
    val errorMessage: JsonElement? = null,
    val data: JsonElement? = null
)
