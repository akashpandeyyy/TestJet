package com.example.modernui.ui.screens.recharge.fetchmodel

import kotlinx.serialization.*
@Serializable
data class Details (
    val mobile: String,
    val operator: String,
    val circle: String
)
