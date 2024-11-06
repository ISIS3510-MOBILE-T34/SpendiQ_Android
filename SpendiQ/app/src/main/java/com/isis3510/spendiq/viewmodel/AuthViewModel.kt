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
import android.util.Base64
import android.util.Log
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Represents the various states of authentication.
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    object BiometricEnabled : AuthState() // State indicating biometric login has been enabled
    object PasswordResetEmailSent : AuthState() // State indicating a password reset email has been sent
    object EmailVerificationSent : AuthState()
    object EmailVerified : AuthState()
    object EmailNotVerified : AuthState()
    data class Error(val message: String) : AuthState() // State indicating an error occurred
    object BiometricAlreadyEnabled : AuthState() // State indicating biometric login was already enabled
}

/**
 * ViewModel for managing authentication-related operations.
 *
 * This ViewModel handles user registration, login, logout, and user data management.
 * It also provides methods for biometric authentication and email verification.
 *
 * @param application The application context.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _userData = MutableStateFlow<UserDataState>(UserDataState.Idle)
    val userData: StateFlow<UserDataState> = _userData

    private val biometricHelper = BiometricHelper(application)

    private val _biometricLoginEvent = MutableSharedFlow<String>() // Event to handle the result of biometric enablement
    val biometricLoginEvent = _biometricLoginEvent.asSharedFlow() // Expose as SharedFlow

    init {
        // Initialize user data
        _user.value = authRepository.getCurrentUser()
        if (_user.value != null) {
            _authState.value = AuthState.Authenticated
        }
    }

    /**
     * Registers a new user with the provided information.
     *
     * @param email User's email address.
     * @param password User's password.
     * @param fullName User's full name.
     * @param phoneNumber User's phone number.
     * @param birthDate User's birth date.
     */
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

    /**
     * Logs in a user with the provided email and password.
     *
     * @param email User's email address.
     * @param password User's password.
     */
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

    /**
     * Logs out the current user.
     */
    fun logout() {
        authRepository.logout()
        _user.value = null
        _authState.value = AuthState.Idle
    }

    /**
     * Saves user data to the repository.
     *
     * @param data User data to be saved.
     */
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

    /**
     * Retrieves user data from the repository.
     */
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

    /**
     * Uploads the user's profile image to the repository.
     *
     * @param uri The URI of the image to upload.
     */
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

    /**
     * Represents the various states of user data operations.
     */
    sealed class UserDataState {
        object Idle : UserDataState()
        object Loading : UserDataState()
        data class Success(val data: Map<String, Any>) : UserDataState()
        data class Error(val message: String) : UserDataState()
    }

    /**
     * Sends a verification email to the user's email address.
     */
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

    /**
     * Checks if the user's email is verified.
     */
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

    /**
     * Sends a password reset email to the user's email address.
     *
     * @param email User's email address.
     */
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

    /**
     * Sets up the biometric authentication prompt.
     *
     * @param activity The FragmentActivity to show the prompt in.
     * @param onSuccess Callback when biometric authentication is successful.
     * @param onError Callback when an error occurs during biometric authentication.
     */
    fun setupBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        biometricHelper.setupBiometricPrompt(activity, onSuccess, onError)
    }

    /**
     * Shows the biometric authentication prompt.
     */
    fun showBiometricPrompt() {
        biometricHelper.showBiometricPrompt()
    }

    /**
     * Enables biometric login by storing the user's credentials.
     *
     * @param email User's email address.
     * @param password User's password.
     */
    fun enableBiometricLogin(email: String, password: String) {
        viewModelScope.launch {
            // Check if biometric login is already enabled
            if (biometricHelper.isBiometricEnabled()) {
                _authState.value = AuthState.BiometricAlreadyEnabled // Change state to indicate biometric already enabled
                return@launch // Exit the function
            }

            _authState.value = AuthState.Loading
            authRepository.login(email, password).collect { result ->
                when {
                    result.isSuccess -> {
                        val user = result.getOrNull()
                        user?.let {
                            Log.d("AuthViewModel", "Login successful, storing credentials")
                            biometricHelper.storeCredentials(email, password)
                            _authState.value = AuthState.BiometricEnabled // Change state to enabled
                        }
                    }
                    result.isFailure -> {
                        _authState.value = AuthState.Error("Failed to enable biometric login")
                    }
                }
            }
        }
    }

    /**
     * Logs in the user using stored biometric credentials.
     */
    fun loginWithBiometrics() {
        val (encryptedEmail, encryptedPassword) = biometricHelper.getStoredCredentials()

        if (encryptedEmail == null || encryptedPassword == null) {
            _authState.value = AuthState.Error("Biometrics are not enabled. Please, enable them and try again")
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

    /**
     * Resets the authentication state to idle.
     */
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}
