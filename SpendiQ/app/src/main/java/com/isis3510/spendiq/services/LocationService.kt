package com.isis3510.spendiq.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * LocationService is a class that provides methods to retrieve the current location of the device.
 * It utilizes the FusedLocationProviderClient to access location services efficiently.
 *
 * @param context The context of the application, used to access system services.
 */
class LocationService(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Retrieves the current location of the device.
     *
     * This method first checks if location permissions are granted. If they are not, it returns null.
     * If permissions are granted, it attempts to get the last known location. If the last known
     * location is null, it requests a single location update.
     *
     * @return The current location as a [Location] object, or null if permissions are not granted or an error occurs.
     */
    suspend fun getCurrentLocation(): Location? {
        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null // Return null if permissions are not granted
        }

        return suspendCancellableCoroutine { continuation ->
            // Attempt to get the last known location
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(location) // Resume with the last known location
                    } else {
                        // If last known location is null, request a single location update
                        val locationRequest = LocationRequest.create().apply {
                            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Set high accuracy for the location request
                            numUpdates = 1 // Set the number of updates to 1
                        }
                        // Define a location callback to handle the location result
                        val locationCallback = object : LocationCallback() {
                            override fun onLocationResult(locationResult: LocationResult) {
                                fusedLocationClient.removeLocationUpdates(this) // Stop location updates
                                continuation.resume(locationResult.lastLocation) // Resume with the new location
                            }
                        }
                        // Request location updates
                        fusedLocationClient.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            Looper.getMainLooper() // Use the main looper for the callback
                        )
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resume(null) // Resume with null on failure
                }

            // Clean up: Remove location updates if the coroutine is cancelled
            continuation.invokeOnCancellation {
                fusedLocationClient.removeLocationUpdates(object : LocationCallback() {})
            }
        }
    }
}
