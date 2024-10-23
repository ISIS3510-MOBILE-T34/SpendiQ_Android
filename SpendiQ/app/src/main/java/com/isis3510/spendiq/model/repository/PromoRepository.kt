package com.isis3510.spendiq.model.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.spendiq.model.data.Promo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Date

class PromoRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getPromos(): Flow<Result<List<Promo>>> = flow {
        try {
            val currentDate = Date()
            val snapshot = firestore.collection("promos")
                .whereGreaterThan("expirationDate", currentDate)
                .get()
                .await()

            val promos = snapshot.documents.mapNotNull { doc ->
                Promo(
                    id = doc.id,
                    title = doc.getString("title") ?: return@mapNotNull null,
                    description = doc.getString("description") ?: return@mapNotNull null,
                    imageUrl = doc.getString("imageUrl") ?: return@mapNotNull null,
                    discountCode = doc.getString("discountCode") ?: return@mapNotNull null,
                    restaurantName = doc.getString("restaurantName") ?: return@mapNotNull null,
                    expirationDate = doc.getDate("expirationDate") ?: return@mapNotNull null
                )
            }
            emit(Result.success(promos))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}