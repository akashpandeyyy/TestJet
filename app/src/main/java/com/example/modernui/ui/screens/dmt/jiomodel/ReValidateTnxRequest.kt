package com.example.modernui.ui.screens.dmt.jiomodel

import kotlinx.serialization.*

import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class ReValidateTnxRequest (
    @SerialName("requestId")
    val requestId: String
)
