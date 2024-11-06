package com.isis3510.spendiq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.spendiq.model.data.Account
import com.isis3510.spendiq.model.data.Transaction
import com.isis3510.spendiq.model.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing account-related operations.
 *
 * This ViewModel handles fetching, creating, and deleting accounts, as well as managing
 * the current money state and associated transactions.
 */
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
        fetchAccounts() // Fetch accounts when ViewModel is initialized
    }

    /**
     * Fetches accounts from the repository and updates the UI state.
     */
    fun fetchAccounts() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading // Set loading state
            accountRepository.getAccounts().collect { result ->
                if (result.isSuccess) {
                    val accountList = result.getOrNull() ?: emptyList() // Get accounts
                    _accounts.value = accountList
                    _currentMoney.value = accountList.sumOf { it.amount } // Calculate current money
                    _uiState.value = UiState.Success // Set success state
                } else {
                    _uiState.value = UiState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to fetch accounts" // Handle error
                    )
                }
            }
        }
    }

    /**
     * Creates a new account of the specified type.
     *
     * @param accountType The type of account to create.
     */
    fun createAccount(accountType: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading // Set loading state
            accountRepository.createAccount(accountType).collect { result ->
                _uiState.value = when {
                    result.isSuccess -> {
                        fetchAccounts() // Refresh accounts
                        UiState.Success // Set success state
                    }
                    result.isFailure -> {
                        UiState.Error(result.exceptionOrNull()?.message ?: "Failed to create account") // Handle error
                    }
                    else -> UiState.Error("Unexpected error") // Handle unexpected state
                }
            }
        }
    }

    /**
     * Deletes an account of the specified type.
     *
     * @param accountType The type of account to delete.
     */
    fun deleteAccount(accountType: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading // Set loading state
            accountRepository.deleteAccount(accountType).collect { result ->
                _uiState.value = when {
                    result.isSuccess -> {
                        fetchAccounts() // Refresh accounts
                        UiState.Success // Set success state
                    }
                    result.isFailure -> {
                        UiState.Error(result.exceptionOrNull()?.message ?: "Failed to delete account") // Handle error
                    }
                    else -> UiState.Error("Unexpected error") // Handle unexpected state
                }
            }
        }
    }

    /**
     * Represents the various UI states for account operations.
     */
    sealed class UiState {
        object Idle : UiState() // Initial state
        object Loading : UiState() // Loading state
        object Success : UiState() // Success state
        data class Error(val message: String) : UiState() // Error state with a message
    }
}
