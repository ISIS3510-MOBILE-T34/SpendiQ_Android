package com.isis3510.spendiq.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ProfileViewModel class
 *
 * This ViewModel manages the user's profile data, including the profile image and user details.
 * It provides functionalities to save, load, and manage profile image URIs and user data using
 * Kotlin's Flow for reactive data handling.
 *
 * Key Features:
 * - Profile Image Management: Allows saving and retrieving the user's profile image URI.
 * - User Data Management: Facilitates storing and accessing user-related data in a reactive manner.
 *
 * State Management:
 * - `_profileImageUri`: MutableStateFlow holding the URI of the user's profile image.
 * - `profileImageUri`: Public immutable StateFlow for observing changes to the profile image URI.
 * - `_userData`: MutableStateFlow holding user data in a map format.
 * - `userData`: Public immutable StateFlow for observing changes to user data.
 *
 * Profile Image Functions:
 * - `saveProfileImage(context: Context, uri: Uri)`: Saves the provided profile image URI and updates the state.
 * - `loadProfileImage(context: Context)`: Loads the profile image URI from shared preferences and updates the state.
 *
 * User Data Functions:
 * - `setUserData(data: Map<String, Any?>)`: Updates the user data state with the provided map.
 *
 * Private Functions:
 * - `saveProfileImageUri(context: Context, uri: Uri)`: Saves the profile image URI to shared preferences.
 * - `getProfileImageUri(context: Context)`: Retrieves the profile image URI from shared preferences.
 */
class ProfileViewModel : ViewModel() {

    // MutableStateFlow for the profile image URI
    private val _profileImageUri = MutableStateFlow<Uri?>(null)

    // Public immutable StateFlow for the profile image URI
    val profileImageUri: StateFlow<Uri?> = _profileImageUri

    // MutableStateFlow for user data
    private val _userData = MutableStateFlow<Map<String, Any?>?>(null)

    // Public immutable StateFlow for user data
    val userData: StateFlow<Map<String, Any?>?> = _userData

    // Save the profile image URI and update the state
    fun saveProfileImage(context: Context, uri: Uri) {
        saveProfileImageUri(context, uri)
        _profileImageUri.value = uri
    }

    // Load the profile image URI from shared preferences and update the state
    fun loadProfileImage(context: Context) {
        _profileImageUri.value = getProfileImageUri(context)
    }

    // Update the user data state
    fun setUserData(data: Map<String, Any?>) {
        _userData.value = data
    }

    // Save the profile image URI to shared preferences
    private fun saveProfileImageUri(context: Context, uri: Uri) {
        val sharedPreferences = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("profile_image_uri", uri.toString()).apply()
    }

    // Retrieve the profile image URI from shared preferences
    private fun getProfileImageUri(context: Context): Uri? {
        val sharedPreferences = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        val uriString = sharedPreferences.getString("profile_image_uri", null)
        return uriString?.let { Uri.parse(it) }
    }
}
