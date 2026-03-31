package com.example.modernui.Api

import android.content.Context
import com.example.modernui.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private var appContext: Context? = null
    private const val HOST = "bc.finrichtechnology.com"
   // private const val BASE_URL = "https://$HOST/"
    private const val BASE_URL = "https://b2buat.softmintdigital.com/"
    private const val BASE_URLL = "https://jsonplaceholder.typicode.com/"


    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)

        appContext?.let {
            builder.addInterceptor(RequestInterceptor(it))
        }
        
        builder.addInterceptor(ResponseInterceptor())
        
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
        
        builder.build()
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)
    }
    val apii: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URLL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

}
