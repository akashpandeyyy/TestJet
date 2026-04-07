package com.example.modernui.ui.screens.login


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class otprequest(
    @SerialName("otp")
    val otp: String?,
    @SerialName("password")
    val password: String?,
    @SerialName("source")
    val source: String?,
    @SerialName("username")
    val username: String?
)