package com.example.modernui.ui.screens.recharge.fetchmodel
import kotlinx.serialization.*

@Serializable
data class PlanDetail (
    val amount: String,
    val description: String,
    val validity: String
)