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

    fun logout() {
        authRepository.logout()
        _user.value = null
        _loginState.value = LoginState.Idle
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}