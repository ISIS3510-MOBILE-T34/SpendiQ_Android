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
 * Displays a list of nearby sales offers to the user. This screen utilizes location permissions to
 * identify the user's current location and sorts the offers based on proximity to provide a more
 * relevant user experience.
 *
 * Key Features:
 * - Location-Based Filtering: Only displays offers within 1 kilometer of the user's current location.
 * - Permissions Handling: Manages location permissions and displays prompts if permission is not granted.
 * - Dynamic UI Components:
 *   - `LocationPermissionCard`: A card prompting users to enable location permissions if not granted.
 *   - `OfferCard`: Displays individual offer details, including store image, name, description,
 *     and an estimated distance from the user's current location.
 * - Navigation: Enables users to navigate to a detailed view of each offer when clicked.
 *
 * UI Structure:
 * - Scaffold with:
 *   - TopAppBar for the screen title.
 *   - BottomNavigation for easy navigation between sections.
 * - Column layout including:
 *   - `LocationPermissionCard` (if location access is not granted).
 *   - A list of filtered offers rendered as `OfferCard` elements.
 *
 * Supporting Functions:
 * - `getCurrentLocation`: Retrieves the user's current location if location permissions are granted.
 * - `sortOffersByDistance`: Filters and sorts offers by their proximity to the user’s location.
 * - `formatDistance`: Formats distances into meters or kilometers for display clarity.
 *
 * @param navController [NavController] used for navigating within the app.
 * @param viewModel [OffersViewModel] provides the list of offers.
 * @param transactionViewModel [TransactionViewModel] for integration with bottom navigation.
 * @param accountViewModel [AccountViewModel] for integration with bottom navigation.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersScreen(
    navController: NavController,
    viewModel: OffersViewModel,
    transactionViewModel: TransactionViewModel,
    accountViewModel: AccountViewModel
) {
    // Acceder al contexto y configurar el scope de corrutinas
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Gestión de estados para ofertas, ubicación y permisos
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

    // Lanzador de solicitud de permisos para acceso a la ubicación
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

    // Carga inicial de datos y comprobación de permisos de ubicación
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

    // Reordenar y filtrar ofertas cada vez que cambie la lista de ofertas o la ubicación actual
    LaunchedEffect(offers, currentLocation) {
        sortedOffers = sortOffersByDistance(offers, currentLocation)
    }

    // Diseño Scaffold con barra superior y navegación inferior
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Special offers in your area") },
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
                contentDescription = "Ubicación",
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Habilita la ubicación para mejores ofertas",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onEnableClick) {
                Text("Habilitar")
            }
        }
    }
}

@Composable
fun OfferCard(offer: Offer, distance: Float?, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Imagen de la tienda
            offer.shopImage?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Imagen de la Tienda",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Nombre de la tienda y distancia
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
                                text = "${formatDistance(it)} de distancia",
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

private fun sortOffersByDistance(offers: List<Offer>, currentLocation: Location?): List<Pair<Offer, Float?>> {
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
                null // Excluir ofertas que estén a más de 1 km
            }
        } else {
            null // Excluir ofertas sin ubicación válida
        }
    }.sortedBy { it.second }
}

private fun formatDistance(meters: Float): String {
    val df = DecimalFormat("#.#")
    return when {
        meters < 1000 -> "${df.format(meters)}m"
        else -> "${df.format(meters / 1000)}km"
    }
}
