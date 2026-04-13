package com.example.modernui.ui.screens.common.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Data(
    @SerialName("aadhaar")
    val aadhaar: String?,
    @SerialName("authToken")
    val authToken: String?,
    @SerialName("fing1")
    val fing1: Boolean?,
    @SerialName("fing2")
    val fing2: Boolean?,
    @SerialName("fing3")
    val fing3: Boolean?,
    @SerialName("fing4")
    val fing4: Boolean?,
    @SerialName("merchantName")
    val merchantName: String?,
    @SerialName("orderId")
    val orderId: String?,
    @SerialName("vid")
    val vid: String?
)