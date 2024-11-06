// ProfileStatisticsScreen.kt
package com.isis3510.spendiq.views.profile

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
                        data = accountBalances.toMap(),
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
    val step = 10000.0
    val maxPositiveAmount = data.values.filter { it > 0 }.maxOrNull() ?: 0.0
    val maxNegativeAmount = data.values.filter { it < 0 }.minOrNull() ?: 0.0
    val maxAmount = maxOf(maxPositiveAmount, abs(maxNegativeAmount))
    val numberOfSteps = if (maxAmount % step == 0.0) (maxAmount / step).toInt() else (maxAmount / step).toInt() + 1

    val barColors = if (isDarkTheme) {
        listOf(Color.Cyan, Color.Magenta)
    } else {
        listOf(Color(0xFFB3CB54), Color(0xFFE57373))
    }

    val sortedData = data.toList().sortedBy { it.first }

    Canvas(modifier = modifier) {
        val paddingBottom = 40.dp.toPx() // Reducido para acomodar etiquetas
        val paddingTop = 20.dp.toPx()
        val paddingStart = 40.dp.toPx()
        val paddingEnd = 20.dp.toPx()

        val availableHeight = size.height - paddingTop - paddingBottom
        val availableWidth = size.width - paddingStart - paddingEnd

        val centerY = paddingTop + (availableHeight / 2)

        val barWidth = availableWidth / (sortedData.size * 2)
        var xPosition = paddingStart + barWidth / 2

        // Dibujar líneas de referencia horizontales
        for (i in 1..numberOfSteps) {
            val y = centerY - (i * step / maxAmount * availableHeight)
            drawLine(
                color = Color.LightGray,
                start = Offset(paddingStart, y.toFloat()),
                end = Offset(size.width - paddingEnd, y.toFloat()),
                strokeWidth = 1f
            )
        }

        // Dibujar eje X
        drawLine(
            color = Color.Black,
            start = Offset(paddingStart, centerY),
            end = Offset(size.width - paddingEnd, centerY),
            strokeWidth = 2f
        )


        // Dibujar barras
        sortedData.forEach { (accountName, amount) ->
            val barHeightRatio = if (maxAmount != 0.0) abs(amount) / maxAmount else 0.0
            val barHeight = barHeightRatio.toFloat() * availableHeight

            val isPositive = amount >= 0.0
            val topLeftY = if (isPositive) centerY - barHeight else centerY

            drawRoundRect(
                color = if (isPositive) barColors[0] else barColors[1],
                topLeft = Offset(xPosition, topLeftY),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
            )

            // Dibujar etiquetas de las cuentas justo debajo del eje X dentro del Canvas
            drawIntoCanvas { canvas ->
                val paint = AndroidPaint().apply {
                    color = android.graphics.Color.BLACK
                    textAlign = AndroidPaint.Align.CENTER
                    textSize = 12.sp.toPx()
                    isAntiAlias = true
                }
                canvas.nativeCanvas.drawText(
                    accountName,
                    xPosition + barWidth / 2,
                    centerY + 25.dp.toPx(), // Aumentado para mayor separación del eje X
                    paint
                )
            }

            // Dibujar línea delgada gris debajo del eje X hasta el final del gráfico
            drawLine(
                color = Color.LightGray,
                start = Offset(xPosition, centerY + 2.dp.toPx()), // Un poco debajo del eje X
                end = Offset(xPosition + barWidth, centerY + 2.dp.toPx()),
                strokeWidth = 1f
            )

            xPosition += barWidth * 2
        }

        // **Agregar una línea delgada gris al final del eje X**
        drawLine(
            color = Color.LightGray,
            start = Offset(paddingStart, size.height - paddingBottom),
            end = Offset(size.width - paddingEnd, size.height - paddingBottom),
            strokeWidth = 1f
        )
    }
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
