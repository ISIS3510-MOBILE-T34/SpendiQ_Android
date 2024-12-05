package com.isis3510.spendiq.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property for DataStore instance tied to the context
// Creates or retrieves a DataStore instance named "user_preferences"
private val Context.dataStore by preferencesDataStore(name = "user_preferences")

// Utility object for managing DataStore operations
object DataStoreUtils {

    // Define a key for storing/retrieving the "Include Location" preference
    private val INCLUDE_LOCATION_KEY = booleanPreferencesKey("include_location")

    /**
     * Save the "Include Location" state to DataStore.
     * @param context The context from which to access the DataStore.
     * @param includeLocation The boolean value indicating the user's preference.
     */
    suspend fun setIncludeLocation(context: Context, includeLocation: Boolean) {
        context.dataStore.edit { preferences ->
            // Store the value in DataStore with the key INCLUDE_LOCATION_KEY
            preferences[INCLUDE_LOCATION_KEY] = includeLocation
        }
    }

    /**
     * Retrieve the "Include Location" state from DataStore as a Flow.
     * @param context The context from which to access the DataStore.
     * @return A Flow emitting the boolean value stored for the preference.
     *         Defaults to 'false' if no value is found.
     */
    fun getIncludeLocation(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            // Retrieve the value associated with INCLUDE_LOCATION_KEY, or return false if not found
            preferences[INCLUDE_LOCATION_KEY] ?: false
        }
    }
}
