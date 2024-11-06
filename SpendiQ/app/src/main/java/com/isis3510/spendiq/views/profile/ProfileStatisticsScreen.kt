// ProfileStatisticsScreen.kt
package com.isis3510.spendiq.views.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileStatisticsScreen(
    navController: NavController,
    transactionViewModel: TransactionViewModel,
    accountViewModel: AccountViewModel
) {
    // Detectar si está en modo oscuro
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color.DarkGray else Color(0xFFEEEEEE)
    val cardBackgroundColor = Color(0xFFB3CB54)
    val textColor = Color.Black

    // Estado para evitar múltiples clics rápidos en el botón de retroceso
    var isNavigating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!isNavigating) {
                            isNavigating = true
                            coroutineScope.launch {
                                navController.popBackStack()
                                delay(300) // Esperar 300 ms antes de permitir otro clic
                                isNavigating = false
                            }
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.round_arrow_back_ios_24),
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
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Toggle para Daily/Weekly
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

            // Placeholder para la gráfica
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

            // Tarjetas resumen
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryCard(
                        title = "8 PM",
                        subtitle = "Highest spending time",
                        backgroundColor = cardBackgroundColor,
                        textColor = textColor,
                        iconResId = R.drawable.round_clock_24
                    )
                    SummaryCard(
                        title = "Saturday",
                        subtitle = "Highest spending day",
                        backgroundColor = cardBackgroundColor,
                        textColor = textColor,
                        iconResId = R.drawable.calendar24
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryCard(
                        title = "September 3",
                        subtitle = "Last advice",
                        backgroundColor = cardBackgroundColor,
                        textColor = textColor
                    )
                    SummaryCard(
                        title = "El Corral",
                        subtitle = "Most visited place",
                        backgroundColor = cardBackgroundColor,
                        textColor = textColor
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryCard(
                        title = "$67,500",
                        subtitle = "Highest expend",
                        backgroundColor = cardBackgroundColor,
                        textColor = textColor
                    )
                    SummaryCard(
                        title = "Nequi",
                        subtitle = "Preferred payment account",
                        backgroundColor = cardBackgroundColor,
                        textColor = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, subtitle: String, backgroundColor: Color, textColor: Color, iconResId: Int? = null) {
    Surface(
        modifier = Modifier
            .width(140.dp)
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            iconResId?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier
                        .size(48.dp)  // Tamaño mayor para mayor visibilidad
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                )
            }
            Column(
                modifier = Modifier
                    .padding(start = 12.dp, top = 12.dp, end = 8.dp, bottom = 12.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = textColor)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = textColor)
            }
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
