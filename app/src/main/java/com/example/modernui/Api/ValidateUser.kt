package com.example.modernui.Api

import com.google.gson.annotations.SerializedName

data class ValidateUser(

    @SerializedName("status")
    val status: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("errorMessage")
    val errorMessage: String?,

    @SerializedName("data")
    val data: Data
)

data class Data(

    @SerializedName("userid")
    val userId: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("mobile")
    val mobile: String,

    @SerializedName("roleid")
    val roleId: Int,

    @SerializedName("uplineid")
    val uplineId: String,

    @SerializedName("uplinemobile")
    val uplineMobile: String,

    @SerializedName("uplinename")
    val uplineName: String,

    @SerializedName("adhaar")
    val adhaar: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("pan")
    val pan: String,

    @SerializedName("firmname")
    val firmName: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("city")
    val city: String,

    @SerializedName("pincode")
    val pincode: String,

    @SerializedName("state")
    val state: String,

    @SerializedName("country")
    val country: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("time")
    val time: String,

    @SerializedName("balance")
    val balance: Double,

    @SerializedName("status")
    val status: Boolean,

    @SerializedName("otpverify")
    val otpVerify: Boolean,

    @SerializedName("gstn")
    val gstn: String,

    @SerializedName("authorities")
    val authorities: List<Authority>,

    @SerializedName("enabled")
    val enabled: Boolean,

    @SerializedName("credentialsNonExpired")
    val credentialsNonExpired: Boolean,

    @SerializedName("accountNonExpired")
    val accountNonExpired: Boolean,

    @SerializedName("username")
    val username: String,

    @SerializedName("accountNonLocked")
    val accountNonLocked: Boolean
)

data class Authority(

    @SerializedName("authority")
    val authority: String
)