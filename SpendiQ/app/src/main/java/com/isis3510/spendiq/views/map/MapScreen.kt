package com.isis3510.spendiq.views.map

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
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
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.TransactionViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    accountViewModel: AccountViewModel = viewModel(),
    transactionViewModel: TransactionViewModel = viewModel()
) {
    // Observe accounts and transactions
    val accounts by accountViewModel.accounts.collectAsState()
    val transactions by transactionViewModel.transactions.collectAsState()

    // Default location (Bogota)
    val defaultLocation = LatLng(4.6097100, -74.0817500)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 10f)
    }

    // Marker and heatmap data
    val incomeMarkers = remember { mutableStateListOf<MarkerOptions>() }
    val expenseMarkers = remember { mutableStateListOf<MarkerOptions>() }
    val heatmapPoints = remember { mutableStateListOf<LatLng>() }

    // Fetch accounts and transactions dynamically
    LaunchedEffect(accounts) {
        incomeMarkers.clear()
        expenseMarkers.clear()
        heatmapPoints.clear()

        // Fetch transactions for all accounts
        accounts.forEach { account ->
            transactionViewModel.fetchTransactions(account.id)
        }

        // Update markers and heatmap points after transactions are fetched
        transactions.forEach { transaction ->
            val position = LatLng(transaction.location?.latitude ?: 0.0, transaction.location?.longitude ?: 0.0)
            if (position.latitude != 0.0 && position.longitude != 0.0) {
                heatmapPoints.add(position)
            }

            // Create markers
            val account = accounts.firstOrNull { it.id == transaction.accountId }
            if (account != null && position.latitude != 0.0 && position.longitude != 0.0) {
                val markerIcon = when (transaction.transactionType) {
                    "Income" -> BitmapDescriptorFactory.fromBitmap(
                        generateCustomMarker(account.color, "Income")
                    )
                    "Expense" -> BitmapDescriptorFactory.fromBitmap(
                        generateCustomMarker(account.color, "Expense")
                    )
                    else -> BitmapDescriptorFactory.defaultMarker()
                }

                val title = buildString {
                    append("${transaction.transactionType}: ${transaction.amount} ")
                    append("- ${transaction.transactionName}")
                    append(" on ${
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(transaction.dateTime.toDate())
                    }")
                    if (transaction.amountAnomaly) append(" (Amount Anomaly)")
                    if (transaction.locationAnomaly) append(" (Location Anomaly)")
                }

                if (transaction.transactionType == "Income") {
                    incomeMarkers.add(
                        MarkerOptions().position(position).title(title).icon(markerIcon)
                    )
                } else if (transaction.transactionType == "Expense") {
                    expenseMarkers.add(
                        MarkerOptions().position(position).title(title).icon(markerIcon)
                    )
                }
            }
        }

        Log.d("Heatmap", "Heatmap points updated: $heatmapPoints")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Locations & Heatmap") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                // Add heatmap if there are points
                if (heatmapPoints.isNotEmpty()) {
                    val heatmapTileProvider = HeatmapTileProvider.Builder()
                        .data(heatmapPoints)
                        .opacity(0.7) // Adjust opacity for visibility
                        .radius(50) // Larger radius for density
                        .build()
                    TileOverlay(tileProvider = heatmapTileProvider)
                    Log.d("Heatmap", "Heatmap added with ${heatmapPoints.size} points")
                } else {
                    Log.d("Heatmap", "No points for heatmap")
                }

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

// Helper function to generate custom marker
fun generateCustomMarker(accountColor: ComposeColor, type: String): Bitmap {
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