package com.example.modernui.ui.screens.recharge.rechargemodel



import kotlinx.serialization.*

@Serializable
data class RechargeRequest(
    val mobile: String,
    val amount: String,
    val incode: String
)
