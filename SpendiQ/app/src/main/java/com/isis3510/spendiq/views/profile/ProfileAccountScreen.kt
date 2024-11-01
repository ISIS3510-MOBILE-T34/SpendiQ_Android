package com.isis3510.spendiq.views.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.isis3510.spendiq.R
import com.isis3510.spendiq.views.common.BottomNavigation
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAccountScreen(
    navController: NavController,
    userData: Map<String, Any?>?,
    transactionViewModel: TransactionViewModel,
    accountViewModel: AccountViewModel
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("profile") { launchSingleTop = true } }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Campos de perfil con fondo rosado en los iconos
            ProfileField("Full Name", (userData?.get("fullName") as? String) ?: "N/A", R.drawable.person24)
            ProfileField("Email Address", (userData?.get("email") as? String) ?: "N/A", R.drawable.email24)
            ProfileField("Phone Number", (userData?.get("phoneNumber") as? String) ?: "N/A", R.drawable.phone24)
            ProfileField("Birth Date", (userData?.get("birthDate") as? String) ?: "N/A", R.drawable.calendar24)
        }
    }
}

@Composable
fun ProfileField(label: String, value: String, iconResId: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFC33BA5), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}



