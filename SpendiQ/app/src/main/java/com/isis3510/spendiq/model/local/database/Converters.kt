package com.isis3510.spendiq.model.local.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromExpenseEntityList(value: List<ExpenseEntity>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toExpenseEntityList(value: String): List<ExpenseEntity> {
        val listType = object : TypeToken<List<ExpenseEntity>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
