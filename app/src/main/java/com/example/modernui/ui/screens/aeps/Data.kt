package com.example.modernui.ui.screens.aeps

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Data(

    @SerialName("aadhaar")
    val aadhaar: String?,

    @SerialName("iin")
    val iin: String?,

    @SerialName("txnType")
    val txnType: String?,

    @SerialName("txnAmount")
    val txnAmount: String?,

    @SerialName("stan")
    val stan: String?,

    @SerialName("rrn")
    val rrn: String?,

    @SerialName("responseCode")
    val responseCode: String?,

    @SerialName("responsemessage")
    val responsemessage: String?,

    @SerialName("merchantcode")
    val merchantcode: String?,

    @SerialName("location")
    val location: String?,

    @SerialName("remainingBal")
    val remainingBal: String?,

    @SerialName("terminalid")
    val terminalid: String?,

    @SerialName("errormessage")
    val errormessage: String?,

    @SerialName("requestid")
    val requestid: String?,

    @SerialName("date")
    val date: String?,

    @SerialName("time")
    val time: String?,

    @SerialName("statement")
    val statement: String? = null,   // safer than Any

    @SerialName("status")
    val status: String?,

    @SerialName("commission")
    val commission: String? = null,  // safer than Any

    @SerialName("provider")
    val provider: String?
)