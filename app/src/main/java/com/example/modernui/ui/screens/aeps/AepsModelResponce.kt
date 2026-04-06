package com.example.modernui.ui.screens.aeps


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AepsModelResponce(
    @SerialName("data")
    val `data`: Data?,
    @SerialName("errorMessage")
    val errorMessage: Any?,
    @SerialName("message")
    val message: String?,
    @SerialName("status")
    val status: Int?
)