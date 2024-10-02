package com.isis3510.spendiq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.spendiq.model.User
import com.isis3510.spendiq.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthenticationViewModel(private val authRepository: AuthRepository = AuthRepository()) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    init {
        _user.value = authRepository.getCurrentUser()
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            authRepository.login(email, password).collect { result ->
                _loginState.value = when {
                    result.isSuccess -> {
                        _user.value = result.getOrNull()
                        LoginState.Success
                    }
                    result.isFailure -> LoginState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                    else -> LoginState.Error("Unexpected error")
                }
            }
        }
    }

    fun register(email: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            _registerState.value = RegisterState.Error("Passwords do not match")
            return
        }
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            authRepository.register(email, password).collect { result ->
                _registerState.value = when {
                    result.isSuccess -> {
                        _user.value = result.getOrNull()
                        RegisterState.Success
                    }
                    result.isFailure -> RegisterState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
                    else -> RegisterState.Error("Unexpected error")
                }
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _user.value = null
        _loginState.value = LoginState.Idle
        _registerState.value = RegisterState.Idle
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}