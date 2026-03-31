package com.example.modernui.pagination.model

import com.google.gson.annotations.SerializedName
data class PagingResponceModelItem(
    @SerializedName("body")
    val body: String?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("userId")
    val userId: Int?
)