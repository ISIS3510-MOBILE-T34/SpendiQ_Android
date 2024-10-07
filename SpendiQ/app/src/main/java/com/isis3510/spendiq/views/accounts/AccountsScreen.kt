package com.isis3510.spendiq.views.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.spendiq.views.main.BottomNavigation
import com.isis3510.spendiq.views.transaction.AddTransactionModal
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(navController: NavController) {
    var accounts by remember { mutableStateOf<List<Account>>(emptyList()) }
    var showEditModal by remember { mutableStateOf(false) }
    var showAddTransactionModal by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            accounts = fetchAccounts()
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigation(
                navController = navController,
                onAddTransactionClick = { showAddTransactionModal = true }
            )
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
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(accounts) { account ->
                    AccountItem(account, navController)
                }
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
            onAccountChanged = {
                coroutineScope.launch {
                    accounts = fetchAccounts()
                }
            }
        )
    }

    if (showAddTransactionModal) {
        AddTransactionModal(
            onDismiss = { showAddTransactionModal = false },
            onTransactionAdded = {
                showAddTransactionModal = false
                coroutineScope.launch {
                    accounts = fetchAccounts() // Refresh accounts after adding a transaction
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    onAccountChanged: () -> Unit
) {
    var selectedAccountType by remember { mutableStateOf("") }
    var selectedAction by remember { mutableStateOf("") }
    var expandedAccountType by remember { mutableStateOf(false) }
    var expandedAction by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

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
                        coroutineScope.launch {
                            createAccount(selectedAccountType)
                            onAccountChanged()
                            onDismiss()
                        }
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
                        coroutineScope.launch {
                            deleteAccount(selectedAccountType)
                            onAccountChanged()
                            showDeleteConfirmation = false
                            onDismiss()
                        }
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

data class Account(
    val name: String,
    val type: String,
    val amount: Long,
    val color: Color
)

suspend fun fetchAccounts(): List<Account> {
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()

    val snapshot = firestore.collection("accounts")
        .whereEqualTo("user_id", userId)
        .get()
        .await()

    return snapshot.documents.mapNotNull { doc ->
        val name = doc.getString("name") ?: return@mapNotNull null
        val amount = doc.getLong("amount") ?: 0L
        val color = when (name) {
            "Nu" -> Color(0xFF9747FF)
            "Bancolombia" -> Color(0xFFFFCC00)
            "Nequi" -> Color(0xFF8B2F87)
            else -> Color.Gray
        }
        Account(name, "Debit", amount, color)
    }
}

suspend fun deleteAccount(accountType: String) {
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val documents = firestore.collection("accounts")
        .whereEqualTo("name", accountType)
        .whereEqualTo("user_id", userId)
        .get()
        .await()

    for (document in documents) {
        document.reference.delete().await()
    }
}

fun createAccount(accountType: String) {
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    firestore.collection("accounts").add(
        mapOf(
            "name" to accountType,
            "amount" to 0L,
            "user_id" to userId
        )
    )
}