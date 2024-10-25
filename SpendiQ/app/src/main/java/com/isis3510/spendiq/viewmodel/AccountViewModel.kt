package com.isis3510.spendiq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.spendiq.model.data.Account
import com.isis3510.spendiq.model.data.Transaction
import com.isis3510.spendiq.model.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AccountViewModel : ViewModel() {
    private val accountRepository = AccountRepository()

    // State flows
    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    private val _currentMoney = MutableStateFlow(0L)
    val currentMoney: StateFlow<Long> = _currentMoney

    private val _selectedTransaction = MutableStateFlow<Transaction?>(null)
    val selectedTransaction: StateFlow<Transaction?> = _selectedTransaction

    init {
        fetchAccounts()
    }

    fun fetchAccounts() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            accountRepository.getAccounts().collect { result ->
                if (result.isSuccess) {
                    val accountList = result.getOrNull() ?: emptyList()
                    _accounts.value = accountList
                    _currentMoney.value = accountList.sumOf { it.amount }
                    _uiState.value = UiState.Success
                } else {
                    _uiState.value = UiState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to fetch accounts"
                    )
                }
            }
        }
    }

    fun createAccount(accountType: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            accountRepository.createAccount(accountType).collect { result ->
                _uiState.value = when {
                    result.isSuccess -> {
                        fetchAccounts()
                        UiState.Success
                    }
                    result.isFailure -> {
                        UiState.Error(result.exceptionOrNull()?.message ?: "Failed to create account")
                    }
                    else -> UiState.Error("Unexpected error")
                }
            }
        }
    }

    fun deleteAccount(accountType: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            accountRepository.deleteAccount(accountType).collect { result ->
                _uiState.value = when {
                    result.isSuccess -> {
                        fetchAccounts()
                        UiState.Success
                    }
                    result.isFailure -> {
                        UiState.Error(result.exceptionOrNull()?.message ?: "Failed to delete account")
                    }
                    else -> UiState.Error("Unexpected error")
                }
            }
        }
    }

    fun fetchTransactions(accountId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            accountRepository.getTransactions(accountId).collect { result ->
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

    class TransactionViewModel(private val repository: AccountRepository = AccountRepository()) : ViewModel() {
        private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
        val transactions: StateFlow<List<Transaction>> = _transactions

        private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
        val uiState: StateFlow<UiState> = _uiState

        fun fetchTransactions(accountName: String) {
            viewModelScope.launch {
                _uiState.value = UiState.Loading
                try {
                    val result = repository.fetchTransactions_repo(accountName)
                    _transactions.value = result
                    _uiState.value = UiState.Success
                } catch (e: Exception) {
                    _uiState.value = UiState.Error(e.message ?: "Unknown error occurred")
                }
            }
        }

        sealed class UiState {
            data object Idle : UiState()
            data object Loading : UiState()
            data object Success : UiState()
            data class Error(val message: String) : UiState()
        }
    }

    fun addTransactionWithAccountCheck(transaction: Transaction) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            accountRepository.addTransactionWithAccountCheck(transaction).collect { result ->
                _uiState.value = when {
                    result.isSuccess -> {
                        fetchAccounts() // Refresh accounts after adding transaction
                        fetchTransactions(transaction.accountId) // Refresh transactions
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

    fun getTransaction(accountId: String, transactionId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            accountRepository.getTransaction(accountId, transactionId).collect { result ->
                when {
                    result.isSuccess -> {
                        _selectedTransaction.value = result.getOrNull()
                        _uiState.value = UiState.Success
                    }
                    result.isFailure -> {
                        _uiState.value = UiState.Error(
                            result.exceptionOrNull()?.message ?: "Failed to get transaction"
                        )
                        _selectedTransaction.value = null
                    }
                }
            }
        }
    }

    fun updateTransaction(accountId: String, oldTransaction: Transaction, newTransaction: Transaction) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            accountRepository.updateTransaction(accountId, oldTransaction, newTransaction).collect { result ->
                _uiState.value = when {
                    result.isSuccess -> {
                        fetchAccounts() // Refresh accounts after update
                        fetchTransactions(oldTransaction.accountId) // Refresh transactions
                        _selectedTransaction.value = newTransaction // Update selected transaction
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
            accountRepository.deleteTransaction(accountId, transaction).collect { result ->
                _uiState.value = when {
                    result.isSuccess -> {
                        fetchAccounts() // Refresh accounts after deletion
                        fetchTransactions(accountId) // Refresh transactions
                        _selectedTransaction.value = null // Clear selected transaction
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

    fun clearSelectedTransaction() {
        _selectedTransaction.value = null
    }

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }
}
