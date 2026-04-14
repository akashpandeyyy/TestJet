//package com.example.modernui.core.location
//
//import android.annotation.SuppressLint
//import android.content.Context
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.location.Priority
//import com.google.android.gms.tasks.CancellationTokenSource
//import kotlinx.coroutines.suspendCancellableCoroutine
//import kotlin.coroutines.resume
//
//
//data class LocationCoordinates(
//    val latitude: Double,
//    val longitude: Double
//)
//
//class UserLocationProvider(private val context: Context) {
//
//    private val fusedLocationClient: FusedLocationProviderClient =
//        LocationServices.getFusedLocationProviderClient(context)
//
//    @SuppressLint("MissingPermission")
//    suspend fun getCurrentLocation(): LocationCoordinates? {
//        return suspendCancellableCoroutine { continuation ->
//            val cts = CancellationTokenSource()
//
//            fusedLocationClient.getCurrentLocation(
//                Priority.PRIORITY_HIGH_ACCURACY,
//                cts.token
//            ).addOnSuccessListener { location ->
//                if (location != null) {
//                    continuation.resume(LocationCoordinates(location.latitude, location.longitude))
//                } else {
//                    continuation.resume(null)
//                }
//            }.addOnFailureListener {
//                continuation.resume(null)
//            }
//
//            continuation.invokeOnCancellation {
//                cts.cancel()
//            }
//        }
//    }
//}




package com.example.modernui.core.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class UserLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Pair<String, String>? {
        return suspendCancellableCoroutine { continuation ->
            val cts = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cts.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    continuation.resume(Pair(location.latitude.toString(), location.longitude.toString()))
                } else {
                    continuation.resume(null)
                }
            }.addOnFailureListener {
                continuation.resume(null)
            }
            continuation.invokeOnCancellation { cts.cancel() }
        }
    }


}