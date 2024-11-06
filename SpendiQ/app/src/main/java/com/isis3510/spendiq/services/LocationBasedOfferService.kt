package com.isis3510.spendiq.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.isis3510.spendiq.R
import com.isis3510.spendiq.model.data.Offer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.*

// This service listens for location updates and sends notifications for nearby offers.
class LocationBasedOfferService(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "OfferNotificationChannel" // Channel ID for notifications
    private val scope = CoroutineScope(Dispatchers.IO) // Coroutine scope for background tasks
    private var activeOffers = listOf<Offer>() // List of offers to monitor

    // Track the last notification time and the last notified offer ID
    private var lastNotificationTime: Long = 0
    private var lastNotifiedOfferId: String? = null
    private val NOTIFICATION_COOLDOWN = 15 * 60 * 1000 // Cooldown period of 15 minutes
    private val MAX_NOTIFICATION_DISTANCE = 1000.0 // Maximum distance for notifications in meters

    // Callback to receive location updates
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { currentLocation ->
                checkNearbyOffers(currentLocation.latitude, currentLocation.longitude)
            }
        }
    }

    init {
        createNotificationChannel() // Create the notification channel on initialization
    }

    /**
     * Starts monitoring for nearby offers by setting up location updates.
     * @param offers List of offers to monitor for proximity.
     */
    fun startMonitoring(offers: List<Offer>) {
        if (!hasLocationPermission()) {
            return // Exit if location permission is not granted
        }

        activeOffers = offers // Update the list of active offers

        // Create a location request for high accuracy updates
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Use high accuracy for location updates
            interval = 60000 // Request location updates every minute
            fastestInterval = 30000 // Fastest update interval
        }

        try {
            // Request location updates with the specified location request and callback
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper() // Use the main looper for callbacks
            )
        } catch (e: SecurityException) {
            // Handle the exception if permissions are missing
        }
    }

    /**
     * Stops monitoring for offers by removing location updates.
     */
    fun stopMonitoring() {
        fusedLocationClient.removeLocationUpdates(locationCallback) // Remove location updates
        activeOffers = emptyList() // Clear the list of active offers
    }

    /**
     * Checks for offers that are nearby the current location.
     * @param currentLat Current latitude of the device.
     * @param currentLon Current longitude of the device.
     */
    private fun checkNearbyOffers(currentLat: Double, currentLon: Double) {
        scope.launch {
            val currentTime = System.currentTimeMillis() // Get the current time

            // Ensure enough time has passed since the last notification
            if (currentTime - lastNotificationTime < NOTIFICATION_COOLDOWN) {
                return@launch
            }

            // Find the closest offer within the specified range
            val closestOffer = activeOffers
                .filter { offer ->
                    offer.id != null &&
                            offer.latitude != null &&
                            offer.longitude != null &&
                            offer.id != lastNotifiedOfferId // Exclude the last notified offer
                }
                .map { offer ->
                    Pair(offer, calculateDistance(
                        currentLat, currentLon,
                        offer.latitude!!, offer.longitude!!
                    ))
                }
                .filter { (_, distance) -> distance <= MAX_NOTIFICATION_DISTANCE } // Filter by maximum distance
                .minByOrNull { (_, distance) -> distance } // Find the closest offer

            closestOffer?.let { (offer, distance) ->
                sendOfferNotification(offer, distance.toInt()) // Send notification for the offer
                lastNotificationTime = currentTime // Update the last notification time
                lastNotifiedOfferId = offer.id // Update the last notified offer ID
            }
        }
    }

    /**
     * Calculates the distance between two geographical points using the Haversine formula.
     * @param lat1 Latitude of the first point.
     * @param lon1 Longitude of the first point.
     * @param lat2 Latitude of the second point.
     * @param lon2 Longitude of the second point.
     * @return The distance in meters.
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371e3 // Earth's radius in meters
        val φ1 = lat1 * Math.PI / 180
        val φ2 = lat2 * Math.PI / 180
        val Δφ = (lat2 - lat1) * Math.PI / 180
        val Δλ = (lon2 - lon1) * Math.PI / 180

        // Haversine formula
        val a = sin(Δφ / 2) * sin(Δφ / 2) +
                cos(φ1) * cos(φ2) *
                sin(Δλ / 2) * sin(Δλ / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c // Return the distance
    }

    /**
     * Sends a notification to the user about a nearby offer.
     * @param offer The offer details to include in the notification.
     * @param distance The distance to the offer.
     */
    private fun sendOfferNotification(offer: Offer, distance: Int) {
        val distanceText = when {
            distance < 100 -> "less than 100 meters"
            distance < 1000 -> "${(distance / 100) * 100} meters"
            else -> "${distance / 1000.0} km"
        }

        // Build and send the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Special Offer Nearby!")
            .setContentText("${offer.placeName} (${distanceText} away): ${offer.offerDescription}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${offer.placeName} (${distanceText} away): ${offer.offerDescription}\n${offer.recommendationReason ?: ""}")
            )
            .setSmallIcon(R.drawable.notification) // Icon for the notification
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true) // Dismiss the notification when clicked
            .build()

        notificationManager.notify(OFFER_NOTIFICATION_ID, notification) // Notify using the ID
    }

    /**
     * Creates a notification channel for API 26 and above.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Nearby Offers" // Name of the notification channel
            val descriptionText = "Notifications for nearby special offers" // Description of the channel
            val importance = NotificationManager.IMPORTANCE_DEFAULT // Importance level
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText // Set the description
            }
            notificationManager.createNotificationChannel(channel) // Create the channel
        }
    }

    /**
     * Checks if the application has location permissions.
     * @return True if location permissions are granted, false otherwise.
     */
    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED // Check for fine location permission
    }

    companion object {
        private const val OFFER_NOTIFICATION_ID = 1001 // Notification ID for offer notifications
    }
}
