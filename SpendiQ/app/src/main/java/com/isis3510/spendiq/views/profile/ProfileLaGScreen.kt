package com.isis3510.spendiq.views.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.isis3510.spendiq.R
import com.isis3510.spendiq.views.common.BottomNavigation
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileLaGScreen(
    navController: NavController,
    transactionViewModel: TransactionViewModel,
    accountViewModel: AccountViewModel
) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color.DarkGray else Color(0xFFEEEEEE)
    val textColor = if (isDarkTheme) Color.White else Color.Black
    
    // Checkbox states
    var byExpenseChecked by remember { mutableStateOf(true) }
    var byQuantityChecked by remember { mutableStateOf(false) }

    // Lista de gastos
    var expenses by remember { mutableStateOf(mutableListOf<Expense>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Limits & Goals", fontWeight = FontWeight.Bold, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // By Expense Section
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = byExpenseChecked,
                    onCheckedChange = {
                        byExpenseChecked = true
                        byQuantityChecked = false
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("By expense", style = MaterialTheme.typography.bodyLarge, color = textColor)
            }

            // Sección de gastos solo activa cuando 'By Expense' está marcado
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (byExpenseChecked) backgroundColor else backgroundColor.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(8.dp)
            ) {
                if (byExpenseChecked) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp) // Altura máxima de la lista de gastos
                    ) {
                        items(expenses) { expense ->
                            ExpenseCategoryCard(
                                expense = expense,
                                onValueChange = { newName, newAmount ->
                                    expense.name = newName
                                    expense.amount = newAmount
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp)) // Espaciado entre cada gasto
                        }
                    }

                    // Botón para añadir nuevo gasto, limitado a 4 elementos visibles en pantalla
                    if (expenses.size < 4) {
                        Spacer(modifier = Modifier.height(8.dp))

                    }

                }
                Button(
                    onClick = {
                        expenses = expenses.toMutableList().apply {
                            add(Expense("New Expense", "0"))
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCCCCCC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.add24),
                        contentDescription = "Add",
                        tint = Color(0xFF707070),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // By Quantity Section
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = byQuantityChecked,
                    onCheckedChange = {
                        byQuantityChecked = true
                        byExpenseChecked = false
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("By quantity", style = MaterialTheme.typography.bodyLarge, color = textColor)
            }

            // Sección de cantidad solo activa cuando 'By Quantity' está marcado
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (byQuantityChecked) backgroundColor else backgroundColor.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(8.dp)
            ) {
                if (byQuantityChecked) {
                    Slider(
                        value = 75f,
                        onValueChange = {},
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFB3CB54),
                            activeTrackColor = Color(0xFFB3CB54)
                        )
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("$0", fontSize = 14.sp, color = textColor)
                        Text("$100k", fontSize = 14.sp, color = textColor)
                    }
                }
            }
        }
    }
}

data class Expense(var name: String, var amount: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseCategoryCard(expense: Expense, onValueChange: (String, String) -> Unit) {
    var name by remember { mutableStateOf(expense.name) }
    var amount by remember { mutableStateOf(expense.amount) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp) // Altura fija para cada gasto
            .background(Color(0xFFD9D9D9), RoundedCornerShape(50)) // Fondo gris claro
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                onValueChange(it, amount)
            },
            placeholder = { Text("Expense Name", color = Color.Gray, fontSize = 16.sp) },
            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color.Black),
            modifier = Modifier
                .weight(1f)
                .background(Color.Transparent),
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = Color.Gray,
                cursorColor = Color.Black
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .width(80.dp)
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it.filter { char -> char.isDigit() }
                    onValueChange(name, amount)
                },
                placeholder = { Text("Amount", color = Color.Black) },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    cursorColor = Color.Black // Color del cursor

                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

