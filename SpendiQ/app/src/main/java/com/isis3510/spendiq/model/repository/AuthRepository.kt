package com.isis3510.spendiq.model.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.isis3510.spendiq.model.data.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    fun login(email: String, password: String): Flow<Result<User>> = flow {
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                val user = User(it.uid, it.email ?: "")
                emit(Result.success(user))
            } ?: emit(Result.failure(Exception("Login failed")))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun register(email: String, password: String, userData: Map<String, Any>): Flow<Result<User>> = flow {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                val user = User(firebaseUser.uid, email)
                firestore.collection("users").document(user.id)
                    .set(userData)
                    .await()
                emit(Result.success(user))
            } ?: emit(Result.failure(Exception("Registration failed")))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return firebaseUser?.let { User(it.uid, it.email ?: "") }
    }

    fun logout() {
        auth.signOut()
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

    fun uploadProfileImage(uri: Uri): Flow<Result<String>> = flow {
        try {
            val user = auth.currentUser ?: throw Exception("User not authenticated")
            val imageRef = storage.reference.child("profile_images/${user.uid}.jpg")
            val uploadTask = imageRef.putFile(uri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

            firestore.collection("users").document(user.uid)
                .update("profileImageUrl", downloadUrl).await()

            emit(Result.success(downloadUrl))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}