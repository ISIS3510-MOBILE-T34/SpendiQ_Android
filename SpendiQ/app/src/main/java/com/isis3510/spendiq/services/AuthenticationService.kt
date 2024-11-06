package com.isis3510.spendiq.services

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// Protocol defining the authentication service operations
interface AuthenticationServiceProtocol {
    /**
     * Logs in a user with the given email and password.
     * @param email The email of the user.
     * @param password The password of the user.
     * @return A Flow that emits true if the login is successful, false otherwise.
     */
    fun login(email: String, password: String): Flow<Boolean>

    /**
     * Signs up a new user with the given email and password.
     * @param email The email of the new user.
     * @param password The password of the new user.
     * @return A Flow that emits true if the sign-up is successful, false otherwise.
     */
    fun signUp(email: String, password: String): Flow<Boolean>
}

// Implementation of the authentication service using Firebase Authentication
class AuthenticationService : AuthenticationServiceProtocol {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance() // Firebase authentication instance

    /**
     * Logs in a user with the provided email and password.
     * Uses Firebase Authentication to sign in the user.
     * @param email The user's email.
     * @param password The user's password.
     * @return A Flow that emits true if login is successful, or false if it fails.
     */
    override fun login(email: String, password: String): Flow<Boolean> = flow {
        try {
            // Attempt to sign in with the provided email and password
            auth.signInWithEmailAndPassword(email, password).await()
            emit(true) // Emit true if sign in is successful
        } catch (e: Exception) {
            emit(false) // Emit false if there is an error
        }
    }

    /**
     * Signs up a new user using the provided email and password.
     * Utilizes Firebase Authentication to create the user.
     * @param email The new user's email.
     * @param password The new user's password.
     * @return A Flow that emits true if sign up is successful, or false if it fails.
     */
    override fun signUp(email: String, password: String): Flow<Boolean> = flow {
        try {
            // Attempt to create a new user with the provided email and password
            auth.createUserWithEmailAndPassword(email, password).await()
            emit(true) // Emit true if sign up is successful
        } catch (e: Exception) {
            emit(false) // Emit false if there is an error
        }
    }
}
