package com.example.modernui.ui.screens.dmt.jiomodel

import kotlinx.serialization.SerialName

data class ValidateUserRequest(

    @SerialName("latitude")
    val mobile: String,
    @SerialName("latitude")
    val latitude: String?,
    @SerialName("longitude")
    val longitude: String?,
)