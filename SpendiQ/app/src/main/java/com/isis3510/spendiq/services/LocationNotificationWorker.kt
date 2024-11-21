package com.isis3510.spendiq.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.isis3510.spendiq.R
import com.isis3510.spendiq.model.local.database.DatabaseProvider
import kotlinx.coroutines.tasks.await
import java.util.Locale
import kotlin.math.*

class LocationNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val tag = "LocationNotificationWorker"
    private val channelId = "OfferNotificationChannel"
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val database = DatabaseProvider.getDatabase(context)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    override suspend fun doWork(): Result {
        Log.d(tag, "Starting work execution")
        try {
            // Step 1: Create a notification channel for Android O+
            createNotificationChannel()

            // Step 2: Get current location safely
            val location = getCurrentLocation()
            if (location == null) {
                Log.w(tag, "Could not get current location")
                return Result.failure()
            }

            // Step 3: Retrieve all offers from the local database
            val offers = database.offerDao().getAllOffers()
            Log.d(tag, "Retrieved ${offers.size} offers from database")

            // Step 4: Find the closest offer within the specified range
            val closestOffer = offers
                .map { offer ->
                    val distance = calculateDistance(
                        location.latitude, location.longitude,
                        offer.latitude, offer.longitude
                    )
                    Pair(offer, distance)
                }
                .filter { (_, distance) -> distance <= MAX_NOTIFICATION_DISTANCE }
                .minByOrNull { (_, distance) -> distance }

            // Step 5: Send a notification for the closest offer, if any
            closestOffer?.let { (offer, distance) ->
                Log.d(tag, "Found nearby offer: ${offer.placeName} at ${distance.toInt()}m")
                sendNotification(offer.placeName, distance, offer.offerDescription)
            }

            return Result.success()
        } catch (e: SecurityException) {
            Log.e(tag, "Security exception: Ensure permissions are granted", e)
            return Result.failure()
        } catch (e: Exception) {
            Log.e(tag, "Error during work execution", e)
            return Result.failure()
        }
    }

    private suspend fun getCurrentLocation(): Location? {
        // Check for location permissions
        if (!hasLocationPermission()) {
            Log.w(tag, "Location permissions are not granted")
            return null
        }

        return try {
            fusedLocationClient.lastLocation.await()
        } catch (e: SecurityException) {
            Log.e(tag, "Security exception during location retrieval", e)
            null
        } catch (e: Exception) {
            Log.e(tag, "Error getting current location", e)
            null
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val r = 6371e3 // Earth's radius in meters
        val φ1 = Math.toRadians(lat1)
        val φ2 = Math.toRadians(lat2)
        val Δφ = Math.toRadians(lat2 - lat1)
        val Δλ = Math.toRadians(lon2 - lon1)

        val a = sin(Δφ / 2) * sin(Δφ / 2) +
                cos(φ1) * cos(φ2) *
                sin(Δλ / 2) * sin(Δλ / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c
    }

    private fun sendNotification(placeName: String, distance: Double, description: String) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Special Offer Nearby!")
            .setContentText(
                String.format(
                    Locale.getDefault(),
                    "%s is %s away",
                    placeName,
                    formatDistance(distance)
                )
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        String.format(
                            Locale.getDefault(),
                            "%s (%s)\n%s",
                            placeName,
                            formatDistance(distance),
                            description
                        )
                    )
            )
            .setSmallIcon(R.drawable.notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(placeName.hashCode(), notification)
        Log.d(tag, "Notification sent for offer: $placeName")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Nearby Offers"
            val descriptionText = "Notifications for nearby special offers"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun formatDistance(distance: Double): String {
        return when {
            distance < 100 -> "less than 100 meters"
            distance < 1000 -> "${(distance / 100).toInt() * 100} meters"
            else -> String.format(Locale.getDefault(), "%.1f km", distance / 1000)
        }
    }

    companion object {
        private const val MAX_NOTIFICATION_DISTANCE = 1000.0 // 1 kilometer
    }
}
