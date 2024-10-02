package com.isis3510.spendiq.repository

import com.google.firebase.auth.FirebaseAuth
import com.isis3510.spendiq.model.User
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun login(email: String, password: String): Flow<Result<User>> = flow {
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                emit(Result.success(User(it.uid, it.email ?: "")))
            } ?: emit(Result.failure(Exception("Login failed")))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun register(email: String, password: String): Flow<Result<User>> = flow {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                emit(Result.success(User(it.uid, it.email ?: "")))
            } ?: emit(Result.failure(Exception("Registration failed")))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getCurrentUser(): User? {
        return auth.currentUser?.let { User(it.uid, it.email ?: "") }
    }

    fun logout() {
        auth.signOut()
    }
}
