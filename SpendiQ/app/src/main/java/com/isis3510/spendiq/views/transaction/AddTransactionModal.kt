package com.isis3510.spendiq.views.transaction

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.isis3510.spendiq.model.data.Transaction
import com.isis3510.spendiq.model.data.Location
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.google.firebase.Timestamp
import com.isis3510.spendiq.services.LocationService
import kotlinx.coroutines.launch
import java.util.*
import com.isis3510.spendiq.model.data.Account

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionModal(
    accountViewModel: AccountViewModel,
    onDismiss: () -> Unit,
    onTransactionAdded: () -> Unit
) {
    // State variables for form fields
    var amount by remember { mutableStateOf("") }
    var transactionName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Timestamp.now()) }
    var selectedTransactionType by remember { mutableStateOf("Expense") }
    var expandedTransactionType by remember { mutableStateOf(false) }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var expandedAccountType by remember { mutableStateOf(false) }
    var showNoAccountsDialog by remember { mutableStateOf(false) }
    var isLocationEnabled by remember { mutableStateOf(false) }
    var location by remember { mutableStateOf<android.location.Location?>(null) }

    // Collect accounts from ViewModel
    val accounts by accountViewModel.accounts.collectAsState()

    // Context and services
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val locationService = remember { LocationService(context) }
    val scope = rememberCoroutineScope()

    // Initialize date picker dialog
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = Timestamp(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // No accounts dialog
    if (showNoAccountsDialog) {
        AlertDialog(
            onDismissRequest = { showNoAccountsDialog = false },
            title = { Text("No Accounts Available") },
            text = { Text("Please create an account first in the Accounts section before making a transaction.") },
            confirmButton = {
                Button(onClick = { showNoAccountsDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Check for accounts availability
    LaunchedEffect(Unit) {
        if (accounts.isEmpty()) {
            showNoAccountsDialog = true
        }
    }

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

            // Amount field
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { char -> char.isDigit() } },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Transaction name field
            OutlinedTextField(
                value = transactionName,
                onValueChange = { transactionName = it },
                label = { Text("Transaction Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Date selector
            Button(onClick = { datePickerDialog.show() }) {
                Text("Select Date: ${selectedDate.toDate().toString().substring(0, 10)}")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transaction type dropdown
            ExposedDropdownMenuBox(
                expanded = expandedTransactionType,
                onExpandedChange = { expandedTransactionType = !expandedTransactionType }
            ) {
                TextField(
                    value = selectedTransactionType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Transaction Type") },
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

            if (accounts.isNotEmpty()) {
                // Account selection dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedAccountType,
                    onExpandedChange = { expandedAccountType = !expandedAccountType }
                ) {
                    TextField(
                        value = selectedAccount?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccountType) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAccountType,
                        onDismissRequest = { expandedAccountType = false }
                    ) {
                        accounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text(account.name) },
                                onClick = {
                                    selectedAccount = account
                                    expandedAccountType = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Location toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = if (isLocationEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Include Location")
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = isLocationEnabled,
                        onCheckedChange = { enabled ->
                            isLocationEnabled = enabled
                            if (enabled) {
                                scope.launch {
                                    location = locationService.getCurrentLocation()
                                }
                            } else {
                                location = null
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit button
                Button(
                    onClick = {
                        selectedAccount?.let { account ->
                            val transaction = Transaction(
                                id = "", // This will be set by Firestore
                                accountId = account.id, // Using account ID instead of name
                                transactionName = transactionName,
                                amount = amount.toLongOrNull() ?: 0L,
                                dateTime = selectedDate,
                                transactionType = selectedTransactionType,
                                location = if (isLocationEnabled && location != null) {
                                    Location(
                                        latitude = location!!.latitude,
                                        longitude = location!!.longitude
                                    )
                                } else null
                            )
                            accountViewModel.addTransactionWithAccountCheck(transaction)
                            onTransactionAdded()
                            onDismiss()
                        }
                    },
                    enabled = amount.isNotEmpty() &&
                            transactionName.isNotEmpty() &&
                            selectedAccount != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Transaction")
                }
            } else {
                // No accounts message
                Text(
                    "No accounts available. Please create an account first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}