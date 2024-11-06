package com.isis3510.spendiq.model.facade

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * ExternalServicesFacade is a class that encapsulates the logic for setting up
 * and handling biometric authentication using Android's Biometric API.
 *
 * @property context The context used to create the BiometricPrompt and prompt information.
 */
class ExternalServicesFacade(private val context: Context) {
    private lateinit var biometricPrompt: BiometricPrompt // The biometric prompt for authentication
    private lateinit var promptInfo: BiometricPrompt.PromptInfo // Information about the biometric prompt

    /**
     * Sets up the biometric prompt with the provided activity and callback functions.
     *
     * This method initializes the biometric prompt and defines the behavior
     * for authentication success and errors.
     *
     * @param activity The FragmentActivity where the biometric prompt will be shown.
     * @param onSuccess A callback to be invoked when authentication is successful.
     * @param onError A callback to be invoked when an authentication error occurs.
     */
    fun setupBiometricPrompt(activity: FragmentActivity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        // Initialize the biometric prompt with an activity and a callback executor
        biometricPrompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(context),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess() // Call the success callback
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString()) // Call the error callback with error message
                }
            })

        // Build the prompt info with title, subtitle, and negative button text
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication") // Title for the biometric prompt
            .setSubtitle("Log in using your biometric sensor!") // Subtitle for additional context
            .setNegativeButtonText("Cancel") // Text for the negative button
            .build()
    }

    /**
     * Displays the biometric prompt for user authentication.
     *
     * This method will invoke the biometric prompt using the previously set prompt info.
     */
    fun showBiometricPrompt() {
        biometricPrompt.authenticate(promptInfo) // Show the biometric prompt
    }
}
