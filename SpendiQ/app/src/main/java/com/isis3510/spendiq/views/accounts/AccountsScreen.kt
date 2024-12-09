// AccountsScreen.kt
package com.isis3510.spendiq.views.accounts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import com.isis3510.spendiq.viewmodel.ConnectivityViewModel
import com.isis3510.spendiq.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.util.Locale

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
 * - Buttons for editing accounts y adding transactions.
 * - Modal dialog for editing accounts with options for creation and deletion.
 *
 * Supporting Components:
 * - `AccountItem`: A composable for displaying individual account details.
 * - `EditAccountModal`: A composable modal for managing account editing, including creation and deletion.
 *
 * @param navController [NavController] to navigate between screens.
 * @param accountViewModel [AccountViewModel] for managing account-related data.
 * @param transactionViewModel [TransactionViewModel] for managing transactions.
 * @param connectivityViewModel [ConnectivityViewModel] para gestionar la conectividad.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    navController: NavController,
    accountViewModel: AccountViewModel,
    transactionViewModel: TransactionViewModel,
    connectivityViewModel: ConnectivityViewModel // Agregar ViewModel de conectividad
) {
    // Collect accounts and transactions
    val accounts by accountViewModel.accounts.collectAsState()
    val transactions by transactionViewModel.transactions.collectAsState()
    val uiState by accountViewModel.uiState.collectAsState()

    // State variables para modales
    var showEditModal by remember { mutableStateOf(false) }
    var showAddTransactionModal by remember { mutableStateOf(false) }

    // Calcular la cuenta con más transacciones
    val accountWithMostTransactions = remember(transactions, accounts) {
        transactions.groupBy { it.accountId }
            .mapValues { (_, txns) -> txns.size }
            .maxByOrNull { it.value }?.key?.let { accountId ->
                accounts.find { it.name == accountId || it.id == accountId }
            }
    }

    // Fetch accounts on load
    LaunchedEffect(Unit) {
        accountViewModel.observeAccounts()
    }

    // Observar el estado de conectividad
    val isNetworkAvailable by connectivityViewModel.isConnected.observeAsState(true)

    Scaffold(
        topBar = {
            Column {
                ConnectivityBanner(isConnected = isNetworkAvailable)
                TopAppBar(
                    title = {
                        Text(
                            "Accounts",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }
        },
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
                "These are your current accounts",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar cuenta con más transacciones
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

            // Mostrar cuentas
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

    // Mostrar Modal de Editar Cuenta
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

    // Mostrar Modal de Agregar Transacción
    if (showAddTransactionModal) {
        AddTransactionModal(
            accountViewModel = accountViewModel,
            transactionViewModel = transactionViewModel,
            onDismiss = { showAddTransactionModal = false },
            onTransactionAdded = {
                showAddTransactionModal = false
                accountViewModel.observeAccounts()
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

    val textColor = when (account.name) {
        "Lulo", "Bancolombia" -> Color.Black
        else -> Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { navController.navigate("accountTransactions/${account.name}") },
        colors = CardDefaults.cardColors(containerColor = account.color)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(account.color)
                .padding(16.dp)
        ) {
            // Mostrar solo el nombre y el tipo
            Column {
                Text(
                    text = account.name,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = account.type,
                    color = textColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            // Mostrar el monto al final
            Text(
                text = formatAmount(account.amount),
                color = textColor, // Color definido
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

fun formatAmount(amount: Long): String {
    val locale = Locale("es", "CO")
    val formatter = NumberFormat.getCurrencyInstance(locale)
    return formatter.format(amount)
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

    // Filtrar tipos de cuenta disponibles
    val availableAccountTypes = listOf("Nu", "Bancolombia", "Nequi")
        .filter { accountType -> existingAccounts.none { it.name == accountType } }

    // Definir acciones basadas en los tipos de cuenta disponibles
    val actions = if (availableAccountTypes.isEmpty()) listOf("Delete") else listOf("Create", "Delete")

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Edit Accounts", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown para seleccionar acción (Crear/Eliminar)
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

            // Dropdown para seleccionar tipo de cuenta basado en la acción seleccionada
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

            // Botón para confirmar la acción seleccionada
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

    // Diálogo de confirmación para eliminación de cuenta
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

@Composable
fun ConnectivityBanner(isConnected: Boolean) {
    AnimatedVisibility(
        visible = !isConnected,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(durationMillis = 300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(durationMillis = 300)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Red
        ) {
            Text(
                text = "You have no Internet connection! You will not see updates until the connection is restored",
                modifier = Modifier.padding(16.dp),
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
