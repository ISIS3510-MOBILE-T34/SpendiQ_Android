package com.isis3510.spendiq.utils

import android.content.Context

import androidx.fragment.app.FragmentActivity
import com.isis3510.spendiq.model.facade.ExternalServicesFacade
import com.isis3510.spendiq.model.facade.LDServicesFacade
import java.util.Base64

class BiometricHelper(private val context: Context) {
    private val externalServicesFacade = ExternalServicesFacade(context)
    private val ldServicesFacade = LDServicesFacade(context)

    fun setupBiometricPrompt(activity: FragmentActivity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        externalServicesFacade.setupBiometricPrompt(activity, onSuccess, onError)
    }

    fun showBiometricPrompt() {
        externalServicesFacade.showBiometricPrompt()
    }

    fun storeCredentials(email: String, password: String) {
        ldServicesFacade.storeCredentials(email, password)
    }

    fun getStoredCredentials(): Pair<String?, String?> {
        val encryptedEmail = ldServicesFacade.getEncryptedEmail()
        val encryptedPassword = ldServicesFacade.getEncryptedPassword()

        return Pair(
            encryptedEmail,
            encryptedPassword
        )
    }
}