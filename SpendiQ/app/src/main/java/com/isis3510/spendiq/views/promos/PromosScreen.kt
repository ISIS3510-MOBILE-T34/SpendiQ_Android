package com.isis3510.spendiq.views.promos

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.spendiq.views.main.BottomNavigation
import com.isis3510.spendiq.views.transaction.AddTransactionModal
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class Promo(
    val title: String,
    val description: String,
    val imageUrl: String,
    val discountCode: String,
    val restaurantName: String,
    val expirationDate: Date
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromosScreen(navController: NavController) {
    var promos by remember { mutableStateOf<List<Promo>>(emptyList()) }
    var selectedPromo by remember { mutableStateOf<Promo?>(null) }
    var showAddTransactionModal by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        promos = fetchPromos()
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

    if (showAddTransactionModal) {
        AddTransactionModal(
            onDismiss = { showAddTransactionModal = false },
            onTransactionAdded = {
                showAddTransactionModal = false
                // Optionally, you can add logic here to refresh the promos data if needed
            }
        )
    }
}

@Composable
fun PromoItem(promo: Promo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Image(
                painter = rememberAsyncImagePainter(promo.imageUrl),
                contentDescription = promo.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
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

suspend fun fetchPromos(): List<Promo> {
    val firestore = FirebaseFirestore.getInstance()
    val currentDate = Date()
    val snapshot = firestore.collection("promos")
        .whereGreaterThan("expirationDate", currentDate)
        .get()
        .await()

    return snapshot.documents.mapNotNull { doc ->
        val title = doc.getString("title") ?: return@mapNotNull null
        val description = doc.getString("description") ?: return@mapNotNull null
        val imageUrl = doc.getString("imageUrl") ?: return@mapNotNull null
        val discountCode = doc.getString("discountCode") ?: return@mapNotNull null
        val restaurantName = doc.getString("restaurantName") ?: return@mapNotNull null
        val expirationDate = doc.getDate("expirationDate") ?: return@mapNotNull null

        Promo(title, description, imageUrl, discountCode, restaurantName, expirationDate)
    }
}