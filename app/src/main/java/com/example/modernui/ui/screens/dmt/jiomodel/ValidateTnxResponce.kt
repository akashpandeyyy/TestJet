package com.example.modernui.ui.screens.dmt.jiomodel

import com.google.gson.JsonElement
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*


@Serializable
data class ValidateTnxResponce (
    val status: Int,
    val message: String,
    val errorMessage: JsonElement? = null,
    val data: Dataa
)

@Serializable
data class Dataa (
    val requestId: String,
    val partnerReferance: String,
    val responseCode: String,
    val errorCode: String,
    val status: String,
    val remark: String,
    val vid: String,
    val timestamp: String,
    val remitterMobile: JsonElement? = null,
    val beneficiaryName: String,
    val account: String,
    val ifsc: String,
    val bankName: String,
    val bankTransactionId: String,
    val rrn: String,
    val amount: String,
    val charge: Long,
    val subTotal: Long,
    val commIncGst: Long
)