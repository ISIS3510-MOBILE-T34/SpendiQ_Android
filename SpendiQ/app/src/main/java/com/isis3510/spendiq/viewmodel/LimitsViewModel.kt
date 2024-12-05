package com.isis3510.spendiq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.spendiq.model.local.database.DatabaseProvider
import com.isis3510.spendiq.model.local.database.ExpenseEntity
import com.isis3510.spendiq.model.local.database.LimitsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LimitsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val limitsDao = DatabaseProvider.getDatabase().limitsDao()

    fun saveLimitsLocally(limits: LimitsEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            limitsDao.insertLimits(limits)
        }
    }

    fun getLimitsFromLocal(userId: String, onResult: (LimitsEntity?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val limits = limitsDao.getLimits(userId)
            onResult(limits)
        }
    }

    fun saveLimitsToFirebase(limits: LimitsEntity, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val userId = limits.userId
        db.collection("Limits").document(userId).set(limits)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure() }
    }
}
