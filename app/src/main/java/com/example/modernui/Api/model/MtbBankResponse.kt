package com.example.modernui.Api.model

import com.google.gson.annotations.SerializedName

data class MtbBankResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String?,
    @SerializedName("errorMessage") val errorMessage: String?,
    @SerializedName("data") val data: List<MtbBankData>?
)

data class MtbBankData(
    @SerializedName("id") val id: Int?,
    @SerializedName("userId") val userId: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("mobile") val mobile: String?,
    @SerializedName("accountNo") val accountNo: String?,
    @SerializedName("ifscCode") val ifscCode: String?,
    @SerializedName("beneId") val beneId: String?,
    @SerializedName("bankName") val bankName: String?,
    @SerializedName("verifiedName") val verifiedName: String?,
    @SerializedName("status") val status: Boolean?
)
