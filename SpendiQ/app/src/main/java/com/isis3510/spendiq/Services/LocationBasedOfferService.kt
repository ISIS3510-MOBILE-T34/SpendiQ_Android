package com.isis3510.spendiq.Services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.isis3510.spendiq.R
import com.isis3510.spendiq.model.data.Offer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.*

class LocationBasedOfferService(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "OfferNotificationChannel"
    private val scope = CoroutineScope(Dispatchers.IO)
    private var activeOffers = listOf<Offer>()
    private val notifiedOffers = mutableSetOf<String>()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { currentLocation ->
                checkNearbyOffers(currentLocation.latitude, currentLocation.longitude)
            }
        }
    }

    init {
        createNotificationChannel()
    }

    fun startMonitoring(offers: List<Offer>) {
        if (!hasLocationPermission()) {
            Log.d("LocationOfferService", "Missing location permission")
            return
        }

        activeOffers = offers

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 60000 // Update every minute
            fastestInterval = 30000 // Fastest update interval
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e("LocationOfferService", "Error requesting location updates", e)
        }
    }

    fun stopMonitoring() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        activeOffers = emptyList()
        notifiedOffers.clear()
    }

    private fun checkNearbyOffers(currentLat: Double, currentLon: Double) {
        scope.launch {
            activeOffers.forEach { offer ->
                if (offer.id != null && !notifiedOffers.contains(offer.id) &&
                    offer.latitude != null && offer.longitude != null) {

                    val distance = calculateDistance(
                        currentLat, currentLon,
                        offer.latitude, offer.longitude
                    )

                    if (distance <= 1000) { // 1km in meters
                        sendOfferNotification(offer)
                        notifiedOffers.add(offer.id)
                    }
                }
            }
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

    private fun sendOfferNotification(offer: Offer) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Nearby Offer!")
            .setContentText("${offer.placeName}: ${offer.offerDescription}")
            .setSmallIcon(R.drawable.notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(offer.id.hashCode(), notification)
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

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}