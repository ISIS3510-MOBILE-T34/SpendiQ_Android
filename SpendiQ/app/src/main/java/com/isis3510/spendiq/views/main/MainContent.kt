package com.isis3510.spendiq.views.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.isis3510.spendiq.model.data.Account
import com.isis3510.spendiq.model.data.Transaction
import com.isis3510.spendiq.model.data.Offer
import com.isis3510.spendiq.views.common.BottomNavigation
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.OffersViewModel
import com.google.firebase.Timestamp
import com.isis3510.spendiq.viewmodel.TransactionViewModel
import com.isis3510.spendiq.views.common.CreatePieChart
import java.text.SimpleDateFormat
import java.util.*

/**
 * MainContent composable function
 *
 * Provides a comprehensive dashboard view for the user's financial data. This main screen offers
 * an overview of the user's accounts, a summary of monthly income and expenses, and current
 * promotions. Users can interact with their financial data by viewing account-specific transactions,
 * accessing relevant promotions, and adding new transactions through a modal interface.
 *
 * Key Features:
 * - Financial Summary: Shows the current available balance along with an introductory summary.
 * - Accounts List: Displays the user’s accounts, each represented by a clickable card that
 *   navigates to transaction details.
 * - Income/Expense Chart: Visualizes monthly income and expenses using a pie chart.
 * - Promotions: Highlights nearby promotions, allowing navigation to further promotional details.
 * - Add Transaction Modal: A modal dialog for adding new transactions, including account selection,
 *   amount entry, transaction type, and date selection.
 *
 * UI Structure:
 * - Scaffold with a BottomNavigation bar for seamless navigation across app sections.
 * - LazyColumn that organizes:
 *   - A summary section with the date, current available money, and accounts.
 *   - Income and expense visualization in a pie chart.
 *   - Promotions section with clickable offers.
 * - AddTransactionModal: A dialog for adding transactions, featuring various input fields.
 *
 * Supporting Components:
 * - `AccountItem`: A composable for displaying individual account details within a clickable card.
 * - `PromoItem`: A composable for rendering promotion details.
 * - `AddTransactionModal`: A modal dialog for entering details for a new transaction.
 *
 * @param navController [NavController] to navigate between different screens within the app.
 * @param accountViewModel [AccountViewModel] provides and manages account-related data.
 * @param promoViewModel [OffersViewModel] provides data about available promotions.
 * @param transactionViewModel [TransactionViewModel] manages transaction data and actions.
 */

@Composable
fun MainContent(
    navController: NavController,
    accountViewModel: AccountViewModel,
    promoViewModel: OffersViewModel,
    transactionViewModel: TransactionViewModel,
) {
    // State collection for financial data
    val accounts by accountViewModel.accounts.collectAsState() // List of user accounts
    val promos by promoViewModel.offers.collectAsState() // List of promotions
    val currentMoney by accountViewModel.currentMoney.collectAsState() // Available balance
    var showAddTransactionModal by remember { mutableStateOf(false) } // Control for transaction modal
    val uiState by transactionViewModel.uiState.collectAsState() // UI state for transactions
    val transactions by transactionViewModel.transactions.collectAsState() // Transaction list

    // Calculate total income and expenses from transactions
    val (totalIncome, totalExpenses) = remember(transactions) {
        transactionViewModel.getIncomeAndExpenses()
    }

    // Load necessary data when the composable first appears
    LaunchedEffect(Unit) {
        accountViewModel.fetchAccounts()
        promoViewModel.fetchOffers()
        transactionViewModel.fetchAllTransactions()
    }

    // Main layout with bottom navigation bar
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
            // Financial Summary Section
            item {
                Text(
                    text = SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date()),
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
            }

            // Accounts List Section
            item {
                Text(
                    text = "Accounts",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Account items displayed within clickable cards
            items(accounts) { account ->
                AccountItem(account, navController)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Monthly Income and Expenses Chart
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Monthly Income/Expenses", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                // Show loading state or chart with income/expenses breakdown
                if (uiState is TransactionViewModel.UiState.Loading) {
                    CircularProgressIndicator()
                } else {
                    if (totalIncome > 0 || totalExpenses > 0) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            CreatePieChart(data = listOf("Income" to totalIncome, "Expenses" to totalExpenses))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                // Income display
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Income Icon",
                                    tint = Color(0xffb3cb54)
                                )
                                Text(
                                    text = "$ $totalIncome",
                                    color = Color(0xffb3cb54),
                                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                // Expenses display
                                Text(
                                    text = "$ $totalExpenses",
                                    color = Color(0xffc33ba5),
                                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expenses Icon",
                                    tint = Color(0xffc33ba5)
                                )
                            }
                        }
                    } else {
                        Text("You don't have any transactions.")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Save with these promotions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Promotions Section
            items(promos.take(3)) { promo ->
                PromoItem(promo) {}
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Button to view more promotions
            item {
                Button(
                    onClick = { navController.navigate("promos") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("See More Promotions")
                }
            }
        }

        // Modal for adding a new transaction
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

/**
 * Displays an account in a card, with account name, type, and balance.
 * The card navigates to transaction details for the account when clicked.
 *
 * @param account [Account] object containing account details
 * @param navController [NavController] to navigate to account transaction details
 */
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

/**
 * Displays a promotion item in a card with details such as place name, description,
 * recommendation reason, and an optional shop image.
 *
 * @param promo [Offer] object containing promotion details
 * @param onClick Lambda function called when the promotion is clicked
 */
@Composable
fun PromoItem(promo: Offer, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            promo.placeName?.let { Text(it, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(4.dp))
            promo.offerDescription?.let { Text(it, fontSize = 14.sp) }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Recommended: ${promo.recommendationReason}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            promo.shopImage?.let {
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = "Shop Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }
        }
    }
}

/**
 * Displays a modal bottom sheet for adding a new transaction, with fields for amount,
 * name, date, transaction type, and associated account.
 *
 * @param accountViewModel [AccountViewModel] provides account data for transaction association
 * @param transactionViewModel [TransactionViewModel] manages transaction data and submission
 * @param accounts List of [Account] objects available for association with the transaction
 * @param onDismiss Lambda function to close the modal
 * @param onTransactionAdded Lambda function to perform additional actions after adding the transaction
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionModal(
    accountViewModel: AccountViewModel,
    transactionViewModel: TransactionViewModel,
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onTransactionAdded: () -> Unit
) {
    // Modal state variables for transaction fields
    var amount by remember { mutableStateOf("") }
    var transactionName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Timestamp.now()) }
    var selectedTransactionType by remember { mutableStateOf("Expense") }
    var expandedTransactionType by remember { mutableStateOf(false) }
    var selectedAccountType by remember { mutableStateOf("Nu") }
    var expandedAccountType by remember { mutableStateOf(false) }

    // Date picker dialog setup
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

    // Modal bottom sheet UI for transaction entry
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

            // Amount input field
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { char -> char.isDigit() } },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Transaction name input field
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = transactionName,
                onValueChange = { transactionName = it },
                label = { Text("Transaction Name") },
                modifier = Modifier.fillMaxWidth()
            )

            // Date selection button
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { datePickerDialog.show() }) {
                Text("Select Date: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.toDate())}")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dropdown menu for transaction type
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

            // Dropdown menu for account selection
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

            // Add transaction button
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
