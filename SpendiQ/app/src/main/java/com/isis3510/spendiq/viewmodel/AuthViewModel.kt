package com.isis3510.spendiq.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.spendiq.model.data.User
import com.isis3510.spendiq.model.repository.AuthRepository
import com.isis3510.spendiq.utils.BiometricHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object Authenticated : AuthState()
    data object BiometricEnabled : AuthState() // Re-added this state
    data object PasswordResetEmailSent : AuthState() // Fixed the missing reference
    data object EmailVerificationSent : AuthState()
    data object EmailVerified : AuthState()
    data object EmailNotVerified : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _userData = MutableStateFlow<UserDataState>(UserDataState.Idle)
    val userData: StateFlow<UserDataState> = _userData

    private val biometricHelper = BiometricHelper(application)

    init {
        _user.value = authRepository.getCurrentUser()
        if (_user.value != null) {
            _authState.value = AuthState.Authenticated
        }
    }

    fun register(email: String, password: String, fullName: String, phoneNumber: String, birthDate: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val userData = mapOf(
                "fullName" to fullName,
                "email" to email,
                "phoneNumber" to phoneNumber,
                "birthDate" to birthDate,
                "registrationDate" to Date()
            )
            authRepository.register(email, password, userData).collect { result ->
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
                        _authState.value = AuthState.Error("Failed to save user data")
                    }
                }
            }
        }
    }

    fun getUserData() {
        viewModelScope.launch {
            _userData.value = UserDataState.Loading
            _user.value?.let { user ->
                authRepository.getUserData(user.id).collect { result ->
                    _userData.value = if (result.isSuccess) {
                        UserDataState.Success(result.getOrNull() ?: emptyMap())
                    } else {
                        UserDataState.Error(result.exceptionOrNull()?.message ?: "Failed to get user data")
                    }
                }
            }
        }
    }

    fun uploadProfileImage(uri: Uri) {
        viewModelScope.launch {
            _userData.value = UserDataState.Loading
            authRepository.uploadProfileImage(uri).collect { result ->
                if (result.isSuccess) {
                    getUserData() // Refresh user data after successful upload
                } else {
                    _userData.value = UserDataState.Error(result.exceptionOrNull()?.message ?: "Failed to upload profile image")
                }
            }
        }
    }

    sealed class UserDataState {
        data object Idle : UserDataState()
        data object Loading : UserDataState()
        data class Success(val data: Map<String, Any>) : UserDataState()
        data class Error(val message: String) : UserDataState()
    }

    // Send email verification
    fun sendEmailVerification() {
        viewModelScope.launch {
            authRepository.sendEmailVerification().collect { result ->
                _authState.value = when {
                    result.isSuccess -> AuthState.EmailVerificationSent
                    result.isFailure -> AuthState.Error(result.exceptionOrNull()?.message ?: "Failed to send verification email")
                    else -> AuthState.Error("Unexpected error")
                }
            }
        }
    }

    // Check if the user's email is verified
    fun checkEmailVerification() {
        viewModelScope.launch {
            authRepository.reloadUser().collect { result ->
                if (result.isSuccess) {
                    if (authRepository.isEmailVerified()) {
                        _authState.value = AuthState.EmailVerified
                    } else {
                        _authState.value = AuthState.EmailNotVerified
                    }
                } else {
                    _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Failed to check email verification")
                }
            }
        }
    }

    // Send password reset email
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.sendPasswordResetEmail(email).collect { result ->
                _authState.value = when {
                    result.isSuccess -> AuthState.PasswordResetEmailSent
                    result.isFailure -> AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                    else -> AuthState.Error("Unexpected error")
                }
            }
        }
    }

    // Biometric login setup
    fun setupBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        biometricHelper.setupBiometricPrompt(activity, onSuccess, onError)
    }

    // Show biometric login prompt
    fun showBiometricPrompt() {
        biometricHelper.showBiometricPrompt()
    }

    // Enable biometric login by saving credentials
    fun enableBiometricLogin(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.login(email, password).collect { result ->
                when {
                    result.isSuccess -> {
                        val user = result.getOrNull()
                        user?.let {
                            Log.d("AuthViewModel", "Login successful, storing credentials")
                            biometricHelper.storeCredentials(email, password)
                            _authState.value = AuthState.BiometricEnabled
                        }
                    }
                    result.isFailure -> _authState.value = AuthState.Error("Failed to enable biometric login")
                }
            }
        }
    }

    // Perform login using stored biometric credentials
    fun loginWithBiometrics() {
        val (encryptedEmail, encryptedPassword) = biometricHelper.getStoredCredentials()

        if (encryptedEmail == null || encryptedPassword == null) {
            _authState.value = AuthState.Error("No stored credentials found")
            return
        }

        try {
            val email = String(Base64.decode(encryptedEmail, Base64.DEFAULT))
            val password = String(Base64.decode(encryptedPassword, Base64.DEFAULT))
            login(email, password)
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Error processing credentials: ${e.message}")
        }
    }


    // Reset authentication state
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}
