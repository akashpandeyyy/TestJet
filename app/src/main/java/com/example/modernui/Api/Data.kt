package com.example.modernui.Api


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Data(
    @SerialName("accountNonExpired")
    val accountNonExpired: Boolean?,
    @SerialName("accountNonLocked")
    val accountNonLocked: Boolean?,
    @SerialName("address")
    val address: String?,
    @SerialName("adhaar")
    val adhaar: String?,
    @SerialName("authorities")
    val authorities: List<Authority?>?,
    @SerialName("balance")
    val balance: Double?,
    @SerialName("city")
    val city: String?,
    @SerialName("country")
    val country: String?,
    @SerialName("credentialsNonExpired")
    val credentialsNonExpired: Boolean?,
    @SerialName("date")
    val date: String?,
    @SerialName("email")
    val email: String?,
    @SerialName("enabled")
    val enabled: Boolean?,
    @SerialName("firmname")
    val firmname: String?,
    @SerialName("gstn")
    val gstn: String?,
    @SerialName("mobile")
    val mobile: String?,
    @SerialName("name")
    val name: String?,
    @SerialName("otpverify")
    val otpverify: Boolean?,
    @SerialName("pan")
    val pan: String?,
    @SerialName("pincode")
    val pincode: String?,
    @SerialName("roleid")
    val roleid: Int?,
    @SerialName("state")
    val state: String?,
    @SerialName("status")
    val status: Boolean?,
    @SerialName("time")
    val time: String?,
    @SerialName("uplineid")
    val uplineid: String?,
    @SerialName("uplinemobile")
    val uplinemobile: String?,
    @SerialName("uplinename")
    val uplinename: String?,
    @SerialName("userid")
    val userid: String?,
    @SerialName("username")
    val username: String?
)