package com.isis3510.spendiq.views.offers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.isis3510.spendiq.model.data.Offer
import androidx.navigation.NavController
import com.squareup.picasso.Picasso
import android.widget.ImageView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import com.google.firebase.analytics.FirebaseAnalytics
import com.isis3510.spendiq.R
import com.isis3510.spendiq.views.common.BottomNavigation
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.TransactionViewModel

/**
 * SpecialSalesDetail composable function
 *
 * Displays detailed information about a special sales offer, including images, location, and descriptions.
 * The function provides navigation options and allows users to open the store’s location in Google Maps.
 *
 * UI Structure:
 * - Top Bar: Includes a title ("Special Sales") and a back button to navigate back to the previous screen.
 * - Store Image: Displays the store's image at the top if available.
 * - Store Details:
 *     - Store name displayed in bold font.
 *     - A Google Map view with a marker at the store’s location if latitude and longitude are provided.
 *     - Button to open the store’s location in Google Maps app.
 * - Sales Information:
 *     - Section for displaying details about sales offers with an optional description.
 *     - Recommended offer reason displayed in a styled card if available.
 *
 * Navigation and Intents:
 * - Navigates back to the previous screen when the back button is pressed.
 * - Launches Google Maps with the store’s location when the "Open in Maps" button is clicked.
 *
 * @param offer [Offer] containing details about the sales offer
 * @param navController [NavController] used for navigation to other screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialSalesDetail(
    offer: Offer,
    navController: NavController,
    transactionViewModel: TransactionViewModel,
    accountViewModel: AccountViewModel,
    firebaseAnalytics: FirebaseAnalytics
) {
    // Context for starting external intents
    val context = LocalContext.current
    var isNavigating by remember { mutableStateOf(false) }

    // Registra el evento de visualización de la promoción
    LaunchedEffect(Unit) {
        val bundle = Bundle().apply {
            putString("offer_id", offer.id) // Suponiendo que `offer` tiene un ID
            putString("offer_name", offer.placeName)
        }
        firebaseAnalytics.logEvent("view_promotion", bundle)
    }

    // Scaffold component to manage top-level layout including top bar
    Scaffold(
        topBar = {
            // Top bar with title and back navigation
            TopAppBar(
                title = { Text("Special Sales") }, // Title of the screen
                navigationIcon = {
                    // Back button for navigation
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") // Back icon
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigation(
                navController = navController,
                transactionViewModel = transactionViewModel,
                accountViewModel = accountViewModel
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()) // Enables scrolling for the content
        ) {
            // Display the store logo and name using Picasso for image loading
            offer.shopImage?.let { imageUrl ->
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp), // Sets the image height to 200dp
                    factory = { context ->
                        ImageView(context).apply {
                            Picasso.get()
                                .load(imageUrl) // Load image from the URL
                                .placeholder(R.drawable.placeholder_background) // Show placeholder image if needed
                                .error(R.drawable.error_background) // Show error image if the load fails
                                .into(this) // Set the image into the ImageView
                        }
                    }
                )
            }

            // Content column for offer details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp) // Adds padding around the content
            ) {
                // Display the place name if available
                offer.placeName?.let {
                    Text(
                        text = it,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold // Bold font style for emphasis
                    )
                }

                Spacer(modifier = Modifier.height(16.dp)) // Adds vertical spacing

                // Display a map with the location if latitude and longitude are available
                if (offer.latitude != null && offer.longitude != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp) // Map height
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(), // Expands to fill card
                            cameraPositionState = rememberCameraPositionState {
                                position = CameraPosition.fromLatLngZoom(
                                    LatLng(offer.latitude, offer.longitude),
                                    15f // Zoom level for map view
                                )
                            }
                        ) {
                            // Place a marker at the offer's location
                            Marker(
                                state = MarkerState(position = LatLng(offer.latitude, offer.longitude)),
                                title = offer.placeName // Title for marker
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp)) // Adds spacing below the map

                    // Button to open the location in Google Maps app
                    Button(
                        onClick = {
                            // Create a URI for location with place name as a label
                            val uri = Uri.parse("geo:${offer.latitude},${offer.longitude}?q=${offer.latitude},${offer.longitude}(${offer.placeName})")
                            val mapIntent = Intent(Intent.ACTION_VIEW, uri) // Intent to open maps
                            mapIntent.setPackage("com.google.android.apps.maps") // Restrict to Google Maps
                            if (mapIntent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(mapIntent) // Start Google Maps if available
                            }
                        },
                        modifier = Modifier.fillMaxWidth() // Full-width button
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null) // Location icon
                        Spacer(modifier = Modifier.width(8.dp)) // Spacing between icon and text
                        Text("Open in Maps") // Button label
                    }
                }

                Spacer(modifier = Modifier.height(24.dp)) // Extra spacing between sections

                // Section for displaying sales information
                Text(
                    text = "Sales",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold // Bold for section title
                )

                Spacer(modifier = Modifier.height(8.dp)) // Small spacing below section title

                // Description of the offer if available
                offer.offerDescription?.let {
                    Text(
                        text = it,
                        fontSize = 16.sp, // Regular font size for description
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp)) // Spacing before recommendation section

                // Display recommendation reason in a styled card
                offer.recommendationReason?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer // Card background color
                        )
                    ) {
                        Text(
                            text = "Recommended because: $it",
                            modifier = Modifier.padding(16.dp), // Padding within card
                            color = MaterialTheme.colorScheme.onPrimaryContainer // Text color
                        )
                    }
                }

                Button(
                    onClick = {
                        // Registra el evento de redención
                        val bundle = Bundle().apply {
                            putString("offer_id", offer.id) // Suponiendo que `offer` tiene un ID
                            putString("offer_name", offer.placeName)
                        }
                        firebaseAnalytics.logEvent("redeem_offer", bundle)

                        // Lógica para redimir la oferta
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Redeem")
                }
            }
        }
    }
}
