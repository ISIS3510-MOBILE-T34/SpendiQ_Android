package com.isis3510.spendiq.model.singleton

object SearchBarCacheManager {
    private var searchQuery: String? = null

    fun saveQuery(query: String) {
        searchQuery = query
    }

    fun getQuery(): String {
        return searchQuery ?: ""
    }

    fun clearQuery() {
        searchQuery = null
    }
}
