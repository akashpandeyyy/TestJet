package com.example.modernui.ui.screens.recharge.fetchmodel.dth

data class PlanDetail(
    val channelCount: String,
    val count: String,
    val description: String,
    val language: String,
    val packageId: String,
    val packageName: String,
    val price: Price
)