package com.example.modernui.Api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BankItem(

    @SerialName("id")
    val id: Int?,

    @SerialName("bankname")
    val bankname: String?,

    @SerialName("bankid")
    val bankid: String?,

    @SerialName("iin")
    val iin: String?
)
