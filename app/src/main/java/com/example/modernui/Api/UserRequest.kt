package com.example.modernui.Api

import com.google.gson.annotations.SerializedName



data class UserRequest(
    @SerializedName("password")
    val password: String?,
    @SerializedName("username")
    val username: String?
)