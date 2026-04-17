package com.example.modernui.ui.screens.recharge.rechargemodel

import com.google.gson.JsonElement
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class RechargeResponse (
    val status: Int,
    val message: String,
    val errorMessage: JsonElement? = null,
    val data: Data
)
