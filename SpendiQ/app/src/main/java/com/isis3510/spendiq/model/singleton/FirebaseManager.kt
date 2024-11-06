package com.isis3510.spendiq.model.singleton

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

/**
 * Singleton object to manage Firebase services.
 *
 * This object provides lazy-loaded instances of Firebase Authentication,
 * Firestore Database, and Firebase Storage. By using a singleton pattern,
 * it ensures that there is only one instance of each service throughout
 * the application lifecycle, which can help to optimize resource usage.
 */
object FirebaseManager {
    // Lazy initialization of Firebase Authentication instance
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    // Lazy initialization of Firestore Database instance
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // Lazy initialization of Firebase Storage instance
    val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
}
