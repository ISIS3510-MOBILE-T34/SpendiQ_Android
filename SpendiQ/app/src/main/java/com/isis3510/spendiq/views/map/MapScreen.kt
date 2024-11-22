package com.isis3510.spendiq.views.map

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color as ComposeColor
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.TransactionViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import androidx.navigation.NavController

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    accountViewModel: AccountViewModel = viewModel(),
    transactionViewModel: TransactionViewModel = viewModel()
) {
    // Fetch accounts and transactions
    val accounts by accountViewModel.accounts.collectAsState()
    val transactions by transactionViewModel.transactions.collectAsState()

    // Log the number of accounts and transactions
    LaunchedEffect(accounts) {
        Log.d("MapScreen", "Number of accounts fetched: ${accounts.size}")
    }

    LaunchedEffect(transactions) {
        Log.d("MapScreen", "Number of transactions fetched: ${transactions.size}")
        transactions.forEach { transaction ->
            Log.d("MapScreen", "Transaction ID: ${transaction.id}, Type: ${transaction.transactionType}, Amount: ${transaction.amount}")
        }
    }

    // Handle the camera position and default location
    val defaultLocation = LatLng(4.6097100, -74.0817500) // Default to Bogota
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 10f)
    }

    // Store markers for income and expense transactions
    val incomeMarkers = remember { mutableStateListOf<MarkerOptions>() }
    val expenseMarkers = remember { mutableStateListOf<MarkerOptions>() }

    // Fetch all accounts and their transactions when the screen is loaded
    LaunchedEffect(Unit) {
        accountViewModel.fetchAccounts() // Fetch accounts first
        accounts.forEach { account ->
            // Log account ID before fetching transactions
            Log.d("MapScreen", "Fetching transactions for account: ${account.id}")
            transactionViewModel.fetchTransactions(account.id) // Fetch transactions for each account
        }
    }

    // Separate markers for Income and Expense
    LaunchedEffect(transactions) {
        incomeMarkers.clear()
        expenseMarkers.clear()

        val markerCounts = mutableMapOf<LatLng, Int>() // Store counts of markers at the same location

        transactions.forEach { transaction ->
            // Log each transaction's location
            val position = LatLng(transaction.location?.latitude ?: 0.0, transaction.location?.longitude ?: 0.0)

            // Find the corresponding account for the transaction
            val account = accounts.firstOrNull { it.id == transaction.accountId }

            // Check if account is found and if location is valid
            if (account != null && position.latitude != 0.0 && position.longitude != 0.0) {
                // Increase the marker count at the same location
                markerCounts[position] = markerCounts.getOrDefault(position, 0) + 1

                // Generate the custom marker
                val markerIcon = when (transaction.transactionType) {
                    "Income" -> BitmapDescriptorFactory.fromBitmap(
                        generateCustomMarker(account.color, "Income")
                    )
                    "Expense" -> BitmapDescriptorFactory.fromBitmap(
                        generateCustomMarker(account.color, "Expense")
                    )
                    else -> BitmapDescriptorFactory.defaultMarker() // Default if not income or expense
                }

                // Add markers to appropriate list
                if (transaction.transactionType == "Income") {
                    incomeMarkers.add(
                        MarkerOptions().position(position).title("Income: ${transaction.amount}").icon(markerIcon)
                    )
                } else if (transaction.transactionType == "Expense") {
                    expenseMarkers.add(
                        MarkerOptions().position(position).title("Expense: ${transaction.amount}").icon(markerIcon)
                    )
                }
            }
        }

        // For markers that are in the same position, update their title with the count
        markerCounts.forEach { (position, count) ->
            incomeMarkers.find { it.position == position }?.apply {
                title("Transactions: $count")
            }
            expenseMarkers.find { it.position == position }?.apply {
                title("Transactions: $count")
            }
        }
    }

    // Scaffold layout for the map with back button in the TopAppBar
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Locations") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                // Add income markers
                incomeMarkers.forEach { marker ->
                    Marker(state = MarkerState(position = marker.position), title = marker.title, icon = marker.icon)
                }
                // Add expense markers
                expenseMarkers.forEach { marker ->
                    Marker(state = MarkerState(position = marker.position), title = marker.title, icon = marker.icon)
                }
            }
        }
    }
}

// Helper function to generate a custom marker
fun generateCustomMarker(accountColor: ComposeColor, type: String): Bitmap {
    // Create a bitmap to draw the custom marker
    val width = 100
    val height = 100
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Outer circle (account color)
    val outerCirclePaint = android.graphics.Paint().apply {
        color = accountColor.toArgb()
        style = android.graphics.Paint.Style.FILL
    }
    canvas.drawCircle(width / 2f, height / 2f, width / 2f, outerCirclePaint)

    // Inner circle (income or expense)
    val innerCirclePaint = android.graphics.Paint().apply {
        color = if (type == "Income") android.graphics.Color.GREEN else android.graphics.Color.RED
        style = android.graphics.Paint.Style.FILL
    }
    canvas.drawCircle(width / 2f, height / 2f, width / 4f, innerCirclePaint)

    return bitmap
}
