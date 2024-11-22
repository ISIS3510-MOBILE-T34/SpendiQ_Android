package com.isis3510.spendiq.model.cache

import com.isis3510.spendiq.model.data.DailyTransaction

class MovementsCache {
    private val cache = mutableMapOf<String, List<DailyTransaction>>()

    fun getMovements(key: String): List<DailyTransaction>? {
        return cache[key]
    }

    fun saveMovements(key: String, movements: List<DailyTransaction>) {
        cache[key] = movements
    }
}