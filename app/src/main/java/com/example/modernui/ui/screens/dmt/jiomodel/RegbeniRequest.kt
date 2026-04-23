package com.example.modernui.ui.screens.dmt.jiomodel

import kotlinx.serialization.*

@Serializable
data class RegbeniRequest (
    val mobile: String,
    val authCode: String?,
    val agentId: String,
    val latitude: String,
    val longitude: String
)
