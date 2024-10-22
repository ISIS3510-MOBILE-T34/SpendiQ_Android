package com.isis3510.spendiq.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.spendiq.model.data.User
import com.isis3510.spendiq.model.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _userData = MutableStateFlow<UserDataState>(UserDataState.Idle)
    val userData: StateFlow<UserDataState> = _userData

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

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Authenticated : AuthState()
        object EmailVerificationSent : AuthState()
        object EmailVerified : AuthState()
        object EmailNotVerified : AuthState()
        data class Error(val message: String) : AuthState()
    }

    sealed class UserDataState {
        object Idle : UserDataState()
        object Loading : UserDataState()
        data class Success(val data: Map<String, Any>) : UserDataState()
        data class Error(val message: String) : UserDataState()
    }
}