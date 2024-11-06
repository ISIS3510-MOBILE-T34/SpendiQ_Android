package com.isis3510.spendiq.views.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.navigation.compose.currentBackStackEntryAsState
import com.isis3510.spendiq.R
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.TransactionViewModel
import com.isis3510.spendiq.views.transaction.AddTransactionModal

/**
 * BottomNavigation composable function
 *
 * This component renders a custom bottom navigation bar for the app, allowing users to navigate between
 * key sections: Home, Promotions, Accounts, and Profile. The bar also includes a centered button for adding
 * new transactions, which opens a modal when clicked.
 *
 * Key Features:
 * - Navigation Icons: Provides navigation icons for the main app sections.
 * - Add Transaction Button: Centered button to open a modal for adding new transactions.
 * - Route Highlighting: Highlights the active icon based on the current route.
 *
 * Functionality:
 * - `BottomNavigation`: Displays the navigation bar with icons and labels. It manages the visibility of
 *   `AddTransactionModal`, which allows users to input transaction details.
 * - `NavItem`: Displays each navigation item with an icon and label, with dynamic color based on selection.
 * - `AddTransactionButton`: Renders a circular button at the center of the navigation bar to trigger the add
 *   transaction modal.
 * - `isCurrentRoute`: Extension function on `NavController` to check if a given route is currently active.
 *
 * Supporting Components:
 * - `NavItem`: Used for each navigation icon (Home, Promos, Accounts, Profile) with an icon and label.
 * - `AddTransactionButton`: A circular button that opens `AddTransactionModal` when clicked.
 * - `AddTransactionModal`: A modal dialog for creating a new transaction, triggered by the Add button.
 *
 * @param navController [NavController] used to manage navigation between screens.
 * @param transactionViewModel [TransactionViewModel] manages transactions, passed to the modal.
 * @param accountViewModel [AccountViewModel] provides account-related data, passed to the modal.
 */
@Composable
fun BottomNavigation(
    navController: NavController,
    transactionViewModel: TransactionViewModel,
    accountViewModel: AccountViewModel // Add AccountViewModel parameter
) {
    var showAddTransactionModal by remember { mutableStateOf(false) }

    // Main container for the navigation bar, with rounded corners and white background
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(77.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(Color.White)
    ) {
        // Row layout to arrange navigation items horizontally
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Define each navigation item
            NavItem(
                label = "Home",
                iconRes = R.drawable.rounded_home_24,
                isSelected = navController.isCurrentRoute("main"),
                navController = navController,
                route = "main"
            )
            NavItem(
                label = "Promos",
                iconRes = R.drawable.rounded_gifts_24,
                isSelected = navController.isCurrentRoute("promos"),
                navController = navController,
                route = "promos"
            )
            AddTransactionButton(onClick = { showAddTransactionModal = true }) // Button to add transactions
            NavItem(
                label = "Accounts",
                iconRes = R.drawable.creditcard24,
                isSelected = navController.isCurrentRoute("accounts"),
                navController = navController,
                route = "accounts"
            )
            NavItem(
                label = "Profile",
                iconRes = R.drawable.person24,
                isSelected = navController.isCurrentRoute("profile"),
                navController = navController,
                route = "profile"
            )
        }
    }

    // Display modal for adding a transaction when Add button is clicked
    if (showAddTransactionModal) {
        AddTransactionModal(
            accountViewModel = accountViewModel,
            transactionViewModel = transactionViewModel,
            onDismiss = { showAddTransactionModal = false },
            onTransactionAdded = { showAddTransactionModal = false }
        )
    }
}

/**
 * NavItem composable function
 *
 * Displays an individual navigation item with an icon and label. Changes color based on whether the item is selected.
 *
 * @param label Text label for the navigation item
 * @param iconRes Resource ID of the icon to display
 * @param isSelected Boolean indicating if the item is selected (current route)
 * @param navController [NavController] used to navigate to the item's route
 * @param route Route associated with this navigation item
 */
@Composable
fun NavItem(label: String, iconRes: Int, isSelected: Boolean, navController: NavController, route: String) {
    val color = if (isSelected) Color(0xFF5875DD) else Color.Black // Color based on selection

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

/**
 * AddTransactionButton composable function
 *
 * Displays a circular button for adding transactions, styled with a distinctive color and centered in the bottom navigation bar.
 *
 * @param onClick Lambda function triggered when the button is clicked
 */
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

/**
 * Extension function to check if a specific route is currently selected in the NavController.
 *
 * @param route The route to check against the current navigation route
 * @return True if the provided route matches the current route, false otherwise
 */
@Composable
fun NavController.isCurrentRoute(route: String): Boolean {
    return this.currentBackStackEntryAsState().value?.destination?.route == route
}
