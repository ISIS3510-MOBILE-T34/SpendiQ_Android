package com.isis3510.spendiq.model.repository

import android.content.Context
import android.net.Uri
import com.isis3510.spendiq.model.data.User
import com.isis3510.spendiq.model.singleton.FirebaseManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/**
 * Repository class for handling authentication-related operations,
 * such as login, registration, user data management, and email verification.
 *
 * This class interacts with Firebase Authentication, Firestore, and Storage.
 *
 * @property context The application context used for operations requiring it.
 */
class AuthRepository(private val context: Context) {
    // Firebase instances for authentication, Firestore, and storage
    private val auth = FirebaseManager.auth
    private val firestore = FirebaseManager.firestore
    private val storage = FirebaseManager.storage

    /**
     * Logs in a user with the provided email and password.
     *
     * @param email The email of the user.
     * @param password The password of the user.
     * @return A Flow emitting the result containing a User object on success or an error.
     */
    fun login(email: String, password: String): Flow<Result<User>> = flow {
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                // Create and emit User object upon successful login
                val user = User(it.uid, it.email ?: "")
                emit(Result.success(user))
            } ?: emit(Result.failure(Exception("Login failed"))) // Emit failure if user is null
        } catch (e: Exception) {
            emit(Result.failure(e)) // Emit failure in case of exception
        }
    }

    /**
     * Registers a new user with the provided email, password, and additional user data.
     *
     * @param email The email of the new user.
     * @param password The password of the new user.
     * @param userData A map containing additional user data to be saved.
     * @return A Flow emitting the result containing a User object on success or an error.
     */
    fun register(email: String, password: String, userData: Map<String, Any>): Flow<Result<User>> = flow {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                // Create User object and save additional user data to Firestore
                val user = User(firebaseUser.uid, email)
                firestore.collection("users").document(user.id)
                    .set(userData)
                    .await() // Wait for Firestore operation to complete
                emit(Result.success(user)) // Emit the created user
            } ?: emit(Result.failure(Exception("Registration failed"))) // Emit failure if user is null
        } catch (e: Exception) {
            emit(Result.failure(e)) // Emit failure in case of exception
        }
    }

    /**
     * Retrieves the currently authenticated user.
     *
     * @return A User object if authenticated, null otherwise.
     */
    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return firebaseUser?.let { User(it.uid, it.email ?: "") } // Return User object or null
    }

    /**
     * Logs out the current user.
     */
    fun logout() {
        auth.signOut() // Sign out the current user
    }

    /**
     * Sends an email verification link to the currently authenticated user.
     *
     * @return A Flow emitting the result of the operation.
     */
    fun sendEmailVerification(): Flow<Result<Unit>> = flow {
        try {
            auth.currentUser?.sendEmailVerification()?.await() // Send verification email
            emit(Result.success(Unit)) // Emit success
        } catch (e: Exception) {
            emit(Result.failure(e)) // Emit failure in case of exception
        }
    }

    /**
     * Checks if the currently authenticated user's email is verified.
     *
     * @return True if the email is verified, false otherwise.
     */
    fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified ?: false // Return email verification status
    }

    /**
     * Reloads the currently authenticated user's data.
     *
     * @return A Flow emitting the result of the operation.
     */
    fun reloadUser(): Flow<Result<Unit>> = flow {
        try {
            auth.currentUser?.reload()?.await() // Reload user data
            emit(Result.success(Unit)) // Emit success
        } catch (e: Exception) {
            emit(Result.failure(e)) // Emit failure in case of exception
        }
    }

    /**
     * Saves additional user data to Firestore.
     *
     * @param userId The ID of the user to whom the data belongs.
     * @param data A map containing the data to be saved.
     * @return A Flow emitting the result of the operation.
     */
    fun saveUserData(userId: String, data: Map<String, Any>): Flow<Result<Unit>> = flow {
        try {
            firestore.collection("users").document(userId).set(data).await() // Save user data
            emit(Result.success(Unit)) // Emit success
        } catch (e: Exception) {
            emit(Result.failure(e)) // Emit failure in case of exception
        }
    }

    /**
     * Retrieves additional user data from Firestore.
     *
     * @param userId The ID of the user whose data is to be retrieved.
     * @return A Flow emitting the result containing user data on success or an error.
     */
    fun getUserData(userId: String): Flow<Result<Map<String, Any>>> = flow {
        try {
            val snapshot = firestore.collection("users").document(userId).get().await() // Retrieve user data
            if (snapshot.exists()) {
                emit(Result.success(snapshot.data ?: emptyMap())) // Emit user data if exists
            } else {
                emit(Result.failure(Exception("User data not found"))) // Emit failure if user data not found
            }
        } catch (e: Exception) {
            emit(Result.failure(e)) // Emit failure in case of exception
        }
    }

    /**
     * Uploads a profile image to Firebase Storage.
     *
     * @param uri The URI of the image to be uploaded.
     * @return A Flow emitting the result containing the image download URL on success or an error.
     */
    fun uploadProfileImage(uri: Uri): Flow<Result<String>> = flow {
        try {
            val user = auth.currentUser ?: throw Exception("User not authenticated") // Ensure user is authenticated
            val imageRef = storage.reference.child("profile_images/${user.uid}.jpg") // Reference to storage path
            val uploadTask = imageRef.putFile(uri).await() // Upload image and await result
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString() // Get download URL

            // Update Firestore with the new image URL
            firestore.collection("users").document(user.uid)
                .update("profileImageUrl", downloadUrl).await()

            emit(Result.success(downloadUrl)) // Emit the download URL
        } catch (e: Exception) {
            emit(Result.failure(e)) // Emit failure in case of exception
        }
    }

    /**
     * Sends a password reset email to the specified email address.
     *
     * @param email The email address to send the reset link to.
     * @return A Flow emitting the result of the operation.
     */
    fun sendPasswordResetEmail(email: String): Flow<Result<Unit>> = flow {
        try {
            auth.sendPasswordResetEmail(email).await() // Send password reset email
            emit(Result.success(Unit)) // Emit success
        } catch (e: Exception) {
            emit(Result.failure(e)) // Emit failure in case of exception
        }
    }
}
