package com.isis3510.spendiq.views.accounts

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountTransactionsScreen(navController: NavController, accountName: String) {
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(accountName) {
        transactions = fetchTransactions(accountName)
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
                    val groupedTransactions = filteredTransactions.groupBy { it.date }

                    groupedTransactions.forEach { (date, transactionsForDate) ->
                        item {
                            Text(
                                text = formatDate(date),
                                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Gray
                            )
                        }

                        items(transactionsForDate) { transaction ->
                            TransactionItem(transaction)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (transaction.amount > 0) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (transaction.amount > 0) "Income" else "Expense",
                tint = if (transaction.amount > 0) Color(0xFF2196F3) else Color(0xFFFF0000)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.transactionName, fontWeight = FontWeight.Bold)
                Text(transaction.description, color = Color.Gray, fontSize = 14.sp)
                if (transaction.location != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            val uri = Uri.parse("geo:${transaction.location.latitude},${transaction.location.longitude}?q=${transaction.location.latitude},${transaction.location.longitude}")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        }
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
            Text(
                formatCurrency(transaction.amount),
                color = if (transaction.amount > 0) Color(0xFF2196F3) else Color(0xFFFF0000),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

data class Transaction(
    val transactionName: String,
    val description: String,
    val amount: Double,
    val date: Date,
    val location: Location?
)

data class Location(
    val latitude: Double,
    val longitude: Double
)

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

        if (transactionsSnapshot.isEmpty) {
            Log.w("Firebase", "No transactions found for account ID: $accountId")
            return emptyList()
        }

        return transactionsSnapshot.documents.mapNotNull { doc ->
            try {
                Log.d("Firebase", "Processing document: ${doc.id}")
                Log.d("Firebase", "Document data: ${doc.data}")

                val transactionName = doc.getString("transactionName") ?: return@mapNotNull null

                // Handle amount as either Long or Double
                val amount = when (val amountValue = doc.get("amount")) {
                    is Long -> amountValue.toDouble()
                    is Double -> amountValue
                    else -> return@mapNotNull null
                }

                // Handle Timestamp
                val timestamp = doc.get("dateTime") as? Timestamp
                val date = timestamp?.toDate()
                if (date == null) {
                    Log.e("Firebase", "Invalid date format for document: ${doc.id}")
                    return@mapNotNull null
                }

                val transactionType = doc.getString("transactionType") ?: return@mapNotNull null

                // Handle location
                val locationMap = doc.get("location") as? Map<String, Any>
                val location = locationMap?.let { map ->
                    val latitude = map["latitude"] as? Double
                    val longitude = map["longitude"] as? Double
                    if (latitude != null && longitude != null) {
                        Location(latitude, longitude)
                    } else null
                }

                Log.d("Firebase", "Successfully parsed transaction: $transactionName")

                Transaction(
                    transactionName = transactionName,
                    description = if (transactionType.equals("Income", ignoreCase = true)) "De" else "Para",
                    amount = if (transactionType.equals("Income", ignoreCase = true)) amount else -amount,
                    date = date,
                    location = location
                )
            } catch (e: Exception) {
                Log.e("Firebase", "Error parsing transaction document: ${doc.id}", e)
                null
            }
        }.sortedByDescending { it.date }
    } catch (e: Exception) {
        Log.e("Firebase", "Error fetching transactions", e)
        return emptyList()
    }
}

fun formatDate(date: Date): String {
    val calendar = Calendar.getInstance()
    calendar.time = date

    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance()
    yesterday.add(Calendar.DAY_OF_YEAR, -1)

    return when {
        calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Hoy"
        calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "Ayer"
        else -> {
            val formatter = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
            formatter.format(date)
        }
    }
}

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    return format.format(amount)
}