package com.example.modernui.Api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InsuranceResponse(
    @SerialName("status")
    val status: Int?,
    @SerialName("message")
    val message: String?,
    @SerialName("errorMessage")
    val errorMessage: String?,
    @SerialName("data")
    val data: InsuranceData?
)

@Serializable
data class InsuranceData(
    @SerialName("agentId")
    val agentId: String?,
    @SerialName("url")
    val url: String?,
    @SerialName("status")
    val status: String?,
    @SerialName("remark")
    val remark: String?
)
