package com.isis3510.spendiq.model.local.database

import androidx.room.*

@Dao
interface OfferDao {
    @Query("SELECT * FROM offers")
    suspend fun getAllOffers(): List<DatabaseOffer>

    @Query("SELECT * FROM offers WHERE id = :offerId")
    suspend fun getOfferById(offerId: String): DatabaseOffer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: DatabaseOffer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffers(offers: List<DatabaseOffer>)

    @Delete
    suspend fun deleteOffer(offer: DatabaseOffer)

    @Query("DELETE FROM offers")
    suspend fun clearAllOffers()
}