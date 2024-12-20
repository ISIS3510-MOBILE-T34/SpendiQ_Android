// OffersScreen.kt
package com.isis3510.spendiq.views.offers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.isis3510.spendiq.R
import com.isis3510.spendiq.model.data.Offer
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.ConnectivityViewModel
import com.isis3510.spendiq.viewmodel.OffersViewModel
import com.isis3510.spendiq.viewmodel.TransactionViewModel
import com.isis3510.spendiq.views.common.BottomNavigation
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersScreen(
    navController: NavController,
    viewModel: OffersViewModel,
    transactionViewModel: TransactionViewModel,
    accountViewModel: AccountViewModel,
    connectivityViewModel: ConnectivityViewModel // Agregar ViewModel de conectividad
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val offers by viewModel.offers.collectAsState()
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var sortedOffers by remember { mutableStateOf<List<Pair<Offer, Float?>>>(emptyList()) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
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

    // Observar el estado de conectividad
    val isNetworkAvailable by connectivityViewModel.isConnected.observeAsState(true)

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

    LaunchedEffect(offers, currentLocation) {
        sortedOffers = sortOffersByDistance(offers, currentLocation)
    }

    Scaffold(
        topBar = {
            Column {
                ConnectivityBanner(isConnected = isNetworkAvailable)
                TopAppBar(
                    title = { Text("Special offers in your area",
                        fontWeight = FontWeight.Bold) }
                )
            }
        },
        bottomBar = {
            BottomNavigation(
                navController = navController,
                transactionViewModel = transactionViewModel,
                accountViewModel = accountViewModel
            )
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
                if (!hasLocationPermission) {
                    LocationPermissionCard(
                        onEnableClick = {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    )
                }

                Text(
                    "Based on the stores where you've shopped before, we believe these offers near your location may be of interest to you",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

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
                                    navController.navigate("specialSalesDetail/$id")
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
                Text("Enable")
            }
        }
    }
}

@Composable
fun OfferCard(offer: Offer, distance: Float?, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            // Aplica un borde dorado si la oferta es "featured"
            .border(
                width = if (offer.featured) 2.dp else 0.dp,
                color = if (offer.featured) Color(0xFFFFD700) else Color.Transparent, // Dorado
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        // Cambia el color de fondo si es "featured"
        colors = CardDefaults.cardColors(
            containerColor = if (offer.featured) Color(0xFFFFF8DC) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column {

            // Imagen de la oferta
            offer.shopImage?.let { imageUrl ->
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    factory = { context ->
                        ImageView(context).apply {
                            Picasso.get()
                                .load(imageUrl)
                                .placeholder(R.drawable.placeholder_background)
                                .error(R.drawable.error_background)
                                .into(this)
                        }
                    }
                )
            }

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Nombre del lugar
                    offer.placeName?.let {
                        Text(
                            text = it,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Distancia
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

                // Descripción de la oferta
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

private fun sortOffersByDistance(
    offers: List<Offer>,
    currentLocation: Location?
): List<Pair<Offer, Float?>> {
    return offers.mapNotNull { offer ->
        if (currentLocation != null && offer.latitude != null && offer.longitude != null) {
            val offerLocation = Location("").apply {
                latitude = offer.latitude
                longitude = offer.longitude
            }
            val distance = currentLocation.distanceTo(offerLocation)
            if (distance <= 1000f) {
                offer to distance
            } else {
                null
            }
        } else {
            null
        }
    }.sortedWith(
        // Primero ordena por 'featured' (true primero), luego por distancia (ascendente)
        compareByDescending<Pair<Offer, Float?>> { it.first.featured }
            .thenBy { it.second }
    )
}

private fun formatDistance(meters: Float): String {
    val df = DecimalFormat("#.#")
    return if (meters < 1000) "${df.format(meters)}m" else "${df.format(meters / 1000)}km"
}

@Composable
fun ConnectivityBanner(isConnected: Boolean) {
    AnimatedVisibility(
        visible = !isConnected,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(durationMillis = 300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(durationMillis = 300)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Red
        ) {
            Text(
                text = "You have no Internet connection! You will not see updates until the connection is restored",
                modifier = Modifier.padding(16.dp),
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
