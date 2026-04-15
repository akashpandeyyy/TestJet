package com.example.modernui.ui.screens.aeps

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AepsModelResponse(

    @SerialName("data")
    val data: Data?,

    @SerialName("errorMessage")
    val errorMessage: String?,

    @SerialName("message")
    val message: String?,

    @SerialName("status")
    val status: Int?
)