package com.isis3510.spendiq.views.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import com.isis3510.spendiq.R
import com.isis3510.spendiq.views.transaction.AddTransactionModal
import com.isis3510.spendiq.viewmodel.AuthenticationViewModel
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.tasks.await

@Composable
fun MainContent(navController: NavController, viewModel: AuthenticationViewModel) {
    var showAddTransactionModal by remember { mutableStateOf(false) }
    var accounts by remember { mutableStateOf<List<Account>>(emptyList()) }
    var promos by remember { mutableStateOf<List<Promo>>(emptyList()) }
    var currentMoney by remember { mutableStateOf(0L) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            accounts = fetchAccounts()
            currentMoney = accounts.sumOf { it.amount }
            promos = fetchPromos().take(3)
        }
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
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Sun, 15 SEP",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Summary",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Take a look at your finances",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Current available money",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
            )
            Text(
                text = "$ $currentMoney",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Accounts",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
            )
            Spacer(modifier = Modifier.height(8.dp))
            accounts.forEach { account ->
                AccountItem(account)
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Save with these promotions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
            )
            Spacer(modifier = Modifier.height(8.dp))
            promos.forEach { promo ->
                PromoItem(promo) {}
                Spacer(modifier = Modifier.height(8.dp))
            }
            Button(
                onClick = { navController.navigate("promos") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("See More Promotions")
            }
        }

        if (showAddTransactionModal) {
            AddTransactionModal(
                onDismiss = { showAddTransactionModal = false },
                onTransactionAdded = {
                    showAddTransactionModal = false
                }
            )
        }
    }
}

@Composable
fun BottomNavigation(navController: NavController, onAddTransactionClick: () -> Unit) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(77.dp)
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem("Home", R.drawable.home24, isSelected = currentRoute == "main", navController, "main")
            NavItem("Promos", R.drawable.gift24, isSelected = currentRoute == "promos", navController, "promos")
            AddTransactionButton(onClick = onAddTransactionClick)
            NavItem("Accounts", R.drawable.creditcard24, isSelected = currentRoute == "accounts", navController, "accounts")
            NavItem("Profile", R.drawable.person24, isSelected = currentRoute == "profile", navController, "profile")
        }
    }
}

@Composable
fun NavItem(label: String, iconRes: Int, isSelected: Boolean, navController: NavController, route: String) {
    val color = if (isSelected) Color(0xFF5875DD) else Color.Black

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = { navController.navigate(route) }) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AddTransactionButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color(0xFF5875DD))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.add24),
                contentDescription = "Add Transaction",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
fun AccountItem(account: Account) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = account.color)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = account.name,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = account.type,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$ ${account.amount}",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
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
                    .height(150.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(promo.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(promo.description, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Expires on: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(promo.expirationDate)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

suspend fun fetchAccounts(): List<Account> {
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()

    val snapshot = firestore.collection("accounts")
        .whereEqualTo("user_id", userId)
        .get()
        .await()

    return snapshot.documents.mapNotNull { doc ->
        val name = doc.getString("name") ?: return@mapNotNull null
        val amount = doc.getLong("amount") ?: 0L
        val color = when (name) {
            "Nu" -> Color(0xFF9747FF)
            "Bancolombia" -> Color(0xFFFFCC00)
            "Nequi" -> Color(0xFF8B2F87)
            else -> Color.Gray
        }
        Account(name, "Debit", amount, color)
    }
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

data class Account(
    val name: String,
    val type: String,
    val amount: Long,
    val color: Color
)

data class Promo(
    val title: String,
    val description: String,
    val imageUrl: String,
    val discountCode: String,
    val restaurantName: String,
    val expirationDate: Date
)