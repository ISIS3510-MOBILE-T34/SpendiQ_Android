package com.isis3510.spendiq.views.accounts

import android.content.Intent
import android.net.Uri
import android.util.Log
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
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.spendiq.model.data.Transaction
import com.isis3510.spendiq.model.data.Location
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountTransactionsScreen(navController: NavController, accountName: String) {
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(accountName) {
        isLoading = true
        try {
            transactions = fetchTransactions(accountName)
        } catch (e: Exception) {
            error = e.message
            Log.e("AccountTransactions", "Error fetching transactions", e)
        } finally {
            isLoading = false
        }
    }

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
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: $error",
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
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )

                    if (transactions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay transacciones aún", style = MaterialTheme.typography.bodyLarge)
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
                                val transactionsForDate = groupedTransactions[date] ?: return@forEach

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
                Icon(
                    imageVector = if (transaction.transactionType == "Income") Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (transaction.amount > 0) "Income" else "Expense",
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
                    color = if (transaction.amount > 0) Color(0xFF2196F3) else Color(0xFFFF0000),
                    fontWeight = FontWeight.Bold
                )
            }

            // Anomaly indicators
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

            // Location button (if available)
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

private fun normalizeDate(date: Date): Date {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

suspend fun fetchTransactions(accountName: String): List<Transaction> {
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()

    try {
        Log.d("Firebase", "Fetching accounts for user: $userId")
        val accountSnapshot = firestore.collection("accounts")
            .whereEqualTo("user_id", userId)
            .whereEqualTo("name", accountName)
            .get()
            .await()

        if (accountSnapshot.documents.isEmpty()) {
            Log.w("Firebase", "No account found with name: $accountName for user: $userId")
            return emptyList()
        }

        val accountId = accountSnapshot.documents[0].id
        Log.d("Firebase", "Found account ID: $accountId")

        val transactionsSnapshot = firestore.collection("accounts")
            .document(accountId)
            .collection("transactions")
            .get()
            .await()

        return transactionsSnapshot.documents.mapNotNull { doc ->
            try {
                val transactionName = doc.getString("transactionName") ?: return@mapNotNull null
                val amount = doc.getLong("amount") ?: return@mapNotNull null
                val timestamp = doc.getTimestamp("dateTime") ?: return@mapNotNull null
                val transactionType = doc.getString("transactionType") ?: return@mapNotNull null
                val locationMap = doc.get("location") as? Map<String, Any>
                val amountAnomaly = doc.getBoolean("amountAnomaly") ?: false
                val locationAnomaly = doc.getBoolean("locationAnomaly") ?: false

                val location = locationMap?.let {
                    val latitude = it["latitude"] as? Double
                    val longitude = it["longitude"] as? Double
                    if (latitude != null && longitude != null) {
                        Location(latitude, longitude)
                    } else null
                }

                Transaction(
                    id = doc.id,
                    accountId = accountId,
                    transactionName = transactionName,
                    amount = amount,
                    dateTime = timestamp,
                    transactionType = transactionType,
                    location = location,
                    amountAnomaly = amountAnomaly,
                    locationAnomaly = locationAnomaly
                )
            } catch (e: Exception) {
                Log.e("Firebase", "Error parsing transaction document", e)
                null
            }
        }.sortedByDescending { it.dateTime.toDate() }
    } catch (e: Exception) {
        Log.e("Firebase", "Error fetching transactions", e)
        throw e
    }
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