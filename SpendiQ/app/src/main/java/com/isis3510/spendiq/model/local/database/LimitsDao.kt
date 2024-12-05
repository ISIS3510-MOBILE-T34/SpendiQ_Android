package com.isis3510.spendiq.model.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LimitsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLimits(limits: LimitsEntity)

    @Query("SELECT * FROM limits WHERE userId = :userId")
    suspend fun getLimits(userId: String): LimitsEntity?
}
