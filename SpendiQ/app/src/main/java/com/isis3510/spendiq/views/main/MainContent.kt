package com.isis3510.spendiq.views.main

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.isis3510.spendiq.model.data.Account
import com.isis3510.spendiq.model.data.Transaction
import com.isis3510.spendiq.views.common.BottomNavigation
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.AuthViewModel
import com.isis3510.spendiq.viewmodel.TransactionViewModel
import com.isis3510.spendiq.R
import com.isis3510.spendiq.views.common.CreatePieChart
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.ZeroLineProperties
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

fun saveIsMoneyVisible(context: Context, isVisible: Boolean) {
    val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    sharedPreferences.edit().putBoolean("isMoneyVisible", isVisible).apply()
}

fun getIsMoneyVisible(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean("isMoneyVisible", true)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainContent(
    navController: NavController,
    authViewModel: AuthViewModel,
    accountViewModel: AccountViewModel,
    transactionViewModel: TransactionViewModel,
) {
    val accounts by accountViewModel.accounts.collectAsState()
    val currentMoney by accountViewModel.currentMoney.collectAsState()
    var showAddTransactionModal by remember { mutableStateOf(false) }
    val uiState by transactionViewModel.uiState.collectAsState()
    val transactions by transactionViewModel.transactions.collectAsState()
    val context = LocalContext.current
    var isMoneyVisible by remember { mutableStateOf(getIsMoneyVisible(context)) }

    val (totalIncome, totalExpenses) = remember(transactions) {
        transactionViewModel.getIncomeAndExpenses()
    }
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

    LaunchedEffect(isMoneyVisible) {
        saveIsMoneyVisible(context, isMoneyVisible)
    }

    LaunchedEffect(Unit) {
        accountViewModel.fetchAccounts()
        transactionViewModel.fetchAllTransactions()
    }

    Scaffold(
        bottomBar = {
            BottomNavigation(
                navController = navController,
                transactionViewModel,
                accountViewModel
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date()),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp)
                )
                Text(
                    text = "Summary",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium)
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "Take a look at your finances",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp)
                )

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "Current available money",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = if (isMoneyVisible) currencyFormatter.format(currentMoney) else "******",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(
                        onClick = { isMoneyVisible = !isMoneyVisible } // Cambiar visibilidad y guardar automáticamente
                    ) {
                        Icon(
                            painter = painterResource(id = if (isMoneyVisible) R.drawable.round_visibility_24 else R.drawable.baseline_visibility_off_24),
                            contentDescription = if (isMoneyVisible) "Hide money" else "Show money"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text(
                    text = "Accounts",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(accounts) { account ->
                AccountItem(account, navController)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("User Balance Over Last 30 Days", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState is TransactionViewModel.UiState.Loading) {
                    CircularProgressIndicator()
                } else {
                    if (totalIncome > 0 || totalExpenses > 0) {
                        val dailyTransactions = transactionViewModel.getIncomeAndExpensesLast30Days()

                        val movements = dailyTransactions.map { it.amount }
                        val moveLabels = dailyTransactions.map { it.day }

                        LineChart(
                            modifier = Modifier
                                .height(300.dp)
                                .fillMaxSize().padding(horizontal = 22.dp),
                            data = remember {
                                listOf(
                                    Line(
                                        label = "Movements",
                                        values = movements,
                                        color = SolidColor(Color(0xffb3cb54)),
                                        firstGradientFillColor = Color(0xFF94B719).copy(alpha = .5f),
                                        secondGradientFillColor = Color.Transparent,
                                        strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                                        drawStyle = DrawStyle.Stroke(width = 2.dp),
                                        dotProperties = DotProperties(
                                            enabled = true,
                                            color = SolidColor(Color.White),
                                            strokeWidth = 2.dp,
                                            radius = 3.5.dp,
                                            strokeColor = SolidColor(Color(0xFF94B719)),
                                        )
                                    )
                                )
                            },
                            labelProperties = LabelProperties(
                                enabled = true,
                                textStyle = MaterialTheme.typography.labelSmall,
                                padding = 16.dp,
                                labels = moveLabels ),
                            curvedEdges = false,
                            zeroLineProperties = ZeroLineProperties(
                                enabled = true,
                                color = SolidColor(Color(0xffc33ba5)),
                                thickness = 1.5.dp
                            ),
                            animationMode = AnimationMode.Together(delayBuilder = {
                                it * 500L
                            }),
                        )
                    } else {
                        Text("You don't have any transactions.")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (showAddTransactionModal) {
            AddTransactionModal(
                accountViewModel = accountViewModel,
                transactionViewModel,
                accounts = accounts,
                onDismiss = { showAddTransactionModal = false },
                onTransactionAdded = {
                    showAddTransactionModal = false
                    accountViewModel.fetchAccounts()
                }
            )
        }
    }
}

@Composable
fun AccountItem(account: Account, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("accountTransactions/${account.name}") },
        colors = CardDefaults.cardColors(containerColor = account.color)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = account.name,
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = account.type,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$ ${account.amount}",
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionModal(
    accountViewModel: AccountViewModel,
    transactionViewModel: TransactionViewModel,
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onTransactionAdded: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var transactionName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Timestamp.now()) }
    var selectedTransactionType by remember { mutableStateOf("Expense") }
    var expandedTransactionType by remember { mutableStateOf(false) }
    var selectedAccountType by remember { mutableStateOf("Nu") }
    var expandedAccountType by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = Timestamp(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Add Transaction", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { char -> char.isDigit() } },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = transactionName,
                onValueChange = { transactionName = it },
                label = { Text("Transaction Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { datePickerDialog.show() }) {
                Text("Select Date: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.toDate())}")
            }

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expandedTransactionType,
                onExpandedChange = { expandedTransactionType = !expandedTransactionType }
            ) {
                TextField(
                    value = selectedTransactionType,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTransactionType) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedTransactionType,
                    onDismissRequest = { expandedTransactionType = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Income") },
                        onClick = {
                            selectedTransactionType = "Income"
                            expandedTransactionType = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Expense") },
                        onClick = {
                            selectedTransactionType = "Expense"
                            expandedTransactionType = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expandedAccountType,
                onExpandedChange = { expandedAccountType = !expandedAccountType }
            ) {
                TextField(
                    value = selectedAccountType,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccountType) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedAccountType,
                    onDismissRequest = { expandedAccountType = false }
                ) {
                    accounts.forEach { account ->
                        DropdownMenuItem(
                            text = { Text(account.name) },
                            onClick = {
                                selectedAccountType = account.name
                                expandedAccountType = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val transaction = Transaction(
                        id = "",
                        accountId = selectedAccountType,
                        transactionName = transactionName,
                        amount = amount.toLongOrNull() ?: 0L,
                        dateTime = selectedDate,
                        transactionType = selectedTransactionType,
                        location = null
                    )
                    transactionViewModel.addTransactionWithAccountCheck(transaction)
                    onTransactionAdded()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Transaction")
            }
        }
    }
}
