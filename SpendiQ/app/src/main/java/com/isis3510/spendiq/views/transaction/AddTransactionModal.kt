package com.isis3510.spendiq.views.transaction

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionModal(
    onDismiss: () -> Unit,
    onTransactionAdded: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var transactionName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Date()) }
    var selectedTransactionType by remember { mutableStateOf("Expense") }
    var expandedTransactionType by remember { mutableStateOf(false) }
    var selectedAccountType by remember { mutableStateOf("Nu") }
    var expandedAccountType by remember { mutableStateOf(false) }

    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.time
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Add Transaction", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { char -> char.isDigit() } },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = transactionName,
                onValueChange = { transactionName = it },
                label = { Text("Transaction Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { datePickerDialog.show() }) {
                Text("Select Date: ${selectedDate.toString().substring(0, 10)}")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transaction Type Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedTransactionType,
                onExpandedChange = { expandedTransactionType = !expandedTransactionType }
            ) {
                TextField(
                    value = selectedTransactionType,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTransactionType) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedTransactionType,
                    onDismissRequest = { expandedTransactionType = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Income") },
                        onClick = {
                            selectedTransactionType = "Income"
                            expandedTransactionType = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Expense") },
                        onClick = {
                            selectedTransactionType = "Expense"
                            expandedTransactionType = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expandedAccountType,
                onExpandedChange = { expandedAccountType = !expandedAccountType }
            ) {
                TextField(
                    value = selectedAccountType,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccountType) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedAccountType,
                    onDismissRequest = { expandedAccountType = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Nu") },
                        onClick = {
                            selectedAccountType = "Nu"
                            expandedAccountType = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        if (userId != null) {
                            addTransaction(
                                userId,
                                amount.toLongOrNull() ?: 0L,
                                transactionName,
                                selectedDate.time,
                                selectedTransactionType,
                                selectedAccountType,
                                firestore,
                                onTransactionAdded,
                                onDismiss
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Transaction")
            }
        }
    }
}



private suspend fun addTransaction(
    userId: String,
    amount: Long,
    transactionName: String,
    dateTime: Long,
    transactionType: String,
    accountType: String,
    firestore: FirebaseFirestore,
    onTransactionAdded: () -> Unit,
    onDismiss: () -> Unit
) {
    try {
        val accountSnapshot = firestore.collection("accounts")
            .whereEqualTo("name", accountType)
            .whereEqualTo("user_id", userId)
            .get()
            .await()

        val accountId = if (accountSnapshot.documents.isNotEmpty()) {
            accountSnapshot.documents[0].id
        } else {
            val newAccount = hashMapOf(
                "amount" to 0L,
                "name" to accountType,
                "user_id" to userId
            )
            firestore.collection("accounts").add(newAccount).await().id
        }

        val transaction = hashMapOf(
            "accountID" to accountId,
            "amount" to amount,
            "dateTime" to dateTime,
            "transactionName" to transactionName,
            "transactionType" to transactionType
        )
        firestore.collection("accounts").document(accountId)
            .collection("transactions")
            .add(transaction)
            .await()

        val accountDoc = firestore.collection("accounts").document(accountId)
        firestore.runTransaction { transaction ->
            val account = transaction.get(accountDoc)
            val currentBalance = account.getLong("amount") ?: 0L
            val newBalance = if (transactionType == "Income") currentBalance + amount else currentBalance - amount
            transaction.update(accountDoc, "amount", newBalance)
        }.await()

        onTransactionAdded()
        onDismiss()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
