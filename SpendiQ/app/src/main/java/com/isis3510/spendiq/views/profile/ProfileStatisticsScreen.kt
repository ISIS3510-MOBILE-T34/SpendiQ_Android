package com.isis3510.spendiq.views.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.isis3510.spendiq.views.common.BottomNavigation
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileStatisticsScreen(navController: NavController, transactionViewModel: TransactionViewModel, accountViewModel: AccountViewModel) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color.DarkGray else Color(0xFFEEEEEE)
    val cardBackgroundColor = Color(0xFFB3CB54)
    val textColor =  Color.Black

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("profile") { launchSingleTop = true } }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Toggle for Daily/Weekly
            Surface(
                modifier = Modifier.padding(vertical = 16.dp),
                shape = RoundedCornerShape(50),
                color = backgroundColor
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SegmentedButton(
                        options = listOf("Daily", "Weekly"),
                        selectedOption = "Daily",
                        onOptionSelected = { /* Handle selection */ },
                        backgroundColor = backgroundColor,
                        selectedColor = cardBackgroundColor,
                        textColor = textColor
                    )
                }
            }

            // Placeholder for Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(if (isDarkTheme) Color.Gray else Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text("Chart Placeholder", color = textColor)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryCard(title = "8 PM", subtitle = "Highest spending time", backgroundColor = cardBackgroundColor, textColor = textColor)
                    SummaryCard(title = "Saturday", subtitle = "Highest spending day", backgroundColor = cardBackgroundColor, textColor = textColor)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryCard(title = "September 3", subtitle = "Last advice", backgroundColor = cardBackgroundColor, textColor = textColor)
                    SummaryCard(title = "El Corral", subtitle = "Most visited place", backgroundColor = cardBackgroundColor, textColor = textColor)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryCard(title = "$67,500", subtitle = "Highest expend", backgroundColor = cardBackgroundColor, textColor = textColor)
                    SummaryCard(title = "Nequi", subtitle = "Preferred payment account", backgroundColor = cardBackgroundColor, textColor = textColor)
                }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, subtitle: String, backgroundColor: Color, textColor: Color) {
    Surface(
        modifier = Modifier
            .width(140.dp)
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = textColor)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = textColor)
        }
    }
}

@Composable
fun SegmentedButton(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    backgroundColor: Color,
    selectedColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .background(backgroundColor, shape = RoundedCornerShape(50))
            .padding(4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            Button(
                onClick = { onOptionSelected(option) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) selectedColor else backgroundColor)
            ) {
                Text(option, color = textColor)
            }
        }
    }
}
