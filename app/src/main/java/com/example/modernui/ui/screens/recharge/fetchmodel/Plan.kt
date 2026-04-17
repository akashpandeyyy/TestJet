package com.example.modernui.ui.screens.recharge.fetchmodel

import kotlinx.serialization.Serializable

@Serializable
data class Plan (
    val planType: String,
    val planDetails: List<PlanDetail>
)