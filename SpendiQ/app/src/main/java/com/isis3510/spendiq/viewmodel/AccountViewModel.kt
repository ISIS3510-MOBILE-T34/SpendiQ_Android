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

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }
}
