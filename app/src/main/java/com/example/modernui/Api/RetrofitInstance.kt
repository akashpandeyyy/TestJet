package com.example.modernui.Api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val HOST = "bc.finrichtechnology.com"
    private const val BASE_URL = "https://\$HOST/"

    val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)


}

// const val HOST = "bc.finrichtechnology.com"
//  const val BASE_URL = "https://$HOST/"
