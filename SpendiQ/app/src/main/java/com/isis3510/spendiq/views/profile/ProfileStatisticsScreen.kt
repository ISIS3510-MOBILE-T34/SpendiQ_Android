// ProfileStatisticsScreen.kt
package com.isis3510.spendiq.views.profile

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.isis3510.spendiq.R
import com.isis3510.spendiq.views.common.BottomNavigation
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.TransactionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.isis3510.spendiq.model.data.Account
import com.isis3510.spendiq.model.data.Transaction
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.LabelProperties
import kotlin.math.abs
import android.graphics.Paint as AndroidPaint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileStatisticsScreen(
    navController: NavController,
    transactionViewModel: TransactionViewModel,
    accountViewModel: AccountViewModel
) {
    val isDarkTheme = isSystemInDarkTheme()
    val cardBackgroundColor = Color(0xFFB3CB54)
    val textColor = if (isDarkTheme) Color.White else Color.Black

    var isNavigating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val accounts by accountViewModel.accounts.collectAsState()
    val transactions by transactionViewModel.transactions.collectAsState()

    val accountBalances = remember { mutableStateMapOf<String, Double>() }

    LaunchedEffect(Unit) {
        transactionViewModel.fetchAllTransactions()
    }

    LaunchedEffect(accounts, transactions) {
        val balances = mutableMapOf<String, Double>()
        accounts.forEach { account ->
            val transactionsForAccount = transactions.filter { it.accountId == account.id }
            val total = transactionsForAccount.sumOf {
                if (it.transactionType == "Income") it.amount else -it.amount
            }
            balances[account.name] = total.toDouble()
        }
        accountBalances.clear()
        accountBalances.putAll(balances)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!isNavigating) {
                            isNavigating = true
                            coroutineScope.launch {
                                navController.popBackStack()
                                delay(300)
                                isNavigating = false
                            }
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.round_arrow_back_ios_24),
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
                .padding(horizontal = 16.dp) // Aumentado para mejor espaciado
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botones Segmented "Daily" y "Weekly"
            SegmentedButton(
                options = listOf("Daily", "Weekly"),
                selectedOption = "Daily",
                onOptionSelected = {},
                backgroundColor = Color.Transparent,
                selectedColor = cardBackgroundColor,
                textColor = textColor
            )

            // Espacio adicional para bajar el gráfico (aprox. 50 dp)
            Spacer(modifier = Modifier.height(50.dp))

            // Gráfico de Barras
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp) // Ajusta la altura según sea necesario
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                if (accountBalances.isNotEmpty()) {
                    BarChart(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        data = accountBalances,
                        isDarkTheme = isDarkTheme
                    )
                } else {
                    Text("Cargando datos...", color = textColor)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Etiquetas de las cuentas justo debajo del eje X
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                sortedAccounts(accountBalances).forEach { accountName ->
                    Text(
                        text = accountName,
                        fontSize = 12.sp,
                        color = textColor,
                        maxLines = 1,
                        modifier = Modifier
                            .widthIn(min = 40.dp, max = 80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                        title = "Sept. 3",
                        subtitle = "Last advice",
                        backgroundColor = cardBackgroundColor,
                        textColor = textColor,
                        iconResId = R.drawable.round_lightbulb_24
                    )
                    SummaryCard(
                        title = "El Corral",
                        subtitle = "Most visited place",
                        backgroundColor = cardBackgroundColor,
                        textColor = textColor,
                        iconResId = R.drawable.round_star_24
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
                        textColor = textColor,
                        iconResId = R.drawable.round_money_24
                    )
                    SummaryCard(
                        title = "Nequi",
                        subtitle = "Preferred payment account",
                        backgroundColor = cardBackgroundColor,
                        textColor = textColor,
                        iconResId = R.drawable.outline_giftcard_24
                    )
                }
            }
        }
    }
}

// Función auxiliar para ordenar las cuentas (si es necesario)
@Composable
fun sortedAccounts(accountBalances: Map<String, Double>): List<String> {
    return accountBalances.keys.sorted()
}

@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    data: Map<String, Double>,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    // Preparar los datos para la gráfica
    val barsData = remember(data) {
        listOf(
            Bars(
                label = "Balances",
                values = data.map { (accountName, amount) ->
                    Bars.Data(
                        label = accountName,
                        value = amount,
                        color = if (accountName ==  "Nequi") SolidColor(Color(0xFFda0081)) else if
                                (accountName == "Bancolombia") SolidColor(Color(0xFFFDDA24)) else if
                                (accountName ==  "Nu") SolidColor(Color(0xFF820ad1)) else if
                                (accountName == "LuloBank") SolidColor(Color(0xFFe8ff00)) else if
                                (accountName == "Davivienda") SolidColor(Color(0xFFed1c27)) else if
                                (accountName == "Banco de Bogotá") SolidColor(Color(0xFF00317e)) else
                                SolidColor(Color(0xFFED0722))
                    )
                }
            )
        )
    }

    // Configuración de propiedades de la gráfica
    val barProperties = BarProperties(
        cornerRadius = Bars.Data.Radius.Rectangle(topRight = 6.dp, topLeft = 6.dp),
        spacing = 20.dp,
        thickness = 40.dp
    )

    // Configuración de etiquetas
    val labelProperties = LabelProperties(
        enabled = true,
        textStyle = MaterialTheme.typography.labelSmall,
        padding = 16.dp,
        labels = data.keys.toList() // Usar los nombres de las cuentas como etiquetas
    )

    // Renderizar la gráfica
    ColumnChart(
        modifier = modifier,
        data = barsData,
        barProperties = barProperties,
        labelProperties = labelProperties,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
}

@Composable
fun SummaryCard(
    title: String,
    subtitle: String,
    backgroundColor: Color,
    textColor: Color,
    iconResId: Int? = null
) {
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
                        .size(48.dp)
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