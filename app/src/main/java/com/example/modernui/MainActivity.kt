package com.example.modernui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.modernui.ui.screens.common.UserLocation
import com.example.modernui.ui.theme.ModernUITheme
import dagger.hilt.android.AndroidEntryPoint
import com.example.modernui.ui.screens.login.AppNavigation


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

        // Check Permission
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION), 1000)
        }
        else {
            fetchLocation(this)
        }
    }

    @Override
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults,deviceId)
        if(requestCode == 1000 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            fetchLocation(this)
        }
        else {
            Toast.makeText(this@MainActivity, "Permission Denied", Toast.LENGTH_SHORT).show()
            finish()
        }


    }

    private fun fetchLocation(activity: MainActivity) {
        val locationManager = UserLocation(activity)
        locationManager.fetchAndSaveLocation(activity)
        val latitude = UserLocation.getLatitude(activity)
        val longitude = UserLocation.getLongitude(activity)
        Toast.makeText(activity, "Latitude: $latitude, Longitude: $longitude", Toast.LENGTH_SHORT).show()

    }


}








































































//
//override fun onRequestPermissionsResult(
//    requestCode: Int,
//    permissions: Array<out String?>,
//    grantResults: IntArray,
//    deviceId: Int
//) {
//    super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
//    if (requestCode == 101 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//        // TODO: Fetch the Upated/Last Location
//    }
//}
