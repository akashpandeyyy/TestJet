package com.example.modernui.Api


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    @SerialName("email")
    val email: String?,
    @SerialName("id")
    val id: Int?,
    @SerialName("token")
    val token: String?,
    @SerialName("username")
    val username: String?
)