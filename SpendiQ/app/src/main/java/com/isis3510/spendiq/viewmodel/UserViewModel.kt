package com.isis3510.spendiq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * UserViewModel class
 *
 * This ViewModel is responsible for managing the user's data and loading it from Firebase Firestore.
 * It utilizes Firebase Authentication to retrieve the current user's ID and fetch their related data from Firestore.
 * The user data is stored in a StateFlow which allows the UI to reactively observe changes.
 *
 * Key Features:
 * - Reactive User Data Management: Exposes user data as a StateFlow to allow UI components to subscribe and react to changes.
 * - Firebase Integration: Fetches user data from Firebase Firestore using the authenticated user's ID.
 * - Error Handling: Provides basic error handling for data retrieval failures.
 *
 * Initialization:
 * - On initialization, the ViewModel fetches the user data by calling `loadUserDataFromFirebase`.
 *
 * State Management:
 * - `_userData`: A private MutableStateFlow that holds the user's data.
 * - `userData`: A public immutable StateFlow that exposes user data to the UI.
 *
 * Firebase Interaction:
 * - Uses FirebaseAuth to obtain the current user's ID.
 * - Queries the Firestore database to fetch the user's document using the user ID.
 * - Updates the `_userData` state when data is successfully retrieved.
 *
 * Error Handling:
 * - Logs errors encountered during data retrieval to the console.
 * - Handles the case where no authenticated user exists.
 */
class UserViewModel : ViewModel() {
    // Private mutable state flow for user data
    private val _userData = MutableStateFlow<Map<String, Any?>>(emptyMap())

    // Public immutable state flow for user data
    val userData: StateFlow<Map<String, Any?>> = _userData

    // Initializing the ViewModel and loading user data from Firebase
    init {
        loadUserDataFromFirebase()
    }

    // Function to load user data from Firebase Firestore
    private fun loadUserDataFromFirebase() {
        // Get the current user
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        if (userId != null) {
            viewModelScope.launch {
                val firestore = FirebaseFirestore.getInstance()
                val userDocument = firestore.collection("users").document(userId)

                // Retrieve user document from Firestore
                userDocument.get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            // Extract only the necessary fields
                            _userData.value = mapOf(
                                "fullName" to document.getString("fullName"),
                                "email" to document.getString("email"),
                                "phoneNumber" to document.getString("phoneNumber"),
                                "birthDate" to document.getString("birthDate")
                            )
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Handle the error
                        println("Error retrieving user data: $exception")
                    }
            }
        } else {
            // Handle the case where no authenticated user exists
            println("No authenticated user found.")
        }
    }
}
