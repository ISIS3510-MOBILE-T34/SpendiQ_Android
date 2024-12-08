package com.isis3510.spendiq.viewmodel

import android.util.Log
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
    private val accountRepository = AccountRepository.getInstance() // Singleton instance of the repository

    // State flows
    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    // Estado para las 3 cuentas más recientes
    private val _top3Accounts = MutableStateFlow<List<Account>>(emptyList())
    val top3Accounts: StateFlow<List<Account>> = _top3Accounts

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    private val _currentMoney = MutableStateFlow(0L)
    val currentMoney: StateFlow<Long> = _currentMoney

    private val _selectedTransaction = MutableStateFlow<Transaction?>(null)
    val selectedTransaction: StateFlow<Transaction?> = _selectedTransaction

    init {
        observeAccounts()
        observeTop3RecentAccounts()
    }

    /**
     * Observa las cuentas en tiempo real y actualiza el estado.
     */
    fun observeAccounts() {
        viewModelScope.launch {
            accountRepository.getAccountsRealTime().collect { result ->
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

    /**
     * Observa las 3 cuentas más recientes en tiempo real y actualiza el estado.
     */
    private fun observeTop3RecentAccounts() {
        viewModelScope.launch {
            accountRepository.getTop3RecentAccountsRealTime().collect { result ->
                if (result.isSuccess) {
                    val topAccounts = result.getOrNull() ?: emptyList()
                    _top3Accounts.value = topAccounts
                    Log.d("AccountViewModel", "Top 3 Accounts Updated: $topAccounts")
                } else {
                    Log.e("AccountViewModel", "Error fetching top 3 accounts: ${result.exceptionOrNull()?.message}")
                }
            }
        }
    }

    suspend fun fetchAccountsFinal() {
        accountRepository.getAccounts().collect {

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
                        observeAccounts()
                        observeTop3RecentAccounts()
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
     * @param accountName The Name of the account to delete.
     */
    fun deleteAccount(accountName: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading // Set loading state
            accountRepository.deleteAccount(accountName).collect { result ->
                _uiState.value = when {
                    result.isSuccess -> {
                        observeAccounts()
                        observeTop3RecentAccounts()
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
     * Updates the balance of a specified account.
     *
     * @param accountId The ID of the account to update.
     * @param amountDelta The change in amount (can be positive or negative).
     */
    fun updateAccountBalance(accountId: String, amountDelta: Long) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading // Set loading state
            try {
                accountRepository.updateAccountBalance(accountId, amountDelta)
                observeAccounts()
                observeTop3RecentAccounts()
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to update account balance")
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
