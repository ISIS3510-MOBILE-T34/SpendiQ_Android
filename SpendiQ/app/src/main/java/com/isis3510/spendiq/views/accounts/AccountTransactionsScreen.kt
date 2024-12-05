package com.isis3510.spendiq.views.accounts

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
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
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.isis3510.spendiq.model.data.Transaction
import com.isis3510.spendiq.model.singleton.SearchBarCacheManager
import com.isis3510.spendiq.viewmodel.NetworkViewModel
import com.isis3510.spendiq.viewmodel.TransactionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountTransactionsScreen(
    navController: NavController,
    accountName: String,
    viewModel: TransactionViewModel = viewModel(),
    networkViewModel: NetworkViewModel = viewModel()
) {
    // Caching - J0FR
    var searchQuery by remember { mutableStateOf(SearchBarCacheManager.getQuery()) }
    val transactions by viewModel.transactions.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var isNavigating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val isNetworkConnected by networkViewModel.isConnected.collectAsState(initial = true)
    var isRefreshing by remember { mutableStateOf(false) }

    // Voice recognition launcher - J0FR
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val recognizedText =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!recognizedText.isNullOrBlank()) {
                searchQuery = recognizedText
            }
        }
    }

    val speechToTextIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something to search")
    }

    // Fetch transactions on launch
    LaunchedEffect(accountName, isNetworkConnected) {
        if (isNetworkConnected) {
            viewModel.fetchTransactions(accountName) // Fetch transactions if online
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(accountName) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!isNavigating) {
                            isNavigating = true
                            coroutineScope.launch {
                                navController.popBackStack()
                                delay(300)
                                isNavigating = false
                            }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (!isNetworkConnected) {
            // Show message when offline
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "You need an active internet connection to view transactions.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
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
                            text = "Error: You need internet connection in order to use this feature",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    SwipeRefresh(
                        state = rememberSwipeRefreshState(isRefreshing),
                        onRefresh = {
                            // Coreroutine Main Dispatcher - J0FR
                            coroutineScope.launch(Dispatchers.Main) {
                                isRefreshing = true
                                viewModel.fetchTransactions(accountName)
                                delay(1000)
                                isRefreshing = false
                            }
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                    SearchBarCacheManager.saveQuery(searchQuery) // Cache the search query
                                },
                                label = { Text("Search") },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = "Search")
                                },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        speechRecognizerLauncher.launch(speechToTextIntent)
                                    }) {
                                        Icon(Icons.Default.Face, contentDescription = "Voice Search")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )

                            if (transactions.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No transactions yet", style = MaterialTheme.typography.bodyLarge)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    val filteredTransactions = transactions.filter {
                                        it.transactionName.contains(searchQuery, ignoreCase = true)
                                    }
                                    val groupedTransactions = filteredTransactions.groupBy { normalizeDate(it.dateTime.toDate()) }
                                    val sortedDates = groupedTransactions.keys.sortedDescending()

                                    sortedDates.forEach { date ->
                                        val transactionsForDate =
                                            groupedTransactions[date] ?: return@forEach

                                        item {
                                            Text(
                                                text = formatDate(date),
                                                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                                                style = MaterialTheme.typography.labelLarge,
                                                color = Color.Gray
                                            )
                                        }

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
    }
}

@Composable
fun TransactionItem(transaction: Transaction, navController: NavController, accountName: String) {
    val context = LocalContext.current
    val backgroundColor = when {
        transaction.locationAnomaly && transaction.amountAnomaly -> Color(0xFFFFE0E0)
        transaction.locationAnomaly || transaction.amountAnomaly -> Color(0xFFFFECB3)
        else -> Color(0xFFF5F5F5)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                navController.navigate("transactionDetails/${transaction.accountId}/${transaction.id}")
            },
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
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
                        if (transaction.transactionType.equals("Income", ignoreCase = true)) "From" else "To",
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnomalyIndicator(label = "Location", isAnomaly = transaction.locationAnomaly)
                AnomalyIndicator(label = "Amount", isAnomaly = transaction.amountAnomaly)
            }

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
                        "View location",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AnomalyIndicator(label: String, isAnomaly: Boolean) {
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
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}

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
    val formatter = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    return formatter.format(date)
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
    return format.format(amount)
}
