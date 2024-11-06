package com.isis3510.spendiq.views.offers

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.isis3510.spendiq.model.data.Offer
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.OffersViewModel
import com.isis3510.spendiq.views.common.BottomNavigation
import kotlinx.coroutines.launch
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.isis3510.spendiq.viewmodel.TransactionViewModel
import kotlinx.coroutines.tasks.await
import java.text.DecimalFormat

/**
 * OffersScreen composable function
 *
 * Displays a list of special sales offers near the user's location. This screen leverages location
 * permissions to determine the user's current location and sorts the offers based on proximity.
 *
 * Key Features:
 * - Location-based Sorting: Requests the user’s location to sort nearby offers by distance.
 * - Permissions Handling: Manages location permissions, prompting the user if access is not granted.
 * - Dynamic UI Elements:
 *   - LocationPermissionCard: Displays a prompt to enable location permissions if not granted.
 *   - OfferCard: Shows individual offer details, including the store image, name, description,
 *     and estimated distance from the user’s current location.
 * - Navigation: Allows users to navigate to detailed offer screens when an offer is clicked.
 *
 * UI Structure:
 * - Scaffold with TopAppBar for the screen title and BottomNavigation for navigating between sections.
 * - Column layout containing:
 *   - LocationPermissionCard (if permission is not granted).
 *   - List of sorted offers with OfferCards for each available offer.
 *
 * Supporting Functions:
 * - `getCurrentLocation`: Retrieves the user’s current location if permissions are granted.
 * - `sortOffersByDistance`: Sorts the list of offers by proximity to the user’s location.
 * - `formatDistance`: Formats distances in meters or kilometers for display.
 *
 * @param navController [NavController] for handling navigation within the app.
 * @param viewModel [OffersViewModel] provides the list of offers.
 * @param transactionViewModel [TransactionViewModel] used for bottom navigation integration.
 * @param accountViewModel [AccountViewModel] used for bottom navigation integration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersScreen(
    navController: NavController,
    viewModel: OffersViewModel,
    transactionViewModel: TransactionViewModel,
    accountViewModel: AccountViewModel
) {
    // Context and coroutine scope
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Collects offers from the ViewModel
    val offers by viewModel.offers.collectAsState()

    // Manages current location and sorted offers based on distance
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var sortedOffers by remember { mutableStateOf<List<Pair<Offer, Float?>>>(emptyList()) }

    // Location permission status
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher to request location permission if not granted
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            scope.launch {
                getCurrentLocation(context)?.let { location ->
                    currentLocation = location
                    sortedOffers = sortOffersByDistance(offers, location)
                }
            }
        }
    }

    // Initial data loading and location-based sorting
    LaunchedEffect(Unit) {
        viewModel.fetchOffers() // Fetches offers initially
        if (hasLocationPermission) {
            getCurrentLocation(context)?.let { location ->
                currentLocation = location
                sortedOffers = sortOffersByDistance(offers, location)
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Re-sorts offers if offers or location changes
    LaunchedEffect(offers, currentLocation) {
        sortedOffers = sortOffersByDistance(offers, currentLocation)
    }

    // Scaffold layout with top bar and bottom navigation
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Special Sales in your Area") }, // Screen title
            )
        },
        bottomBar = {
            BottomNavigation(navController = navController, transactionViewModel = transactionViewModel, accountViewModel)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Request location permission if not granted
                if (!hasLocationPermission) {
                    LocationPermissionCard(
                        onEnableClick = {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    )
                }

                // Description text
                Text(
                    "Based on the shops where you have purchased before, we think these sales near to your location may interest you",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // List of sorted offers
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(sortedOffers) { (offer, distance) ->
                        OfferCard(
                            offer = offer,
                            distance = distance,
                            onClick = {
                                offer.id?.let { id ->
                                    navController.navigate("specialSalesDetail/$id") // Navigate to offer details
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationPermissionCard(onEnableClick: () -> Unit) {
    // Card prompting the user to enable location permissions
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Enable location for better offers",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onEnableClick) {
                Text("Enable") // Button to enable location permissions
            }
        }
    }
}

@Composable
fun OfferCard(offer: Offer, distance: Float?, onClick: () -> Unit) {
    // Card showing the details of a specific offer
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Display shop image if available
            offer.shopImage?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Store Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Store name and distance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    offer.placeName?.let {
                        Text(
                            text = it,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    distance?.let {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "${formatDistance(it)} away",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Offer description
                offer.offerDescription?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// Gets the current location if permission is granted
private suspend fun getCurrentLocation(context: Context): Location? {
    return try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.await()
        } else null
    } catch (e: Exception) {
        null
    }
}

// Sorts offers by distance to the current location
private fun sortOffersByDistance(offers: List<Offer>, currentLocation: Location?): List<Pair<Offer, Float?>> {
    return offers.map { offer ->
        if (currentLocation != null && offer.latitude != null && offer.longitude != null) {
            val offerLocation = Location("").apply {
                latitude = offer.latitude
                longitude = offer.longitude
            }
            val distance = currentLocation.distanceTo(offerLocation)
            offer to distance
        } else {
            offer to null
        }
    }.sortedBy { it.second ?: Float.MAX_VALUE }
}

// Formats the distance in meters or kilometers
private fun formatDistance(meters: Float): String {
    val df = DecimalFormat("#.#")
    return when {
        meters < 1000 -> "${df.format(meters)}m"
        else -> "${df.format(meters / 1000)}km"
    }
}
