package com.example.modernui.ui.screens.recharge.fetchmodel

import kotlinx.serialization.Serializable

@Serializable
data class Data (
    val details: Details,
    val plans: List<Plan>
)
