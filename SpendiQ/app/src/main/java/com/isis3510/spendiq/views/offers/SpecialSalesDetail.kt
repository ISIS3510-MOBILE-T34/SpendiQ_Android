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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.isis3510.spendiq.model.data.Offer
import androidx.navigation.NavController
import android.widget.ImageView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.isis3510.spendiq.R
import com.isis3510.spendiq.views.common.BottomNavigation
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialSalesDetail(
    offer: Offer,
    navController: NavController,
    transactionViewModel: TransactionViewModel,
    accountViewModel: AccountViewModel,
    firebaseAnalytics: FirebaseAnalytics
) {
    val context = LocalContext.current
    var isNavigating by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val bundle = Bundle().apply {
            putString("offer_id", offer.id)
            putString("offer_name", offer.placeName)
        }
        firebaseAnalytics.logEvent("view_promotion", bundle)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Special Sales") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
                .verticalScroll(rememberScrollState())
        ) {
            // Caching - J0FR
            offer.shopImage?.let { imageUrl -> // Ensure imageUrl is not null
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()   // The ImageView will take the full width
                        .height(200.dp),  // Height is fixed to 200dp
                    factory = { context -> // Create the ImageView
                        ImageView(context).apply {
                            // Use Glide to load the image into this ImageView
                            Glide.with(context)
                                .load(imageUrl)                       // Load image from URL
                                .placeholder(R.drawable.placeholder_background) // Show while loading
                                .error(R.drawable.error_background)  // Show if loading fails
                                .into(this)                          // Display the image in this ImageView
                        }
                    }
                )
            }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                offer.placeName?.let {
                    Text(
                        text = it,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (offer.latitude != null && offer.longitude != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = rememberCameraPositionState {
                                position = CameraPosition.fromLatLngZoom(
                                    LatLng(offer.latitude, offer.longitude),
                                    15f
                                )
                            }
                        ) {
                            Marker(
                                state = MarkerState(position = LatLng(offer.latitude, offer.longitude)),
                                title = offer.placeName
                            )
                        }
                    }

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
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open in Maps", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Sales",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                offer.offerDescription?.let {
                    Text(
                        text = it,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                offer.recommendationReason?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = "Recommended because: $it",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Button(
                    onClick = {
                        val bundle = Bundle().apply {
                            putString("offer_id", offer.id)
                            putString("offer_name", offer.placeName)
                        }
                        firebaseAnalytics.logEvent("redeem_offer", bundle)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Redeem",
                        color = Color.White
                    )
                }
            }
        }
    }
}
