package com.example.modernui.ui.screens.common.model



// for validate the token in po.sofmintindia

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TwoFAValiResponce(
    @SerialName("data")
    val `data`: Data?,
    @SerialName("errorMessage")
    val errorMessage: Any?,
    @SerialName("message")
    val message: String?,
    @SerialName("status")
    val status: Int?
)