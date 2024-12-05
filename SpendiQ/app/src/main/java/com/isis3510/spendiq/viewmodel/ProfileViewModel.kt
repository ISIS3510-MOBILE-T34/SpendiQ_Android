package com.isis3510.spendiq.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel : ViewModel() {

    private val _profileImageUri = MutableStateFlow<Uri?>(null)
    val profileImageUri: StateFlow<Uri?> = _profileImageUri

    private val PREFS_NAME = "profile_prefs"
    private val KEY_PROFILE_IMAGE_URI = "profile_image_uri"

    fun saveProfileImage(context: Context, uri: Uri) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(KEY_PROFILE_IMAGE_URI, uri.toString()).apply()
        _profileImageUri.value = uri
    }

    fun loadProfileImage(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val uriString = sharedPreferences.getString(KEY_PROFILE_IMAGE_URI, null)
        _profileImageUri.value = uriString?.let { Uri.parse(it) }
    }

    fun clearProfileImage(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().remove(KEY_PROFILE_IMAGE_URI).apply()
        _profileImageUri.value = null
    }
}
