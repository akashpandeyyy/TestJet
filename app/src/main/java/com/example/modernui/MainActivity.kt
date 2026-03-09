package com.example.modernui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.modernui.ui.theme.ModernUITheme
import dagger.hilt.android.AndroidEntryPoint
import android.provider.Settings
import android.content.Context

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ModernUITheme {
                AppNavigation()
            }
        }
    }


}
