package com.example.modernui.Api
import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String?,
    @SerializedName("errorMessage") val errorMessage: String?,
    @SerializedName("data") val data: LoginData?
)

data class LoginData(
    @SerializedName("token") val token: String?,
    @SerializedName("userData") val userData: MyUserData?
)

data class MyUserData(
    @SerializedName("userId") val userId: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("mobile") val mobile: String?,
    @SerializedName("role") val roleId: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("id") val id: Int?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("role_name") val role: String?
)
