package com.isis3510.spendiq.view.accounts  // Ensure this matches the actual directory structure

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.isis3510.spendiq.model.data.Account
import com.isis3510.spendiq.views.common.BottomNavigation
import com.isis3510.spendiq.views.transaction.AddTransactionModal
import com.isis3510.spendiq.viewmodel.AccountViewModel

@Composable
fun AccountsScreen(navController: NavController, accountViewModel: AccountViewModel) {  // Make sure the parameter name is consistent
    val accounts by accountViewModel.accounts.collectAsState()
    val uiState by accountViewModel.uiState.collectAsState()
    var showEditModal by remember { mutableStateOf(false) }
    var showAddTransactionModal by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        accountViewModel.fetchAccounts()
    }

    Scaffold(
        bottomBar = {
            BottomNavigation(
                navController = navController,
                accountViewModel)
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
                else -> {} // Idle state, do nothing
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { showEditModal = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Accounts")
            }
        }
    }

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

    if (showAddTransactionModal) {
        AddTransactionModal(
            accountViewModel = accountViewModel,
            onDismiss = { showAddTransactionModal = false },
            onTransactionAdded = {
                showAddTransactionModal = false
                accountViewModel.fetchAccounts()
            }
        )
    }
}

@Composable
fun AccountItem(account: Account, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        onClick = { navController.navigate("accountTransactions/${account.name}") }
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

    val availableAccountTypes = listOf("Nu", "Bancolombia", "Nequi")
        .filter { accountType -> existingAccounts.none { it.name == accountType } }

    val actions = if (availableAccountTypes.isEmpty()) listOf("Delete") else listOf("Create", "Delete")

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Edit Accounts", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

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
                Text(selectedAction)
            }
        }
    }

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
