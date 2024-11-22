package com.isis3510.spendiq.model.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offers")
data class DatabaseOffer(
    @PrimaryKey val id: String,
    val placeName: String,
    val offerDescription: String,
    val shopImage: String?,
    val recommendationReason: String?,
    val latitude: Double,
    val longitude: Double,
    val distance: Float
)