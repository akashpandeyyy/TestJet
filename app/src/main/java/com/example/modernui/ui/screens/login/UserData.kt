package com.example.modernui.ui.screens.login


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    @SerialName("address")
    val address: String?,
    @SerialName("mobile")
    val mobile: String?,
    @SerialName("name")
    val name: String?,
    @SerialName("roleId")
    val roleId: Int?,
    @SerialName("userId")
    val userId: String?
)