package com.isis3510.spendiq.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.spendiq.model.User
import com.isis3510.spendiq.model.repository.AuthRepository
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
            _user.value?.let { user ->
                authRepository.getUserData(user.id).collect { result ->
                    if (result.isSuccess) {
                        // Handle successful data retrieval
                        val userData = result.getOrNull()
                        // Update UI or state with userData
                    } else {
                        _authState.value = AuthState.Error("Failed to get user data")
                    }
                }
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