package com.isis3510.spendiq.model.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.spendiq.model.User
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepository(context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun login(email: String, password: String): Flow<Result<User>> = flow {
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                val user = User(it.uid, it.email ?: "")
                saveUserSession(user)
                emit(Result.success(user))
            } ?: emit(Result.failure(Exception("Login failed")))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun register(email: String, password: String): Flow<Result<User>> = flow {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                val user = User(firebaseUser.uid, email)
                firestore.collection("users").document(user.id)
                    .set(mapOf("email" to user.email))
                    .await()
                saveUserSession(user)
                emit(Result.success(user))
            } ?: emit(Result.failure(Exception("Registration failed")))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getCurrentUser(): User? {
        val userId = prefs.getString("user_id", null)
        val userEmail = prefs.getString("user_email", null)
        return if (userId != null && userEmail != null) {
            User(userId, userEmail)
        } else {
            null
        }
    }

    fun logout() {
        auth.signOut()
        clearUserSession()
    }

    fun sendEmailVerification(): Flow<Result<Unit>> = flow {
        try {
            auth.currentUser?.sendEmailVerification()?.await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified ?: false
    }

    fun reloadUser(): Flow<Result<Unit>> = flow {
        try {
            auth.currentUser?.reload()?.await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    private fun saveUserSession(user: User) {
        prefs.edit().apply {
            putString("user_id", user.id)
            putString("user_email", user.email)
            apply()
        }
    }

    private fun clearUserSession() {
        prefs.edit().clear().apply()
    }

    fun saveUserData(userId: String, data: Map<String, Any>): Flow<Result<Unit>> = flow {
        try {
            firestore.collection("users").document(userId).set(data).await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getUserData(userId: String): Flow<Result<Map<String, Any>>> = flow {
        try {
            val snapshot = firestore.collection("users").document(userId).get().await()
            if (snapshot.exists()) {
                emit(Result.success(snapshot.data ?: emptyMap()))
            } else {
                emit(Result.failure(Exception("User data not found")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}