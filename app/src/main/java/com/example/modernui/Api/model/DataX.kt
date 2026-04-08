package com.example.modernui.Api.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DataX(
    @SerialName("cmsLink")
    val cmsLink: String?,
    @SerialName("partnerRefId")
    val partnerRefId: String?,
    @SerialName("vid")
    val vid: String?
)