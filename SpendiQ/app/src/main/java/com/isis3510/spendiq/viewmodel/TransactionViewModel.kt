package com.isis3510.spendiq.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.spendiq.model.data.Transaction
import com.isis3510.spendiq.model.repository.TransactionRepository
import com.isis3510.spendiq.viewmodel.AccountViewModel.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val transactionRepository: TransactionRepository = TransactionRepository()
) : ViewModel() {
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val _selectedTransaction = MutableStateFlow<Transaction?>(null)
    val selectedTransaction: StateFlow<Transaction?> = _selectedTransaction

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    fun fetchTransactions(accountName: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            transactionRepository.getTransactions(accountName).collect { result ->
                _uiState.value = when {
                    result.isSuccess -> {
                        _transactions.value = result.getOrNull() ?: emptyList()
                        UiState.Success
                    }
                    result.isFailure -> {
                        UiState.Error(result.exceptionOrNull()?.message ?: "Failed to fetch transactions")
                    }
                    else -> UiState.Error("Unexpected error")
                }
            }
        }
    }

    fun getTransaction(accountId: String, transactionId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            transactionRepository.getTransaction(accountId, transactionId).collect { result ->
                when {
                    result.isSuccess -> {
                        _selectedTransaction.value = result.getOrNull()
                        _uiState.value = UiState.Success
                    }
                    result.isFailure -> {
                        _uiState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to get transaction")
                        _selectedTransaction.value = null
                    }
                }
            }
        }
    }

    fun addTransactionWithAccountCheck(transaction: Transaction) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            transactionRepository.addTransaction(transaction).collect { result ->
                _uiState.value = when {
                    result.isSuccess -> {
                        // Refresh the transactions list for the current account
                        fetchTransactions(transaction.accountId)
                        UiState.Success
                    }
                    result.isFailure -> {
                        UiState.Error(result.exceptionOrNull()?.message ?: "Failed to add transaction")
                    }
                    else -> UiState.Error("Unexpected error")
                }
            }
        }
    }

    fun updateTransaction(accountId: String, oldTransaction: Transaction, newTransaction: Transaction) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            transactionRepository.updateTransaction(accountId, oldTransaction, newTransaction).collect { result ->
                _uiState.value = when {
                    result.isSuccess -> {
                        _selectedTransaction.value = newTransaction
                        fetchTransactions(newTransaction.accountId) // Refresh transactions list
                        UiState.Success
                    }
                    result.isFailure -> {
                        UiState.Error(result.exceptionOrNull()?.message ?: "Failed to update transaction")
                    }
                    else -> UiState.Error("Unexpected error")
                }
            }
        }
    }

    fun deleteTransaction(accountId: String, transaction: Transaction) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            transactionRepository.deleteTransaction(accountId, transaction).collect { result ->
                _uiState.value = when {
                    result.isSuccess -> {
                        _selectedTransaction.value = null
                        fetchTransactions(transaction.accountId) // Refresh transactions list
                        UiState.Success
                    }
                    result.isFailure -> {
                        UiState.Error(result.exceptionOrNull()?.message ?: "Failed to delete transaction")
                    }
                    else -> UiState.Error("Unexpected error")
                }
            }
        }
    }

    fun getIncomeAndExpenses(): Pair<Long, Long> {
        var totalIncome = 0L
        var totalExpenses = 0L

        transactions.value.forEach { transaction ->
            if (transaction.transactionType == "Income") {
                totalIncome += transaction.amount
            } else if (transaction.transactionType == "Expense") {
                totalExpenses += transaction.amount
            }
        }

        Log.d("TransactionViewModel", "Expenses: $totalExpenses")
        Log.d("TransactionViewModel", "Income: $totalIncome")

        return Pair(totalIncome, totalExpenses)
    }

    fun fetchAllTransactions() {
        Log.d("TransactionViewModel", "fetchAllTransactions called")
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            transactionRepository.getAllTransactions().collect { result ->
                _uiState.value = when {
                    result.isSuccess -> {
                        val transactionsList = result.getOrNull() ?: emptyList()
                        _transactions.value = transactionsList

                        Log.d("TransactionViewModel", "Transacciones obtenidas: $transactionsList")

                        UiState.Success
                    }
                    result.isFailure -> {
                        Log.e("TransactionViewModel", "Error al obtener transacciones: ${result.exceptionOrNull()?.message}")
                        UiState.Error(result.exceptionOrNull()?.message ?: "Failed to fetch transactions")
                    }
                    else -> UiState.Error("Unexpected error")
                }
            }
        }
    }

    fun clearSelectedTransaction() {
        _selectedTransaction.value = null
    }

    sealed class UiState {
        data object Idle : UiState()
        data object Loading : UiState()
        data object Success : UiState()
        data class Error(val message: String) : UiState()
    }
}