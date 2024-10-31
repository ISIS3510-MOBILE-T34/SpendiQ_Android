package com.isis3510.spendiq.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _profileImageUri: MutableStateFlow<Uri?> = MutableStateFlow(null)
    val profileImageUri: StateFlow<Uri?> get() = _profileImageUri

    fun loadProfileImage(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val uri = getProfileImageUri(context)
            _profileImageUri.value = uri
        }
    }

    fun saveProfileImage(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            saveProfileImageUri(context, uri)
            _profileImageUri.value = uri
        }
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
