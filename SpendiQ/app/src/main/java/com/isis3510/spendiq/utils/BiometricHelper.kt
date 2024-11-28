package com.isis3510.spendiq.utils

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.isis3510.spendiq.model.facade.ExternalServicesFacade
import com.isis3510.spendiq.model.facade.LDServicesFacade
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * BiometricHelper is a utility class that handles biometric authentication
 * operations and the storage of user credentials.
 *
 * It interacts with external services for setting up biometric prompts and
 * securely storing user credentials such as email and password.
 *
 * @param context The application context to access services.
 */
class BiometricHelper(private val context: Context) {
    private val externalServicesFacade = ExternalServicesFacade(context) // Facade for external services
    private val ldServicesFacade = LDServicesFacade(context) // Facade for local data services

    /**
     * Sets up the biometric prompt for authentication.
     *
     * @param activity The FragmentActivity where the biometric prompt will be displayed.
     * @param onSuccess Callback function to be executed on successful biometric authentication.
     * @param onError Callback function to handle errors during biometric authentication.
     */
    fun setupBiometricPrompt(activity: FragmentActivity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        externalServicesFacade.setupBiometricPrompt(activity, onSuccess, onError) // Delegate to the facade
    }

    /**
     * Displays the biometric prompt to the user for authentication.
     */
    fun showBiometricPrompt() {
        externalServicesFacade.showBiometricPrompt() // Delegate to the facade to show the prompt
    }

    /**
     * Stores the user's credentials securely.
     *
     * @param email The user's email to be stored.
     * @param password The user's password to be stored.
     */
    suspend fun storeCredentials(email: String, password: String) {
        withContext(Dispatchers.IO) {
            ldServicesFacade.storeCredentials(email, password)
        }
    }

    /**
     * Retrieves the stored user credentials.
     *
     * @return A pair containing the encrypted email and password.
     *         Both values may be null if no credentials are stored.
     */
    suspend fun getStoredCredentials(): Pair<String?, String?> {
        return withContext(Dispatchers.IO) {
            ldServicesFacade.getStoredCredentials()
        }
    }

    /**
     * Checks if biometric authentication is enabled by verifying
     * if the user credentials are stored.
     *
     * @return True if biometric authentication can be used,
     *         false otherwise.
     */
    fun isBiometricEnabled(): Boolean {
        // Check if the credentials are stored
        val encryptedEmail = ldServicesFacade.getEncryptedEmail() // Retrieve the encrypted email
        val encryptedPassword = ldServicesFacade.getEncryptedPassword() // Retrieve the encrypted password
        return encryptedEmail != null && encryptedPassword != null // Return true if both are present
    }
}
