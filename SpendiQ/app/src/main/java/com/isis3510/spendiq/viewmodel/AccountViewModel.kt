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

    // Flow to observe the list of accounts
    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts

    // Flow to observe the list of transactions
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    // Flow to track the UI state
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    // Flow to observe the current balance
    private val _currentMoney = MutableStateFlow(0L)
    val currentMoney: StateFlow<Long> = _currentMoney

    init {
        // Initialize by fetching the list of accounts when the ViewModel is created
        fetchAccounts()
    }

    // Method to fetch all accounts from the repository
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
                    _uiState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to fetch accounts")
                }
            }
        }
    }

    fun createAccount(accountType: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            accountRepository.createAccount(accountType).collect { result ->
                if (result.isSuccess) {
                    fetchAccounts()  // Refresh accounts after creation
                    _uiState.value = UiState.Success
                } else {
                    _uiState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to create account")
                }
            }
        }
    }


    // Method to add a transaction, which will create the account if it doesn't exist
    fun addTransactionWithAccountCheck(transaction: Transaction) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            accountRepository.addTransactionWithAccountCheck(transaction).collect { result ->
                if (result.isSuccess) {
                    // Refresh the accounts and transactions after adding a transaction
                    fetchAccounts()
                    fetchTransactions(transaction.accountId)
                    _uiState.value = UiState.Success
                } else {
                    _uiState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to add transaction")
                }
            }
        }
    }

    // Method to delete an account by account type
    fun deleteAccount(accountType: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            accountRepository.deleteAccount(accountType).collect { result ->
                if (result.isSuccess) {
                    fetchAccounts()
                    _uiState.value = UiState.Success
                } else {
                    _uiState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to delete account")
                }
            }
        }
    }

    // Method to fetch transactions for a specific account
    fun fetchTransactions(accountName: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            accountRepository.getTransactions(accountName).collect { result ->
                if (result.isSuccess) {
                    _transactions.value = result.getOrNull() ?: emptyList()
                    _uiState.value = UiState.Success
                } else {
                    _uiState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to fetch transactions")
                }
            }
        }
    }

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }
}
