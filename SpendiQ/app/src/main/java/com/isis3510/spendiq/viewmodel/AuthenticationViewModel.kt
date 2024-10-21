package com.isis3510.spendiq.viewmodel

import android.app.Application
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.isis3510.spendiq.model.User
import com.isis3510.spendiq.repository.AuthRepository
import com.isis3510.spendiq.utils.BiometricHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthenticationViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    init {
        _user.value = authRepository.getCurrentUser()
        if (_user.value != null) {
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.login(email, password).collect { result ->
                _authState.value = when {
                    result.isSuccess -> {
                        _user.value = result.getOrNull()
                        AuthState.Authenticated
                    }
                    result.isFailure -> AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                    else -> AuthState.Error("Unexpected error")
                }
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.register(email, password).collect { result ->
                _authState.value = when {
                    result.isSuccess -> {
                        _user.value = result.getOrNull()
                        AuthState.Authenticated
                    }
                    result.isFailure -> {
                        val error = result.exceptionOrNull()
                        AuthState.Error(error?.message ?: "Registration failed")
                    }
                    else -> AuthState.Error("Unexpected error during registration")
                }
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _user.value = null
        _authState.value = AuthState.Idle
    }

    fun saveUserData(data: Map<String, Any>) {
        viewModelScope.launch {
            _user.value?.let { user ->
                authRepository.saveUserData(user.id, data).collect { result ->
                    if (result.isFailure) {
                        // Handle error (you might want to add a state for this)
                        _authState.value = AuthState.Error("Failed to save user data")
                    }
                }
            }
        }
    }

    fun getUserData() {
        viewModelScope.launch {
            _user.value?.let { user ->
                authRepository.getUserData(user.id).collect { result ->
                    if (result.isSuccess) {
                        // Handle successful data retrieval (you might want to add a state for this)
                        val userData = result.getOrNull()
                        // Update UI or state with userData
                    } else {
                        // Handle error
                        _authState.value = AuthState.Error("Failed to get user data")
                    }
                }
            }
        }
    }
    private val biometricHelper = BiometricHelper(application)
    private val encryptedPrefs by lazy { createEncryptedSharedPreferences() }

    fun setupBiometricPrompt(activity: FragmentActivity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        biometricHelper.setupBiometricPrompt(activity, onSuccess, onError)
    }

    fun showBiometricPrompt() {
        biometricHelper.showBiometricPrompt()
    }

    fun enableBiometricLogin(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.login(email, password).collect { result ->
                when {
                    result.isSuccess -> {
                        val user = result.getOrNull()
                        user?.let {
                            Log.d("AuthenticationViewModel", "Login successful, storing credentials")
                            storeCredentials(email, password)
                            _authState.value = AuthState.BiometricEnabled
                        }
                    }
                    result.isFailure -> _authState.value = AuthState.Error("Failed to enable biometric login")
                }
            }
        }
    }

    fun loginWithBiometrics() {
        val encryptedEmail = encryptedPrefs.getString("user_email", null)
        val encryptedPassword = encryptedPrefs.getString("user_password", null)

        if (encryptedEmail == null || encryptedPassword == null) {
            Log.e("AuthenticationViewModel", "Credenciales no encontradas")
            _authState.value = AuthState.Error("No se encontraron credenciales")
            return
        }

        try {
            val email = String(Base64.decode(encryptedEmail, Base64.DEFAULT))
            val password = String(Base64.decode(encryptedPassword, Base64.DEFAULT))

            // Realiza el login con las credenciales desencriptadas
            login(email, password)
        } catch (e: Exception) {
            Log.e("AuthenticationViewModel", "Error al procesar las credenciales", e)
            _authState.value = AuthState.Error("Error al procesar las credenciales: ${e.message}")
        }
    }

    private fun storeCredentials(email: String, password: String) {
        val encryptedEmail = Base64.encodeToString(email.toByteArray(), Base64.DEFAULT)
        val encryptedPassword = Base64.encodeToString(password.toByteArray(), Base64.DEFAULT)
        encryptedPrefs.edit().apply {
            putString("user_email", encryptedEmail)
            putString("user_password", encryptedPassword)
            apply()
        }
    }


    private fun createEncryptedSharedPreferences(): SharedPreferences {
        val masterKey = MasterKey.Builder(getApplication())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            getApplication(),
            "secret_shared_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
    object BiometricEnabled : AuthState()
}