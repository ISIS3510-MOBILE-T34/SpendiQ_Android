package com.isis3510.spendiq.views.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.isis3510.spendiq.model.data.Account
import com.isis3510.spendiq.views.common.BottomNavigation
import com.isis3510.spendiq.views.transaction.AddTransactionModal
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.TransactionViewModel

/**
 * AccountsScreen composable function
 *
 * Displays a list of user accounts and provides functionality to edit or delete accounts.
 * Users can view their existing accounts, create new ones, or delete existing ones.
 * The screen also includes navigation to transaction details for each account.
 *
 * Key Features:
 * - Account Listing: Displays the current accounts with details such as account name, type, and balance.
 * - Search Functionality: Allows users to search for specific accounts.
 * - Modal Dialogs: Provides interfaces for creating and deleting accounts.
 * - Integration with ViewModel: Fetches account data and manages UI state through the provided ViewModel.
 *
 * UI Structure:
 * - Scaffold with a BottomNavigation for navigation between sections of the app.
 * - LazyColumn for displaying a list of accounts.
 * - Buttons for editing accounts and adding transactions.
 * - Modal dialog for editing accounts with options for creation and deletion.
 *
 * Supporting Components:
 * - `AccountItem`: A composable for displaying individual account details.
 * - `EditAccountModal`: A composable modal for managing account editing, including creation and deletion.
 *
 * @param navController [NavController] to navigate between screens.
 * @param accountViewModel [AccountViewModel] for managing account-related data.
 * @param transactionViewModel [TransactionViewModel] for managing transactions.
 */
@Composable
fun AccountsScreen(
    navController: NavController,
    accountViewModel: AccountViewModel,
    transactionViewModel: TransactionViewModel
) {
    // Collect accounts and transactions
    val accounts by accountViewModel.accounts.collectAsState()
    val transactions by transactionViewModel.transactions.collectAsState()
    val uiState by accountViewModel.uiState.collectAsState()

    // State variables for modals
    var showEditModal by remember { mutableStateOf(false) }
    var showAddTransactionModal by remember { mutableStateOf(false) }

    // Calculate the account with the most transactions
    val accountWithMostTransactions = remember(transactions, accounts) {
        transactions.groupBy { it.accountId }
            .mapValues { (_, txns) -> txns.size }
            .maxByOrNull { it.value }?.key?.let { accountId ->
                accounts.find { it.name == accountId || it.id == accountId }
            }
    }

    // Fetch accounts on load
    LaunchedEffect(Unit) {
        accountViewModel.fetchAccounts()
    }

    Scaffold(
        bottomBar = {
            BottomNavigation(
                navController = navController,
                transactionViewModel = transactionViewModel,
                accountViewModel = accountViewModel
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("mapScreen") },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(imageVector = Icons.Filled.LocationOn, contentDescription = "Go to Map")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                "Accounts",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "These are your current accounts",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Show account with the most transactions
            if (accountWithMostTransactions != null) {
                Text(
                    text = "Account with the most transactions: ${accountWithMostTransactions.name}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Blue,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else if (accounts.isNotEmpty()) {
                Text(
                    text = "No transactions available to determine the busiest account.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Show accounts
            when (uiState) {
                is AccountViewModel.UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is AccountViewModel.UiState.Success -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(accounts) { account ->
                            AccountItem(account, navController)
                        }
                    }
                }
                is AccountViewModel.UiState.Error -> {
                    Text(
                        text = (uiState as AccountViewModel.UiState.Error).message,
                        color = Color.Red
                    )
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { showEditModal = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Accounts", color = Color.White)
            }
        }
    }

    // Show Edit Account Modal
    if (showEditModal) {
        EditAccountModal(
            existingAccounts = accounts,
            onDismiss = { showEditModal = false },
            onCreateAccount = { accountType ->
                accountViewModel.createAccount(accountType)
            },
            onDeleteAccount = { accountType ->
                accountViewModel.deleteAccount(accountType)
            }
        )
    }

    // Show Add Transaction Modal
    if (showAddTransactionModal) {
        AddTransactionModal(
            accountViewModel = accountViewModel,
            transactionViewModel = transactionViewModel,
            onDismiss = { showAddTransactionModal = false },
            onTransactionAdded = {
                showAddTransactionModal = false
                accountViewModel.fetchAccounts()
            }
        )
    }
}




/**
 * AccountItem composable function
 *
 * Displays the details of an individual account, including its name, type, and balance.
 * Users can click on the item to navigate to a detailed view of transactions related to that account.
 *
 * @param account [Account] the account data to display.
 * @param navController [NavController] to navigate to transaction details for the account.
 */
@Composable
fun AccountItem(account: Account, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { navController.navigate("accountTransactions/${account.name}") }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(account.color)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = account.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = account.type,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            Text(
                text = "$ ${account.amount}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

/**
 * EditAccountModal composable function
 *
 * Provides a modal dialog for editing existing accounts or creating new ones.
 * Allows users to select account types and choose to delete existing accounts.
 *
 * @param existingAccounts List of [Account] currently available.
 * @param onDismiss Function to call when the modal is dismissed.
 * @param onCreateAccount Function to call to create a new account.
 * @param onDeleteAccount Function to call to delete an existing account.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountModal(
    existingAccounts: List<Account>,
    onDismiss: () -> Unit,
    onCreateAccount: (String) -> Unit,
    onDeleteAccount: (String) -> Unit
) {
    var selectedAccountType by remember { mutableStateOf("") }
    var selectedAction by remember { mutableStateOf("") }
    var expandedAccountType by remember { mutableStateOf(false) }
    var expandedAction by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Filter available account types
    val availableAccountTypes = listOf("Nu", "Bancolombia", "Nequi")
        .filter { accountType -> existingAccounts.none { it.name == accountType } }

    // Define actions based on available account types
    val actions = if (availableAccountTypes.isEmpty()) listOf("Delete") else listOf("Create", "Delete")

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Edit Accounts", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown for selecting action (Create/Delete)
            ExposedDropdownMenuBox(
                expanded = expandedAction,
                onExpandedChange = { expandedAction = !expandedAction }
            ) {
                TextField(
                    value = selectedAction,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Action") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAction) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedAction,
                    onDismissRequest = { expandedAction = false }
                ) {
                    actions.forEach { action ->
                        DropdownMenuItem(
                            text = { Text(action) },
                            onClick = {
                                selectedAction = action
                                expandedAction = false
                                selectedAccountType = ""
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dropdown for selecting account type based on the selected action
            if (selectedAction.isNotEmpty()) {
                val applicableAccountTypes = if (selectedAction == "Create") availableAccountTypes else existingAccounts.map { it.name }

                ExposedDropdownMenuBox(
                    expanded = expandedAccountType,
                    onExpandedChange = { expandedAccountType = !expandedAccountType }
                ) {
                    TextField(
                        value = selectedAccountType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Account Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccountType) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAccountType,
                        onDismissRequest = { expandedAccountType = false }
                    ) {
                        applicableAccountTypes.forEach { accountType ->
                            DropdownMenuItem(
                                text = { Text(accountType) },
                                onClick = {
                                    selectedAccountType = accountType
                                    expandedAccountType = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Button to confirm the selected action
            Button(
                onClick = {
                    if (selectedAction == "Delete") {
                        showDeleteConfirmation = true
                    } else {
                        onCreateAccount(selectedAccountType)
                        onDismiss()
                    }
                },
                enabled = selectedAccountType.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedAction, color = Color.White)
            }
        }
    }

    // Confirmation dialog for account deletion
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to delete the account?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAccount(selectedAccountType)
                        showDeleteConfirmation = false
                        onDismiss()
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("No")
                }
            }
        )
    }
}
