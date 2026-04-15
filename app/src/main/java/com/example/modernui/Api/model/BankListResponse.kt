package com.example.modernui.Api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BankListResponse(

    @SerialName("status")
    val status: Int?,

    @SerialName("message")
    val message: String?,

    @SerialName("errorMessage")
    val errorMessage: String?,

    @SerialName("data")
    val data: List<BankItem>?
)