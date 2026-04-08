package com.example.modernui.Api.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BalanceData(
    @SerialName("aeps")
    val aeps: Double?,
    @SerialName("count")
    val count: Int?,
    @SerialName("total")
    val total: Double?,
    @SerialName("wallet")
    val wallet: Double?
)
