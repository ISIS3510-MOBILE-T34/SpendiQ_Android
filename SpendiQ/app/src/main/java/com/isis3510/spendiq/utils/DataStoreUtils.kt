package com.isis3510.spendiq.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property for DataStore
private val Context.dataStore by preferencesDataStore(name = "user_preferences")

object DataStoreUtils {
    private val INCLUDE_LOCATION_KEY = booleanPreferencesKey("include_location")

    // Save the "Include Location" state to DataStore
    suspend fun setIncludeLocation(context: Context, includeLocation: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[INCLUDE_LOCATION_KEY] = includeLocation
        }
    }

    // Retrieve the "Include Location" state from DataStore
    fun getIncludeLocation(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[INCLUDE_LOCATION_KEY] ?: false // Default value: false
        }
    }
}
