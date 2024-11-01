package com.isis3510.spendiq.model.facade

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class ExternalServicesFacade(private val context: Context) {
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    fun setupBiometricPrompt(activity: FragmentActivity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        biometricPrompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(context),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Log in user your biometric sensor!")
            .setNegativeButtonText("Cancel")
            .build()
    }

    fun showBiometricPrompt() {
        biometricPrompt.authenticate(promptInfo)
    }
}