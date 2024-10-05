package com.isis3510.spendiq.views.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.isis3510.spendiq.R
import com.isis3510.spendiq.views.transaction.AddTransactionModal
import com.isis3510.spendiq.viewmodel.AuthenticationViewModel

@Composable
fun MainContent(navController: NavController, viewModel: AuthenticationViewModel) {
    var showAddTransactionModal by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { BottomNavigation(navController, onAddTransactionClick = { showAddTransactionModal = true }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to SpendiQ!",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    viewModel.logout()
                    navController.navigate("authentication") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Logout")
            }
        }

        if (showAddTransactionModal) {
            AddTransactionModal(
                onDismiss = { showAddTransactionModal = false },
                onTransactionAdded = {
                    // Handle successful transaction addition (e.g., show a snackbar)
                    showAddTransactionModal = false
                }
            )
        }
    }
}

@Composable
fun BottomNavigation(navController: NavController, onAddTransactionClick: () -> Unit) {
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
            NavItem("Home", R.drawable.home24, Color(0xFF5875DD), navController, "main")
            NavItem("Promos", R.drawable.gift24, Color.Black, navController, "promos")
            AddTransactionButton(onClick = onAddTransactionClick)
            NavItem("Accounts", R.drawable.creditcard24, Color.Black, navController, "accounts")
            NavItem("Profile", R.drawable.person24, Color.Black, navController, "profile")
        }
    }
}

@Composable
fun NavItem(label: String, iconRes: Int, tint: Color, navController: NavController, route: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = { navController.navigate(route) }) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            color = tint,
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