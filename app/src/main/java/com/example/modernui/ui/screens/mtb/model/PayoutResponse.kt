package com.example.modernui.ui.screens.mtb.model

import com.google.gson.annotations.SerializedName

data class PayoutResponse(
    val status: Int,
    val message: String,
    val errorMessage: String?,
    val data: PayoutData
)

data class PayoutData(
    val requestId: String,
    val status: String,
    val responseCode: String,
    val responseMessage: String,
    val beneficiaryName: String,
    val amount: Int,
    val account: String,
    val ifsc: String,

    @SerializedName("bankname")
    val bankName: String,

    val name: String,
    val charge: Int,
    val date: String,
    val rrn: String
)