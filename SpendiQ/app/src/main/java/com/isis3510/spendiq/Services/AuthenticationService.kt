package com.isis3510.spendiq.services

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface AuthenticationServiceProtocol {
    fun login(email: String, password: String): Flow<Boolean>
    fun signUp(email: String, password: String): Flow<Boolean>
}

class AuthenticationService : AuthenticationServiceProtocol {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun login(email: String, password: String): Flow<Boolean> = flow {
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            emit(true)
        } catch (e: Exception) {
            emit(false)
        }
    }

    override fun signUp(email: String, password: String): Flow<Boolean> = flow {
        try {
            auth.createUserWithEmailAndPassword(email, password).await()
            emit(true)
        } catch (e: Exception) {
            emit(false)
        }
    }
}