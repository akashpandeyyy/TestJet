package com.example.modernui.ui.screens.dmt.jiomodel

import kotlinx.serialization.*

@Serializable
data class CreateTxnRequest (
    val amount: String,

    @SerialName("beneId")
    val beneId: String,

    val mobile: String,
    val account: String,
    val ifsc: String,
    val name: String,
    val latitude: String?,
    val longitude: String?
)
