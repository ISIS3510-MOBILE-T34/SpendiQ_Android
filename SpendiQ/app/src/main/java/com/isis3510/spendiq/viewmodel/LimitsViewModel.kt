package com.isis3510.spendiq.viewmodel

import android.content.Context
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.spendiq.model.local.database.DatabaseProvider
import com.isis3510.spendiq.model.local.database.ExpenseEntity
import com.isis3510.spendiq.model.local.database.LimitsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LimitsViewModel(private val context: Context) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val limitsDao = DatabaseProvider.getDatabase(context).limitsDao()

    // MutableLiveData privada para encapsular  datos
    private val _limits = MutableLiveData<LimitsEntity>()

    // LiveData pública para exponer los datos de forma inmutable
    val limits: LiveData<LimitsEntity> get() = _limits

    init {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            viewModelScope.launch(Dispatchers.IO) {
                val localLimits = limitsDao.getLimits(userId)
                if (localLimits != null) {
                    _limits.postValue(localLimits)
                } else {
                    val newLimits = LimitsEntity(
                        userId = userId,
                        frequency = "Daily",
                        isByExpenseChecked = true,
                        isByQuantityChecked = false,
                        expenses = emptyList(),
                        totalAmount = ""
                    )
                    limitsDao.insertLimits(newLimits)
                    _limits.postValue(newLimits)
                }
            }
        } else {
        }
    }

    fun updateFrequency(frequency: String) {
        val updatedLimits = _limits.value?.copy(frequency = frequency)
        updatedLimits?.let {
            _limits.value = it
            saveLimitsLocally(it)
        }
    }

    fun updateIsByExpenseChecked(isChecked: Boolean) {
        val updatedLimits = _limits.value?.copy(isByExpenseChecked = isChecked)
        updatedLimits?.let {
            _limits.value = it
            saveLimitsLocally(it)
        }
    }

    fun updateIsByQuantityChecked(isChecked: Boolean) {
        val updatedLimits = _limits.value?.copy(isByQuantityChecked = isChecked)
        updatedLimits?.let {
            _limits.value = it
            saveLimitsLocally(it)
        }
    }

    fun updateExpenses(expenses: List<ExpenseEntity>) {
        val updatedLimits = _limits.value?.copy(expenses = expenses)
        updatedLimits?.let {
            _limits.value = it
            saveLimitsLocally(it)
        }
    }

    fun updateTotalAmount(amount: String) {
        val updatedLimits = _limits.value?.copy(totalAmount = amount)
        updatedLimits?.let {
            _limits.value = it
            saveLimitsLocally(it)
        }
    }

    private fun saveLimitsLocally(limits: LimitsEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            limitsDao.insertLimits(limits)
        }
    }

    // Función para guardar en Firebase
    fun saveLimitsToFirebase(onSuccess: () -> Unit, onFailure: () -> Unit) {
        _limits.value?.let { limits ->
            db.collection("Limits").document(limits.userId).set(limits)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure() }
        }
    }
}
