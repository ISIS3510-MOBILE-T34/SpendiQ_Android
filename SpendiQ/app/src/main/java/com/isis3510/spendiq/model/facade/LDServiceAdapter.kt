package com.isis3510.spendiq.model.facade

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.crypto.AEADBadTagException

/**
 * LDServicesFacade is a class responsible for securely storing and retrieving
 * user credentials using encrypted SharedPreferences.
 *
 * It utilizes Android's EncryptedSharedPreferences for storing sensitive data
 * such as email and password for biometric login.
 *
 * @property context The application context used to create the encrypted SharedPreferences.
 */
class LDServiceAdapter(private val context: Context) {
    // Lazy initialization of the encrypted SharedPreferences
    private val encryptedPrefs by lazy { createEncryptedSharedPreferences() }

    /**
     * Stores user credentials securely for biometric login.
     *
     * The credentials are encrypted using Base64 encoding before being saved
     * to the SharedPreferences.
     *
     * @param email The user's email to be stored.
     * @param password The user's password to be stored.
     */
    fun storeCredentials(email: String, password: String) {
        val encryptedEmail = Base64.encodeToString(email.toByteArray(), Base64.DEFAULT) // Encrypt email
        val encryptedPassword = Base64.encodeToString(password.toByteArray(), Base64.DEFAULT) // Encrypt password
        encryptedPrefs.edit().apply {
            putString("user_email", encryptedEmail) // Store encrypted email
            putString("user_password", encryptedPassword) // Store encrypted password
            apply() // Commit changes asynchronously
        }
    }

    /**
     * Retrieves the encrypted email from SharedPreferences.
     *
     * @return The encrypted email as a String, or null if not found.
     */
    fun getEncryptedEmail(): String? {
        return encryptedPrefs.getString("user_email", null) // Retrieve encrypted email
    }

    /**
     * Retrieves the encrypted password from SharedPreferences.
     *
     * @return The encrypted password as a String, or null if not found.
     */
    fun getEncryptedPassword(): String? {
        return encryptedPrefs.getString("user_password", null) // Retrieve encrypted password
    }

    /**
     * Creates an instance of EncryptedSharedPreferences for securely storing
     * biometric login credentials.
     *
     * @return The created SharedPreferences instance.
     */
    private fun createEncryptedSharedPreferences(): SharedPreferences {
        return try {
            // Create a MasterKey for encrypting the SharedPreferences
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            // Create and return encrypted SharedPreferences
            EncryptedSharedPreferences.create(
                context,
                "secret_shared_prefs", // Name of the preferences file
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

            // Create new encrypted SharedPreferences
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
