package com.isis3510.spendiq.model.facade

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.crypto.AEADBadTagException

class LDServicesFacade(private val context: Context) {
    private val encryptedPrefs by lazy { createEncryptedSharedPreferences() }
    // Store credentials securely for biometric login
    fun storeCredentials(email: String, password: String) {
        val encryptedEmail = Base64.encodeToString(email.toByteArray(), Base64.DEFAULT)
        val encryptedPassword = Base64.encodeToString(password.toByteArray(), Base64.DEFAULT)
        encryptedPrefs.edit().apply {
            putString("user_email", encryptedEmail)
            putString("user_password", encryptedPassword)
            apply()
        }
    }

    fun getEncryptedEmail(): String? {
        return encryptedPrefs.getString("user_email", null)
    }

    fun getEncryptedPassword(): String? {
        return encryptedPrefs.getString("user_password", null)
    }

    // Create encrypted SharedPreferences to store biometric login credentials
    private fun createEncryptedSharedPreferences(): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "secret_shared_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: AEADBadTagException) {
            // Handle AEADBadTagException by recreating the SharedPreferences
            context.getSharedPreferences("secret_shared_prefs", Context.MODE_PRIVATE).edit().clear().apply()
            val newMasterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "secret_shared_prefs",
                newMasterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }
}