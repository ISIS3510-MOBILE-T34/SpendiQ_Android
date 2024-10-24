package com.isis3510.spendiq.model.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.spendiq.model.data.Offer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class OffersRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getOffers(): Flow<Result<List<Offer>>> = flow {
        try {
            Log.d(TAG, "Fetching offers...")
            val snapshot = firestore.collection("offers").get().await()
            val offerList = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Offer::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Error deserializing document: ${doc.id} - ${e.message}")
                    null
                }
            }
            emit(Result.success(offerList))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching offers", e)
            emit(Result.failure(e))
        }
    }

    fun getOfferById(offerId: String): Flow<Result<Offer?>> = flow {
        try {
            val doc = firestore.collection("offers").document(offerId).get().await()
            val offer = doc.toObject(Offer::class.java)?.copy(id = doc.id)
            emit(Result.success(offer))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching offer by id: $offerId", e)
            emit(Result.failure(e))
        }
    }

    companion object {
        private const val TAG = "OffersRepository"
    }
}