package com.isis3510.spendiq.model.singleton

// Singleton object to cache and manage the search query across the app
object SearchBarCacheManager {
    // Private variable to hold the cached search query
    private var searchQuery: String? = null

    // Function to save a query in the cache
    fun saveQuery(query: String) {
        searchQuery = query // Store the query in memory
    }

    // Function to retrieve the cached query
    fun getQuery(): String {
        return searchQuery ?: "" // Return the cached query if available, or an empty string if null
    }
}
