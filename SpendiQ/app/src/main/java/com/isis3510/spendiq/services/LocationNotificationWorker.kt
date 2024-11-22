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
import kotlin.math.*

class LocationNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    private val TAG = "LocationNotificationWorker"
    private val channelId = "OfferNotificationChannel"
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val database = DatabaseProvider.getDatabase(context)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting work execution")
        try {
            createNotificationChannel()

            // Get current location
            val location = getCurrentLocation()
            if (location == null) {
                Log.w(TAG, "Could not get current location")
                return Result.success()
            }

            // Get all offers from local database
            val offers = database.offerDao().getAllOffers()
            Log.d(TAG, "Retrieved ${offers.size} offers from database")

            // Find closest offer within range
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

            // Send notification for closest offer if found
            closestOffer?.let { (offer, distance) ->
                Log.d(TAG, "Found nearby offer: ${offer.placeName} at ${distance.toInt()}m")
                val notification = NotificationCompat.Builder(context, channelId)
                    .setContentTitle("Special Offer Nearby!")
                    .setContentText("${offer.placeName} is ${formatDistance(distance)} away")
                    .setStyle(NotificationCompat.BigTextStyle()
                        .bigText("${offer.placeName} (${formatDistance(distance)})\n${offer.offerDescription}")
                    )
                    .setSmallIcon(R.drawable.notification)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(offer.id.hashCode(), notification)
                Log.d(TAG, "Notification sent for offer: ${offer.placeName}")
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during work execution", e)
            return Result.failure()
        }
    }

    private suspend fun getCurrentLocation(): Location? {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Location permission not granted")
            return null
        }

        return try {
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting location", e)
            null
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371e3 // Earth's radius in meters
        val φ1 = lat1 * Math.PI / 180
        val φ2 = lat2 * Math.PI / 180
        val Δφ = (lat2 - lat1) * Math.PI / 180
        val Δλ = (lon2 - lon1) * Math.PI / 180

        val a = sin(Δφ / 2) * sin(Δφ / 2) +
                cos(φ1) * cos(φ2) *
                sin(Δλ / 2) * sin(Δλ / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c
    }

    private fun formatDistance(distance: Double): String {
        return when {
            distance < 100 -> "less than 100 meters"
            distance < 1000 -> "${(distance / 100).toInt() * 100} meters"
            else -> String.format("%.1f km", distance / 1000)
        }
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

    companion object {
        private const val MAX_NOTIFICATION_DISTANCE = 1000.0 // 1 kilometer
    }
}