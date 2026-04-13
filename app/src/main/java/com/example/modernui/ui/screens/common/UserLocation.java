package com.example.modernui.ui.screens.common;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class UserLocation {

    private static final String PREF_NAME = "user_location_pref";
    private static final String KEY_LAT = "latitude";
    private static final String KEY_LNG = "longitude";

    private FusedLocationProviderClient fusedLocationClient;

    public UserLocation(Context context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    // Fetch and store location
    public void fetchAndSaveLocation(Context context) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return; // Permission not granted
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        saveLocation(context, location);
                    }
                });
    }

    // Save location in SharedPreferences
    private void saveLocation(Context context, Location location) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(KEY_LAT, String.valueOf(location.getLatitude()));
        editor.putString(KEY_LNG, String.valueOf(location.getLongitude()));
        editor.apply();
    }

    // Get saved latitude
    public static String getLatitude(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LAT, null);
    }

    // Get saved longitude
    public static String getLongitude(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LNG, null);
    }
}