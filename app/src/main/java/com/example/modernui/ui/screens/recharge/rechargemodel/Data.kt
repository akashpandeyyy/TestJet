package com.example.modernui.ui.screens.recharge.rechargemodel

import kotlinx.serialization.*

@Serializable
data class Data (
    val mobile: String,

    @SerialName("orderId")
    val orderID: String,

    val amount: String,
    val commission: Long,
    val commissionRate: String,
    val serviceRefNo: String,
    val status: String,
    val remark: String,
    val date: String,
    val time: String,
    val operator: String
)
