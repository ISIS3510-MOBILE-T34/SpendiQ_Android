package com.isis3510.spendiq.view.promos

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.isis3510.spendiq.model.data.Promo
import com.isis3510.spendiq.views.common.BottomNavigation
import com.isis3510.spendiq.viewmodel.PromoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromosScreen(navController: NavController, viewModel: PromoViewModel) {
    val promos by viewModel.promos.collectAsState()
    var selectedPromo by remember { mutableStateOf<Promo?>(null) }
    var showAddTransactionModal by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchPromos()
    }

    Scaffold(
        bottomBar = {
            BottomNavigation(
                navController = navController,
                onAddTransactionClick = { showAddTransactionModal = true }
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
                "Special Sales in your Area",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(promos) { promo ->
                    PromoItem(promo) {
                        selectedPromo = promo
                    }
                }
            }
        }
    }

    selectedPromo?.let { promo ->
        PromoDetailDialog(promo) {
            selectedPromo = null
        }
    }
}

@Composable
fun PromoItem(promo: Promo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(promo.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(promo.description, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Expires on: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(promo.expirationDate)}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun PromoDetailDialog(promo: Promo, onDismiss: () -> Unit) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(promo.title) },
        text = {
            Column {
                Text("Discount Code: ${promo.discountCode}")
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "View in Maps",
                    color = Color.Blue,
                    modifier = Modifier.clickable {
                        val uri = Uri.parse("geo:0,0?q=${promo.restaurantName}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(mapIntent)
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}