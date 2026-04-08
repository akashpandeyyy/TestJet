package com.example.modernui.Api.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class balanceresponce(
    @SerialName("data")
    val `data`: BalanceData?,
    @SerialName("errorMessage")
    val errorMessage: String?,
    @SerialName("message")
    val message: String?,
    @SerialName("status")
    val status: Int?
)
