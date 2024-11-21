package com.isis3510.spendiq.utils

import android.content.Context
import android.util.Log
import com.isis3510.spendiq.model.local.database.DatabaseOffer
import com.isis3510.spendiq.model.local.database.DatabaseProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DatabaseTestUtility {
    private const val TAG = "DatabaseTestUtility"

    fun verifyDatabaseContent(context: Context) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val database = DatabaseProvider.getDatabase(context)
                val offers = database.offerDao().getAllOffers()

                Log.d(TAG, "Number of offers in database: ${offers.size}")
                offers.forEach { offer ->
                    Log.d(TAG, """
                        Offer ID: ${offer.id}
                        Name: ${offer.placeName}
                        Description: ${offer.offerDescription}
                        Location: (${offer.latitude}, ${offer.longitude})
                        Distance: ${offer.distance}
                        ----------------------------------------
                    """.trimIndent())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error verifying database content", e)
            }
        }
    }

    fun insertTestOffer(context: Context) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val testOffer = DatabaseOffer(
                    id = "test-offer-1",
                    placeName = "Test Store",
                    offerDescription = "Test Offer Description",
                    shopImage = null,
                    recommendationReason = "Test Reason",
                    latitude = 4.6097100,
                    longitude = -74.0817500,
                    distance = 100f
                )

                val database = DatabaseProvider.getDatabase(context)
                database.offerDao().insertOffer(testOffer)
                Log.d(TAG, "Test offer inserted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting test offer", e)
            }
        }
    }
}