package com.isis3510.spendiq.views.offers // Adjusted to match file location

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight // Added import for FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage // Updated from deprecated rememberImagePainter
import com.isis3510.spendiq.model.data.Offer
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.OffersViewModel
import com.isis3510.spendiq.views.common.BottomNavigation

@Composable
fun OffersScreen(navController: NavController, viewModel: OffersViewModel, accountViewModel: AccountViewModel) {
    val offers by viewModel.offers.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchOffers()
    }

    Scaffold(
        bottomBar = {
            BottomNavigation(
                navController = navController,
                accountViewModel
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                "Special Offers in Your Area",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold // Added FontWeight here
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(offers) { offer ->
                    OfferItem(offer = offer, onClick = {
                        offer.id?.let { id -> navController.navigate("specialSalesDetail/$id") }
                    })
                }
            }
        }
    }
}

@Composable
fun OfferItem(offer: Offer, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            offer.placeName?.let {
                Text(
                    text = it,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            offer.offerDescription?.let { Text(it, fontSize = 14.sp) }
            Spacer(modifier = Modifier.height(8.dp))
            offer.shopImage?.let {
                AsyncImage(
                    model = it,
                    contentDescription = "Offer Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Reason: ${offer.recommendationReason}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
