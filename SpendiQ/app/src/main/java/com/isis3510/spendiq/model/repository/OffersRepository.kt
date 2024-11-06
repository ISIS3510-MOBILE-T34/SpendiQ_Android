package com.isis3510.spendiq.model.repository

import android.util.Log
import com.isis3510.spendiq.model.data.Offer
import com.isis3510.spendiq.model.singleton.FirebaseManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/**
 * Repository class for handling operations related to offers.
 *
 * This class provides methods to fetch all offers from Firestore,
 * as well as fetching a specific offer by its ID.
 */
class OffersRepository {
    // Firestore instance for database interactions
    private val firestore = FirebaseManager.firestore

    /**
     * Retrieves all offers from the Firestore database.
     *
     * @return A Flow emitting the result containing a list of offers on success,
     * or an error if the operation fails.
     */
    fun getOffers(): Flow<Result<List<Offer>>> = flow {
        try {
            Log.d(TAG, "Fetching offers...") // Log the fetching operation
            val snapshot = firestore.collection("offers").get().await() // Await Firestore response
            // Map documents to Offer objects, handling potential deserialization errors
            val offerList = snapshot.documents.mapNotNull { doc ->
                try {
                    // Convert Firestore document to Offer object and include the document ID
                    doc.toObject(Offer::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    // Log error if deserialization fails for this document
                    Log.e(TAG, "Error deserializing document: ${doc.id} - ${e.message}")
                    null // Return null to skip this document
                }
            }
            emit(Result.success(offerList)) // Emit success with the list of offers
        } catch (e: Exception) {
            // Log error and emit failure if the fetching operation fails
            Log.e(TAG, "Error fetching offers", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Retrieves a specific offer by its ID.
     *
     * @param offerId The ID of the offer to retrieve.
     * @return A Flow emitting the result containing the offer on success,
     * or an error if the operation fails.
     */
    fun getOfferById(offerId: String): Flow<Result<Offer?>> = flow {
        try {
            // Fetch the specific document by offer ID
            val doc = firestore.collection("offers").document(offerId).get().await()
            // Convert the document to Offer object and include the document ID
            val offer = doc.toObject(Offer::class.java)?.copy(id = doc.id)
            emit(Result.success(offer)) // Emit success with the offer
        } catch (e: Exception) {
            // Log error and emit failure if the fetching operation fails
            Log.e(TAG, "Error fetching offer by id: $offerId", e)
            emit(Result.failure(e))
        }
    }

    companion object {
        private const val TAG = "OffersRepository" // Tag for logging
    }
}
