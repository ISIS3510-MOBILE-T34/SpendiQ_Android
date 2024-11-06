package com.isis3510.spendiq.views.accounts

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.maps.android.compose.*
import com.isis3510.spendiq.model.data.Location
import com.isis3510.spendiq.model.data.Transaction
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.TransactionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

/**
 * TransactionDetailsScreen composable function
 *
 * Displays the details of a specific transaction and allows the user to edit or delete it. The screen
 * includes fields for transaction name, amount, type, date, and location (optional). A Google Map
 * view is provided for users to select or update the transaction location, enhancing geolocation data
 * for financial tracking.
 *
 * Key Features:
 * - Transaction Editing: Allows modification of transaction details, including the name, amount,
 *   type (income/expense), date, and location.
 * - Location Management:
 *   - Optional location tracking with Google Maps integration.
 *   - Allows enabling or disabling of location tracking.
 *   - Interactive map to select or change location.
 * - Delete Confirmation: A dialog to confirm deletion of the transaction.
 * - Snackbar Feedback: Provides success or error messages for save and delete actions.
 *
 * UI Structure:
 * - Scaffold with a TopAppBar for navigation and actions.
 * - Scrollable Column layout containing:
 *   - Editable fields for transaction details.
 *   - Location options and map view.
 *   - Save button to apply changes and delete button in the app bar for deletion.
 *
 * Supporting Components:
 * - `DatePickerDialog` for selecting a transaction date.
 * - `AlertDialog` for confirming transaction deletion.
 * - Google Maps integration to view and modify the location.
 *
 * @param navController [NavController] to handle navigation.
 * @param accountViewModel [AccountViewModel] to manage account-related data.
 * @param transactionViewModel [TransactionViewModel] to handle transaction data.
 * @param accountId The ID of the account associated with the transaction.
 * @param transactionId The ID of the transaction being viewed or edited.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsScreen(
    navController: NavController,
    accountViewModel: AccountViewModel,
    transactionViewModel: TransactionViewModel,
    accountId: String,
    transactionId: String
) {
    // Context and CoroutineScope
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // UI States and Variables
    val transaction by transactionViewModel.selectedTransaction.collectAsState()
    val uiState by transactionViewModel.uiState.collectAsState()
    var transactionName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Timestamp?>(null) }
    var isLocationEnabled by remember { mutableStateOf(false) }
    var location by remember { mutableStateOf<Location?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var expandedTransactionType by remember { mutableStateOf(false) }

    // Google Map Configuration
    val defaultLocation = LatLng(4.6097100, -74.0817500) // Default to Bogota
    var mapPosition by remember { mutableStateOf(defaultLocation) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
    }

    var isNavigating by remember { mutableStateOf(false) }

    // Load Transaction Data on Screen Start
    LaunchedEffect(Unit) {
        transactionViewModel.getTransaction(accountId, transactionId)
    }

    // Handle UI State
    LaunchedEffect(uiState) {
        if (uiState is TransactionViewModel.UiState.Error) {
            snackbarHostState.showSnackbar((uiState as TransactionViewModel.UiState.Error).message)
        }
    }

    // Update Local State when Transaction Loads
    LaunchedEffect(transaction) {
        transaction?.let {
            transactionName = it.transactionName
            amount = it.amount.toString()
            transactionType = it.transactionType
            selectedDate = it.dateTime
            location = it.location
            isLocationEnabled = it.location != null
            location?.let { loc ->
                mapPosition = LatLng(loc.latitude, loc.longitude)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                    LatLng(loc.latitude, loc.longitude), 15f
                )
            }
        }
    }

    // DatePickerDialog for Selecting Transaction Date
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = Timestamp(calendar.time)
        },
        Calendar.getInstance().get(Calendar.YEAR),
        Calendar.getInstance().get(Calendar.MONTH),
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    )

    // Main Scaffold Layout
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Transaction") },
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDeleteConfirmation = true },
                        enabled = transaction != null
                    ) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (uiState is TransactionViewModel.UiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (transaction == null) {
                Text(
                    text = "Transaction not found",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                // Transaction Name Input
                OutlinedTextField(
                    value = transactionName,
                    onValueChange = { transactionName = it },
                    label = { Text("Transaction Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Input Field
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it.filter { char -> char.isDigit() || char == '.' }
                    },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Transaction Type Dropdown Menu
                ExposedDropdownMenuBox(
                    expanded = expandedTransactionType,
                    onExpandedChange = { expandedTransactionType = !expandedTransactionType }
                ) {
                    TextField(
                        value = transactionType,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedTransactionType) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTransactionType,
                        onDismissRequest = { expandedTransactionType = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Income") },
                            onClick = {
                                transactionType = "Income"
                                expandedTransactionType = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Expense") },
                            onClick = {
                                transactionType = "Expense"
                                expandedTransactionType = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date Selector Button
                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedDate?.toDate()?.toString() ?: "Select Date")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Location Toggle with Map Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = if (isLocationEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Include Location",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = isLocationEnabled,
                        onCheckedChange = { enabled ->
                            isLocationEnabled = enabled
                            if (enabled && location == null) {
                                location = Location(defaultLocation.latitude, defaultLocation.longitude)
                                mapPosition = defaultLocation
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google Map for Selecting Location
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = false),
                        onMapClick = { latLng ->
                            if (isLocationEnabled) {
                                mapPosition = latLng
                                location = Location(latLng.latitude, latLng.longitude)
                            }
                        }
                    ) {
                        if (location != null) {
                            Marker(
                                state = MarkerState(position = mapPosition),
                                title = "Transaction Location"
                            )
                        }
                    }

                    // Overlay if Location Disabled
                    if (!isLocationEnabled) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Location Disabled",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save Changes Button
                Button(
                    onClick = {
                        transaction?.let { currentTransaction ->
                            val updatedTransaction = Transaction(
                                id = currentTransaction.id,
                                accountId = currentTransaction.accountId,
                                transactionName = transactionName,
                                amount = (amount.toDoubleOrNull() ?: 0.0).toLong(),
                                dateTime = selectedDate ?: Timestamp.now(),
                                transactionType = transactionType,
                                location = if (isLocationEnabled) location else null
                            )
                            coroutineScope.launch {
                                try {
                                    transactionViewModel.updateTransaction(
                                        currentTransaction.accountId,
                                        currentTransaction,
                                        updatedTransaction
                                    )
                                    snackbarHostState.showSnackbar("Transaction updated successfully")
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Failed to update transaction: ${e.message}")
                                    Log.e("TransactionUpdate", "Error updating transaction", e)
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = transaction != null
                ) {
                    Text("Save Changes")
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Delete Transaction") },
                text = { Text("Are you sure you want to delete this transaction? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            transaction?.let {
                                coroutineScope.launch {
                                    try {
                                        transactionViewModel.deleteTransaction(accountId, it)
                                        snackbarHostState.showSnackbar("Transaction deleted successfully")
                                        navController.popBackStack()
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Failed to delete transaction: ${e.message}")
                                        Log.e("TransactionDelete", "Error deleting transaction", e)
                                    }
                                }
                                showDeleteConfirmation = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
