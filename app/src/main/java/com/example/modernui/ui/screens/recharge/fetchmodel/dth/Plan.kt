package com.example.modernui.ui.screens.recharge.fetchmodel.dth

data class Plan(
    val dthDefaultPack: String,
    val planDetails: List<PlanDetail>,
    val planType: String
)