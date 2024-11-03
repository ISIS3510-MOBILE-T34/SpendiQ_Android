// ProfileViewModel.kt
package com.isis3510.spendiq.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel : ViewModel() {

    private val _profileImageUri = MutableStateFlow<Uri?>(null)
    val profileImageUri: StateFlow<Uri?> = _profileImageUri

    private val _userData = MutableStateFlow<Map<String, Any?>?>(null)
    val userData: StateFlow<Map<String, Any?>?> = _userData

    fun saveProfileImage(context: Context, uri: Uri) {
        saveProfileImageUri(context, uri)
        _profileImageUri.value = uri
    }

    fun loadProfileImage(context: Context) {
        _profileImageUri.value = getProfileImageUri(context)
    }

    fun setUserData(data: Map<String, Any?>) {
        _userData.value = data
    }

    private fun saveProfileImageUri(context: Context, uri: Uri) {
        val sharedPreferences = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("profile_image_uri", uri.toString()).apply()
    }

    private fun getProfileImageUri(context: Context): Uri? {
        val sharedPreferences = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        val uriString = sharedPreferences.getString("profile_image_uri", null)
        return uriString?.let { Uri.parse(it) }
    }
}
