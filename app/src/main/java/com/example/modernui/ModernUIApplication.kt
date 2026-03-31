package com.example.modernui

import android.app.Application
import com.example.modernui.Api.RetrofitInstance
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ModernUIApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize RetrofitInstance with application context to fix "Unresolved reference context"
        RetrofitInstance.init(this)
    }
}
