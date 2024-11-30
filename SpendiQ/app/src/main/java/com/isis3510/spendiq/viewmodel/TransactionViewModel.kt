package com.isis3510.spendiq.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.spendiq.model.cache.MovementsCache
import com.isis3510.spendiq.model.data.DailyTransaction
import com.isis3510.spendiq.model.data.Transaction
import com.isis3510.spendiq.model.iterator.MonthlyTransactionIterator
import com.isis3510.spendiq.model.iterator.TransactionIterator
import com.isis3510.spendiq.model.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar

/**
 * TransactionViewModel class
 *
 * This ViewModel manages the user's financial transactions, providing functionalities to fetch, add, update,
 * and delete transactions from a repository. It uses Kotlin Coroutines to handle asynchronous operations
 * with the data layer, specifically interfacing with Firestore or any other data source through the
 * TransactionRepository.
 *
 * Key Features:
 * - Reactive Data Management: Exposes transaction data and UI states as StateFlow objects for the UI to observe.
 * - CRUD Operations: Provides functions to create, read, update, and delete transactions.
 * - Error Handling: Manages error states and logs errors for debugging.
 * - Income and Expense Calculation: Offers functionality to compute total income and expenses from the transaction list.
 *
 * State Management:
 * - `_transactions`: MutableStateFlow containing a list of transactions.
 * - `transactions`: Public immutable StateFlow for observing transactions.
 * - `_selectedTransaction`: MutableStateFlow for managing the currently selected transaction.
 * - `selectedTransaction`: Public immutable StateFlow for observing the selected transaction.
 * - `_uiState`: MutableStateFlow for tracking the current UI state (Idle, Loading, Success, or Error).
 * - `uiState`: Public immutable StateFlow for observing UI state changes.
 *
 * Initialization:
 * - On instantiation, the ViewModel initializes the transaction repository and prepares to fetch transactions.
 *
 * Transaction Management Functions:
 * - `fetchTransactions(accountName: String)`: Fetches transactions for a specified account and updates the UI state.
 * - `getTransaction(accountId: String, transactionId: String)`: Retrieves a specific transaction and updates the selected transaction state.
 * - `addTransactionWithAccountCheck(transaction: Transaction)`: Adds a new transaction and refreshes the transaction list.
 * - `updateTransaction(accountId: String, oldTransaction: Transaction, newTransaction: Transaction)`: Updates an existing transaction.
 * - `deleteTransaction(accountId: String, transaction: Transaction)`: Deletes a transaction and refreshes the transaction list.
 * - `getIncomeAndExpenses()`: Calculates and returns the total income and expenses from the current transaction list.
 * - `fetchAllTransactions()`: Fetches all transactions for the authenticated user and updates the transaction list.
 *
 * UI State Management:
 * - Sealed class `UiState` defines the possible UI states (Idle, Loading, Success, and Error) for better state handling.
 *
 * Error Handling:
 * - Logs errors encountered during transaction operations for easier debugging.
 */
class TransactionViewModel(
    private val transactionRepository: TransactionRepository = TransactionRepository(),
    private val movementsCache: MovementsCache = MovementsCache()
) : ViewModel() {
    // MutableStateFlow for transactions
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())

    // Public immutable StateFlow for transactions
    val transactions: StateFlow<List<Transaction>> = _transactions

    // MutableStateFlow for the selected transaction
    private val _selectedTransaction = MutableStateFlow<Transaction?>(null)

    // Public immutable StateFlow for the selected transaction
    val selectedTransaction: StateFlow<Transaction?> = _selectedTransaction

    // MutableStateFlow for the UI state
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)

    // Public immutable StateFlow for the UI state
    val uiState: StateFlow<UiState> = _uiState

    // MutableStateFlow para gastos mensuales
    private val _monthlyExpenses = MutableStateFlow<Pair<Long, Long>>(0L to 0L)

    // Public immutable StateFlow for the UI state
    val monthlyExpenses: StateFlow<Pair<Long, Long>> = _monthlyExpenses

    // MutableStateFlow para ingresos y gastos de los últimos 30 días
    private val _incomeAndExpensesLast30Days = MutableStateFlow<List<DailyTransaction>>(emptyList())
    val incomeAndExpensesLast30Days: StateFlow<List<DailyTransaction>> = _incomeAndExpensesLast30Days

    // MutableStateFlow para ingresos y gastos totales
    private val _totalIncomeAndExpenses = MutableStateFlow<Pair<Long, Long>>(0L to 0L)
    val totalIncomeAndExpenses: StateFlow<Pair<Long, Long>> = _totalIncomeAndExpenses

    // Fetch transactions for a specific account
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

    suspend fun getTransactions(accountName: String): List<Transaction> {
        // Create an empty list to hold the transactions
        var transactionsList = emptyList<Transaction>()

        // Fetch transactions directly from the repository without updating the UI state
        viewModelScope.launch {
            val result = transactionRepository.getTransactions(accountName).first() // Get the result from the flow

            // If the result is successful, assign the value to transactionsList
            if (result.isSuccess) {
                transactionsList = result.getOrNull() ?: emptyList()
            } else {
                // Handle failure case (log or return empty list)
                Log.e("TransactionViewModel", "Failed to fetch transactions for account $accountName: ${result.exceptionOrNull()?.message}")
            }
        }

        return transactionsList
    }

    // Get details for a specific transaction
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

    // Add a transaction with a check on account
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

    // Update a transaction
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

    // Delete a transaction
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

    // Calculate total income and expenses
    fun calculateIncomeAndExpenses(isNetworkAvailable: Boolean) {
        viewModelScope.launch {
            if (isNetworkAvailable) {
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

                // Guardar en caché
                movementsCache.saveMovements("totalIncomeAndExpenses", listOf(
                    DailyTransaction("Total Income", totalIncome.toDouble()),
                    DailyTransaction("Total Expenses", totalExpenses.toDouble())
                ))

                // Actualizar el StateFlow con los nuevos valores
                _totalIncomeAndExpenses.value = Pair(totalIncome, totalExpenses)
            } else {
                // Recuperar del caché
                val cachedTransactions = movementsCache.getMovements("totalIncomeAndExpenses") ?: emptyList()
                val cachedIncome = cachedTransactions.find { it.day == "Total Income" }?.amount?.toLong() ?: 0L
                val cachedExpenses = cachedTransactions.find { it.day == "Total Expenses" }?.amount?.toLong() ?: 0L

                // Actualizar el StateFlow con los valores del caché
                _totalIncomeAndExpenses.value = Pair(cachedIncome, cachedExpenses)
            }
        }
    }
    //Calculate total income and expenses in te last 30 days
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchIncomeAndExpensesLast30Days(isNetworkAvailable: Boolean) {
        viewModelScope.launch {
            val cacheKey = "last30days"

            if (isNetworkAvailable) {
                // Obtener los datos de la red
                val iterator = TransactionIterator(transactions.value)
                while (iterator.hasNext()) {
                    iterator.next()
                }
                val dailyTransactions = iterator.getDailyTransactions()
                movementsCache.saveMovements(cacheKey, dailyTransactions) // Guardar en caché
                _incomeAndExpensesLast30Days.value = dailyTransactions // Actualizar el StateFlow
                Log.d("TransactionViewmodel", "Cached Last 30 days: $dailyTransactions")
            } else {
                // Obtener los datos del caché
                val cachedTransactions = movementsCache.getMovements(cacheKey) ?: emptyList()
                Log.d("TransactionViewmodel", "Recovered Last 30 days: $cachedTransactions")
                _incomeAndExpensesLast30Days.value = cachedTransactions // Actualizar el StateFlow
            }
        }
    }


    // Fetch all transactions
    fun fetchAllTransactions() {
        Log.d("TransactionViewModel", "fetchAllTransactions called")
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            transactionRepository.getAllTransactions().collect { result ->
                _uiState.value = when {
                    result.isSuccess -> {
                        val transactionsList = result.getOrNull() ?: emptyList()
                        _transactions.value = transactionsList

                        Log.d("TransactionViewModel", "Transactions obtained: $transactionsList")

                        UiState.Success
                    }
                    result.isFailure -> {
                        Log.e("TransactionViewModel", "Error fetching transactions: ${result.exceptionOrNull()?.message}")
                        UiState.Error(result.exceptionOrNull()?.message ?: "Failed to fetch transactions")
                    }
                    else -> UiState.Error("Unexpected error")
                }
            }
        }
    }

    // Clear the selected transaction
    fun clearSelectedTransaction() {
        _selectedTransaction.value = null
    }

    // Función para obtener y almacenar gastos mensuales en caché
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchAndCacheMonthlyExpenses(isNetworkAvailable: Boolean) {
        val cacheKey = "monthlyExpenses"

        // Verificar si hay conexión a Internet
        if (isNetworkAvailable) {
            // Obtener los gastos del mes actual y anterior
            val (currentMonth, previousMonth) = getCurrentAndPreviousMonthExpenses()

            // Guardar los gastos en caché
            movementsCache.saveMovements(cacheKey, listOf(
                DailyTransaction("Current Month", currentMonth.toDouble()),
                DailyTransaction("Previous Month", previousMonth.toDouble())
            ))

            // Actualizar el StateFlow con los nuevos valores
            _monthlyExpenses.value = Pair(currentMonth, previousMonth)
        } else {
            // Si no hay conexión, obtener los gastos del caché
            val cachedExpenses = movementsCache.getMovements(cacheKey) ?: emptyList()
            val currentMonth = cachedExpenses.find { it.day == "Current Month" }?.amount?.toLong() ?: 0L
            val previousMonth = cachedExpenses.find { it.day == "Previous Month" }?.amount?.toLong() ?: 0L

            // Si ambos son 0, asignar 0.0 a los gastos mensuales
            if (cachedExpenses.isEmpty()) {
                _monthlyExpenses.value = Pair(0L, 0L) // Asignar 0.0 si el caché está vacío
            } else {
                // Actualizar el StateFlow con los valores del caché
                _monthlyExpenses.value = Pair(currentMonth, previousMonth)
            }
        }
    }

    // Función para obtener gastos del mes actual y anterior
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentAndPreviousMonthExpenses(): Pair<Long, Long> {
        val iterator = MonthlyTransactionIterator(transactions.value)
        return iterator.getMonthlyExpenses()
    }

    // Sealed class for UI state management
    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }
}
