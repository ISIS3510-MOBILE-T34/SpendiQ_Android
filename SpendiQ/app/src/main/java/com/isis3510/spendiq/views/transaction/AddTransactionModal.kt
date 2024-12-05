package com.isis3510.spendiq.views.transaction

import android.app.DatePickerDialog
import android.util.Log
import android.util.LruCache
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
import kotlinx.coroutines.launch
import java.util.*
import com.isis3510.spendiq.model.data.Account
import com.isis3510.spendiq.model.singleton.LruCacheManager
import com.isis3510.spendiq.services.LocationService
import com.isis3510.spendiq.viewmodel.TransactionViewModel
import com.isis3510.spendiq.utils.DataStoreUtils
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionModal(
    accountViewModel: AccountViewModel,
    transactionViewModel: TransactionViewModel,
    onDismiss: () -> Unit,
    onTransactionAdded: () -> Unit
) {
    // Caching - J0FR
    val cache = LruCacheManager.cache  // Initialize LruCache for caching form data

    // Load values from cache or set defaults
    var amount by remember { mutableStateOf(cache.get("amount") as? String ?: "") }
    var transactionName by remember { mutableStateOf(cache.get("transactionName") as? String ?: "") }
    var selectedDate by remember { mutableStateOf(cache.get("selectedDate") as? Timestamp ?: Timestamp.now()) }
    var selectedTransactionType by remember { mutableStateOf(cache.get("transactionType") as? String ?: "Expense") }
    var expandedTransactionType by remember { mutableStateOf(false) }
    var selectedAccount by remember { mutableStateOf(cache.get("selectedAccount") as? Account) }
    var expandedAccountType by remember { mutableStateOf(false) }
    var showNoAccountsDialog by remember { mutableStateOf(false) }
    var isLocationEnabled by remember { mutableStateOf(false) }
    var location by remember { mutableStateOf<android.location.Location?>(null) }

    // Collect accounts from ViewModel
    val accounts by accountViewModel.accounts.collectAsState()

    // Initialize context, calendar, and location service
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val locationService = remember { LocationService(context) }
    val scope = rememberCoroutineScope()

    // Initialize DataStore state
    LaunchedEffect(Unit) {
        // DataStore - J0FR
        DataStoreUtils.getIncludeLocation(context).collectLatest { savedState ->
            isLocationEnabled = savedState // Retrieve saved "Include Location" state
        }
    }

    // Date picker dialog configuration
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = Timestamp(calendar.time)
            cache.put("selectedDate", selectedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Display a dialog if no accounts are available
    if (showNoAccountsDialog) {
        AlertDialog(
            onDismissRequest = { showNoAccountsDialog = false },
            title = { Text("No Accounts Available") },
            text = { Text("Please create an account in the Accounts section before adding a transaction.") },
            confirmButton = {
                Button(onClick = { showNoAccountsDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Check for available accounts when the component is launched
    LaunchedEffect(Unit) {
        if (accounts.isEmpty()) {
            showNoAccountsDialog = true
        }
    }

    // Modal layout and transaction form
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

            // Amount input field with digit-only filter
            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it.filter { char -> char.isDigit() }
                    cache.put("amount", amount)
                },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Transaction name input field
            OutlinedTextField(
                value = transactionName,
                onValueChange = {
                    transactionName = it

                    // Log the updated transactionName
                    Log.d("TransactionName", "Updated transactionName: $transactionName")

                    // Put the updated value into the cache and log it
                    cache.put("transactionName", transactionName)
                    Log.d("Cache", "Saved to cache: ${cache.get("transactionName")}")
                },
                label = { Text("Transaction Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Date picker button
            Button(onClick = { datePickerDialog.show() }) {
                Text("Select Date: ${selectedDate.toDate().toString().substring(0, 10)}", color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transaction type dropdown menu
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
                            cache.put("transactionType", selectedTransactionType)
                            expandedTransactionType = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Expense") },
                        onClick = {
                            selectedTransactionType = "Expense"
                            cache.put("transactionType", selectedTransactionType)
                            expandedTransactionType = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Account selection dropdown if accounts are available
            if (accounts.isNotEmpty()) {
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
                                    cache.put("selectedAccount", selectedAccount)
                                    expandedAccountType = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reset button
                Button(
                    onClick = {
                        // Clear form and cache
                        amount = ""
                        transactionName = ""
                        selectedDate = Timestamp.now()
                        selectedTransactionType = "Expense"
                        selectedAccount = null
                        cache.evictAll()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset", color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Location toggle row with switch
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
                            scope.launch {
                                // DataStore - j0fr
                                DataStoreUtils.setIncludeLocation(context, enabled)
                                if (enabled) {
                                    location = locationService.getCurrentLocation()
                                } else {
                                    location = null
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White, // White thumb (circle)
                            uncheckedThumbColor = Color.White, // White thumb when unchecked
                            checkedTrackColor = MaterialTheme.colorScheme.primary, // Primary color when checked
                            uncheckedTrackColor = Color.Gray // Gray track when unchecked
                        )
                    )
                }


                Spacer(modifier = Modifier.height(8.dp))

                // Submit button for adding transaction
                Button(
                    onClick = {
                        selectedAccount?.let { account ->
                            val transaction = Transaction(
                                id = "",
                                accountId = account.id,
                                transactionName = transactionName,
                                amount = amount.toLongOrNull() ?: 0L,
                                dateTime = selectedDate,
                                transactionType = selectedTransactionType,
                                location = if (isLocationEnabled && location != null) {
                                    Location(
                                        latitude = location!!.latitude,
                                        longitude = location!!.longitude
                                    )
                                } else null,
                                automatic = false,
                                amountAnomaly = false,
                                locationAnomaly = false
                            )
                            transactionViewModel.addTransactionWithAccountCheck(transaction)
                            onTransactionAdded()
                            onDismiss()
                        }
                    },
                    enabled = amount.isNotEmpty() &&
                            transactionName.isNotEmpty() &&
                            selectedAccount != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Transaction", color = Color.White)
                }
            } else {
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
