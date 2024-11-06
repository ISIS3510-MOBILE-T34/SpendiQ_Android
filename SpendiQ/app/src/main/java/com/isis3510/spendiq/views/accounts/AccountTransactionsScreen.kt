package com.isis3510.spendiq.views.accounts

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.isis3510.spendiq.model.data.Transaction
import com.isis3510.spendiq.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * AccountTransactionsScreen composable function
 *
 * Displays a list of transactions for a specific account. This screen allows users to view,
 * search, and filter transactions associated with an account. Users can also access detailed
 * views of individual transactions and see location information if available.
 *
 * Key Features:
 * - Transaction Listing: Displays transactions with details such as name, amount, type (income/expense),
 *   and indicators for any anomalies related to location or amount.
 * - Search Functionality: Allows users to filter transactions by name using a search bar.
 * - Date Grouping: Groups transactions by date for better organization and readability.
 * - Location Handling: Includes a feature to view the location associated with a transaction using
 *   the device's map application.
 *
 * UI Structure:
 * - Scaffold with a TopAppBar that includes the account name and a back navigation button.
 * - Search field for filtering transactions.
 * - A LazyColumn that displays transactions grouped by date, with the ability to click on each
 *   transaction for further details.
 *
 * Supporting Components:
 * - `TransactionItem`: A composable that represents an individual transaction and displays relevant
 *   details.
 * - `AnomalyIndicator`: Visual feedback indicating any anomalies in the transaction data.
 *
 * @param navController [NavController] to handle navigation actions within the app.
 * @param accountName The name of the account for which transactions are being displayed.
 * @param viewModel [TransactionViewModel] to manage and provide transaction data.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountTransactionsScreen(
    navController: NavController,
    accountName: String,
    viewModel: TransactionViewModel = viewModel()
) {
    // State for managing search query
    var searchQuery by remember { mutableStateOf("") }
    // State for collecting transactions and UI state
    val transactions by viewModel.transactions.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Load transactions for the specified account
    LaunchedEffect(accountName) {
        viewModel.fetchTransactions(accountName)
    }

    // Scaffold layout with TopAppBar
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(accountName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (uiState) {
            is TransactionViewModel.UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is TransactionViewModel.UiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${(uiState as TransactionViewModel.UiState.Error).message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Search Field for filtering transactions
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )

                    // Handle empty transaction list
                    if (transactions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay transacciones aún", style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        // LazyColumn to display transactions
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Filter transactions based on the search query
                            val filteredTransactions = transactions.filter {
                                it.transactionName.contains(searchQuery, ignoreCase = true)
                            }

                            // Group transactions by normalized date
                            val groupedTransactions = filteredTransactions.groupBy { normalizeDate(it.dateTime.toDate()) }
                            val sortedDates = groupedTransactions.keys.sortedDescending()

                            // Iterate through grouped dates to display transactions
                            sortedDates.forEach { date ->
                                val transactionsForDate = groupedTransactions[date] ?: return@forEach

                                // Display date header
                                item {
                                    Text(
                                        text = formatDate(date),
                                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.Gray
                                    )
                                }

                                // Display each transaction for the given date
                                items(transactionsForDate.sortedByDescending { it.dateTime }) { transaction ->
                                    TransactionItem(transaction, navController, accountName)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * TransactionItem composable function
 *
 * Displays the details of a single transaction, including its name, amount, type, and any associated
 * anomalies. Provides a clickable interface to navigate to detailed transaction information, and shows
 * location information if applicable.
 *
 * @param transaction [Transaction] the transaction data to display.
 * @param navController [NavController] to handle navigation to transaction details.
 * @param accountName The name of the account associated with the transaction.
 */
@Composable
fun TransactionItem(transaction: Transaction, navController: NavController, accountName: String) {
    val context = LocalContext.current

    // Determine background color based on anomalies
    val backgroundColor = when {
        transaction.locationAnomaly && transaction.amountAnomaly -> Color(0xFFFFE0E0) // Light red
        transaction.locationAnomaly || transaction.amountAnomaly -> Color(0xFFFFECB3) // Light orange
        else -> Color(0xFFF5F5F5) // Light grey
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                navController.navigate("transactionDetails/${transaction.accountId}/${transaction.id}")
            },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Show an icon based on transaction type
                Icon(
                    imageVector = if (transaction.transactionType == "Income") Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (transaction.transactionType == "Income") "Income" else "Expense",
                    tint = if (transaction.transactionType == "Income") Color(0xFF2196F3) else Color(0xFFFF0000)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(transaction.transactionName, fontWeight = FontWeight.Bold)
                    Text(
                        if (transaction.transactionType.equals("Income", ignoreCase = true)) "De" else "Para",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
                Text(
                    formatCurrency(transaction.amount.toDouble()),
                    color = if (transaction.transactionType == "Income") Color(0xFF2196F3) else Color(0xFFFF0000),
                    fontWeight = FontWeight.Bold
                )
            }

            // Anomaly indicators for location and amount
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnomalyIndicator(
                    label = "Ubicación",
                    isAnomaly = transaction.locationAnomaly
                )
                AnomalyIndicator(
                    label = "Monto",
                    isAnomaly = transaction.amountAnomaly
                )
            }

            // Location button to open maps if location is available
            if (transaction.location != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            val uri = Uri.parse("geo:${transaction.location.latitude},${transaction.location.longitude}?q=${transaction.location.latitude},${transaction.location.longitude}")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        }
                        .padding(top = 8.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Ver ubicación",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * AnomalyIndicator composable function
 *
 * Displays a visual indicator for transaction anomalies, such as location or amount anomalies.
 *
 * @param label String to display next to the anomaly indicator.
 * @param isAnomaly Boolean indicating whether the anomaly exists.
 */
@Composable
private fun AnomalyIndicator(
    label: String,
    isAnomaly: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (isAnomaly) Color(0xFFFF0000) else Color(0xFF4CAF50),
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

// Utility functions for date and currency formatting

private fun normalizeDate(date: Date): Date {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

private fun formatDate(date: Date): String {
    val calendar = Calendar.getInstance()
    calendar.time = date

    val today = Calendar.getInstance()
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)
    today.set(Calendar.MILLISECOND, 0)

    val yesterday = Calendar.getInstance()
    yesterday.add(Calendar.DAY_OF_YEAR, -1)
    yesterday.set(Calendar.HOUR_OF_DAY, 0)
    yesterday.set(Calendar.MINUTE, 0)
    yesterday.set(Calendar.SECOND, 0)
    yesterday.set(Calendar.MILLISECOND, 0)

    return when {
        calendar.time == today.time -> "Hoy"
        calendar.time == yesterday.time -> "Ayer"
        else -> {
            val formatter = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
            formatter.format(date)
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    return format.format(amount)
}
