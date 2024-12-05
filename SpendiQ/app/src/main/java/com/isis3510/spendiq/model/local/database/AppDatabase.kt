package com.isis3510.spendiq.model.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(entities = [LimitsEntity::class, DatabaseOffer::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun limitsDao(): LimitsDao
    abstract fun offerDao(): OfferDao
}