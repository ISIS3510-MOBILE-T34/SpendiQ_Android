package com.isis3510.spendiq.views.common

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.isis3510.spendiq.R
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.TransactionViewModel
import com.isis3510.spendiq.views.transaction.AddTransactionModal

@Composable
fun BottomNavigation(
    navController: NavController,
    transactionViewModel: TransactionViewModel,
    accountViewModel: AccountViewModel
) {
    var showAddTransactionModal by remember { mutableStateOf(false) }
    val isLightTheme = !isSystemInDarkTheme()

    val lightGrayNavBar = Color(0xFFDDDDDD)
    val darkGrayNavBar = Color(0xFF2D2D2D)
    val backgroundColor = if (isLightTheme) lightGrayNavBar else darkGrayNavBar
    val iconColor = if (isLightTheme) Color.Black else Color.White

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(77.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(backgroundColor)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                label = "Home",
                iconRes = R.drawable.rounded_home_24,
                isSelected = navController.isCurrentRoute("main"),
                navController = navController,
                route = "main",
                iconColor = iconColor
            )
            NavItem(
                label = "Promos",
                iconRes = R.drawable.rounded_gifts_24,
                isSelected = navController.isCurrentRoute("promos"),
                navController = navController,
                route = "promos",
                iconColor = iconColor
            )
            AddTransactionButton(onClick = { showAddTransactionModal = true })
            NavItem(
                label = "Accounts",
                iconRes = R.drawable.creditcard24,
                isSelected = navController.isCurrentRoute("accounts"),
                navController = navController,
                route = "accounts",
                iconColor = iconColor
            )
            NavItem(
                label = "Profile",
                iconRes = R.drawable.person24,
                isSelected = navController.isCurrentRoute("profile"),
                navController = navController,
                route = "profile",
                iconColor = iconColor
            )
        }
    }

    if (showAddTransactionModal) {
        AddTransactionModal(
            accountViewModel = accountViewModel,
            transactionViewModel = transactionViewModel,
            onDismiss = { showAddTransactionModal = false },
            onTransactionAdded = { showAddTransactionModal = false }
        )
    }
}

@Composable
fun NavItem(label: String, iconRes: Int, isSelected: Boolean, navController: NavController, route: String, iconColor: Color) {
    val color = if (isSelected) Color(0xFF5875DD) else iconColor // Color de icono adaptado al tema

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
fun NavController.isCurrentRoute(route: String): Boolean {
    return this.currentBackStackEntryAsState().value?.destination?.route == route
}
