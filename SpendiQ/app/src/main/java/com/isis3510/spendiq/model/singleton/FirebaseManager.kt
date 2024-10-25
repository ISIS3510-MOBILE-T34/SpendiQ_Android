package com.isis3510.spendiq.model.singleton

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseManager {
    // Firebase instances
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }

    // Getter methods
    fun getAuth(): FirebaseAuth = auth
    fun getFirestore(): FirebaseFirestore = firestore
    fun getStorage(): FirebaseStorage = storage
}