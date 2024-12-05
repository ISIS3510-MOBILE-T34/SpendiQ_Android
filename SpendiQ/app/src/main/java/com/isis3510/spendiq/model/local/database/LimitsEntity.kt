package com.isis3510.spendiq.model.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "limits")
@TypeConverters(Converters::class)
data class LimitsEntity(
    @PrimaryKey val userId: String,
    val frequency: String,
    val isByExpenseChecked: Boolean,
    val isByQuantityChecked: Boolean,
    val expenses: List<ExpenseEntity>,
    val totalAmount: String
)
