package com.example.modernui.Api.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Cmsresponce(
    @SerialName("data")
    val `data`: DataX?,
    @SerialName("errorMessage")
    val errorMessage: Any?,
    @SerialName("message")
    val message: String?,
    @SerialName("status")
    val status: Int?
)