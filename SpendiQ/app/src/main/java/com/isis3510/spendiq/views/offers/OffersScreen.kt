package com.isis3510.spendiq.views.offers

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.ContextCompat
import kotlinx.coroutines.tasks.await
import java.text.DecimalFormat

@Composable
fun OffersScreen(
    navController: NavController,
    viewModel: OffersViewModel,
    accountViewModel: AccountViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val offers by viewModel.offers.collectAsState()
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var sortedOffers by remember { mutableStateOf<List<Pair<Offer, Float?>>>(emptyList()) }

    // Location permission state
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Permission launcher
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

    // Initial location check and setup
    LaunchedEffect(Unit) {
        viewModel.fetchOffers()
        if (hasLocationPermission) {
            getCurrentLocation(context)?.let { location ->
                currentLocation = location
                sortedOffers = sortOffersByDistance(offers, location)
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Update sorted offers when offers or location changes
    LaunchedEffect(offers, currentLocation) {
        sortedOffers = sortOffersByDistance(offers, currentLocation)
    }

    Scaffold(
        bottomBar = {
            BottomNavigation(navController = navController, accountViewModel)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                "Special Offers",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                "Discover great deals near you",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (!hasLocationPermission) {
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
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }) {
                            Text("Enable")
                        }
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(sortedOffers) { (offer, distance) ->
                    OfferCard(
                        offer = offer,
                        distance = distance,
                        onClick = { offer.id?.let { id -> navController.navigate("specialSalesDetail/$id") } }
                    )
                }
            }
        }
    }
}

@Composable
fun OfferCard(offer: Offer, distance: Float?, onClick: () -> Unit) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Image Section
            offer.shopImage?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Offer Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Content Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Distance and business name row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    offer.placeName?.let {
                        Text(
                            text = it,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Distance chip
                    distance?.let {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "${formatDistance(it)} away",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Offer description
                offer.offerDescription?.let {
                    Text(
                        text = it,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Recommendation reason
                offer.recommendationReason?.let {
                    Text(
                        text = "Recommended: $it",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // Map button
                if (offer.latitude != null && offer.longitude != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val uri = Uri.parse("geo:${offer.latitude},${offer.longitude}?q=${offer.latitude},${offer.longitude}(${offer.placeName})")
                            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            if (mapIntent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(mapIntent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Open in Maps",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View on Maps")
                    }
                }
            }
        }
    }
}

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

private fun formatDistance(meters: Float): String {
    val df = DecimalFormat("#.#")
    return when {
        meters < 1000 -> "${df.format(meters)}m"
        else -> "${df.format(meters / 1000)}km"
    }
}
