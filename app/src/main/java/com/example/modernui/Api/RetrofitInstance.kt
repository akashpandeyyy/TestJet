package com.example.modernui.Api

import android.content.Context
import android.util.Log
import com.example.modernui.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private lateinit var appContext: Context
    private const val HOST = "bc.finrichtechnology.com"
    private const val BASE_URL = "https://$HOST/"

    private fun getOkHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(RequestInterceptor(context))   // adds headers to every request
            .addInterceptor(ResponseInterceptor())         // handles all response codes
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG)
                    HttpLoggingInterceptor.Level.BODY
                else
                    HttpLoggingInterceptor.Level.NONE
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }



    val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(getOkHttpClient(appContext))
        .build()
        .create(ApiService::class.java)
}
//st val HOST = "bc.finrichtechnology.com"
//  const val BASE_URL = "https://$HOST/"
