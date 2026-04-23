package com.example.modernui.ui.screens.dmt.jiomodel
import com.google.gson.JsonElement
import kotlinx.serialization.*

import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class KycDataResponse (
    val status: Int,
    val message: String,
    val errorMessage: JsonElement? = null,
    val data: Datam
)

@Serializable
data class Datam (
    val uidaiData: UidaiData
)

@Serializable
data class UidaiData (
    val ret: String,
    val code: String,
    val txn: String,
    val poi: Poi,
    val ts: String,
    val token: String
)

@Serializable
data class Poi (
    val gender: String,
    val dob: String,
    val name: String
)
