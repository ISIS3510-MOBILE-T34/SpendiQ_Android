package com.isis3510.spendiq.model.local.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DatabaseOffer::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun offerDao(): OfferDao
}
