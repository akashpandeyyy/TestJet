package com.example.modernui.ui.screens.aeps


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Data(
    @SerialName("aadhaar")
    val aadhaar: String?,
    @SerialName("commission")
    val commission: Any?,
    @SerialName("date")
    val date: String?,
    @SerialName("errormessage")
    val errormessage: String?,
    @SerialName("iin")
    val iin: String?,
    @SerialName("location")
    val location: String?,
    @SerialName("merchantcode")
    val merchantcode: String?,
    @SerialName("remainingBal")
    val remainingBal: String?,
    @SerialName("requestid")
    val requestid: String?,
    @SerialName("responseCode")
    val responseCode: String?,
    @SerialName("responsemessage")
    val responsemessage: String?,
    @SerialName("rrn")
    val rrn: String?,
    @SerialName("stan")
    val stan: String?,
    @SerialName("statement")
    val statement: Any?,
    @SerialName("status")
    val status: String?,
    @SerialName("terminalid")
    val terminalid: String?,
    @SerialName("time")
    val time: String?,
    @SerialName("txnAmount")
    val txnAmount: String?,
    @SerialName("txnType")
    val txnType: String?
)