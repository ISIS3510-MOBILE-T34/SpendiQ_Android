package com.isis3510.spendiq.views.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.isis3510.spendiq.R
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.LimitsViewModel
import com.isis3510.spendiq.viewmodel.TransactionViewModel
import com.isis3510.spendiq.views.common.BottomNavigation
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.launch
import com.isis3510.spendiq.model.local.database.LimitsEntity
import com.isis3510.spendiq.utils.isConnected
import com.isis3510.spendiq.utils.scheduleSyncWork
import com.isis3510.spendiq.model.local.database.ExpenseEntity
import com.isis3510.spendiq.viewmodel.LimitsViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel



class Expense(
    name: String = "",
    amount: String = ""
) {
    var name by mutableStateOf(name)
    var amount by mutableStateOf(amount)
}

class NumberFormatTransformation : VisualTransformation {
    private val formatter: NumberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        isGroupingUsed = true
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text.replace("[^\\d.]".toRegex(), "")
        val number = originalText.toDoubleOrNull()
        val formatted = if (number != null) formatter.format(number) else originalText

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = formatted.length
            override fun transformedToOriginal(offset: Int): Int = originalText.length
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileLaGScreen(
    navController: NavController,
    transactionViewModel: TransactionViewModel,
    accountViewModel: AccountViewModel
) {
    val context = LocalContext.current
    val limitsViewModel: LimitsViewModel = viewModel(
        factory = LimitsViewModelFactory(context)
    )
    val MAX_EXPENSES = 10
    val MAX_NAME_LENGTH = 50
    val MAX_AMOUNT_LENGTH = 8

    var isByExpenseChecked by remember { mutableStateOf(true) }
    var isByQuantityChecked by remember { mutableStateOf(false) }
    val expenses = remember { mutableStateListOf<Expense>() } // Lista vacía
    var selectedFrequency by remember { mutableStateOf("Daily") } // Unificar la variable de frecuencia
    var amountText by remember { mutableStateOf("") }
    val lightGrayColor = Color(0xFFD9D9D9)
    val selectedButtonColor = Color(0xFFB3CB54) // Color especificado para selección
    val selectedBorderColor = Color(0xFF00008B) // Azul oscuro para selección
    val disabledButtonColor = Color(0xFFB3CB54).copy(alpha = 0.5f) // Color gris para deshabilitado
    val cornerRadius = 30.dp // Bordes más redondeados
    var selectedExpenseIndex by remember { mutableStateOf(-1) }

    // Estado de desplazamiento para el contenido principal
    val mainScrollState = rememberScrollState()

    // Estado de desplazamiento para el componente de gastos
    val expensesScrollState = rememberScrollState()
    val canExpensesScrollUp by remember { derivedStateOf { expensesScrollState.value > 0 } }
    val canExpensesScrollDown by remember { derivedStateOf { expensesScrollState.value < expensesScrollState.maxValue } }

    // Estado de cambios para el botón "Guardar"
    var hasChanges by remember { mutableStateOf(false) }


    // Obtener el CoroutineScope
    val coroutineScope = rememberCoroutineScope()


    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    // Cargar datos locales al iniciar la vista
    LaunchedEffect(Unit) {
        limitsViewModel.getLimitsFromLocal(userId) { limits ->
            if (limits != null) {
                selectedFrequency = limits.frequency
                isByExpenseChecked = limits.isByExpenseChecked
                isByQuantityChecked = limits.isByQuantityChecked
                amountText = limits.totalAmount

                expenses.clear()
                expenses.addAll(limits.expenses.map { Expense(it.name, it.amount) })
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth()) {
                        Text(
                            "Limits and Goals",
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painter = painterResource(id = R.drawable.round_arrow_back_ios_24),
                            contentDescription = "Back")
                    }
                },
                actions = { Spacer(modifier = Modifier.width(48.dp)) }
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
        // Contenido principal, ajustado con innerPadding para evitar superposición con topBar y bottomBar

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(mainScrollState) // Habilitar scroll vertical en el contenido principal
        ) {
            // Contenedor de frecuencia con botones estilizados
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(cornerRadius),
                colors = CardDefaults.cardColors(
                    containerColor = lightGrayColor
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Daily", "Weekly", "Monthly").forEach { frequency ->
                        Button(
                            onClick = { selectedFrequency = frequency },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedFrequency == frequency) selectedButtonColor else lightGrayColor,
                                contentColor = if (selectedFrequency == frequency) Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        ) {
                            Text(frequency)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección By Expense
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isByExpenseChecked,
                    onCheckedChange = {
                        isByExpenseChecked = it
                        if (it) isByQuantityChecked = false
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("By expense")
            }

            if (isByExpenseChecked) {
                Spacer(modifier = Modifier.height(8.dp))

                // Contenedor de gastos desplazable con fondo transparente y bordes redondeados
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .clip(RoundedCornerShape(cornerRadius))
                        .background(Color.Transparent)
                ) {
                    // Contenedor scrollable siempre que el contenido exceda
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(expensesScrollState)
                            .padding(8.dp)
                    ) {
                        expenses.forEachIndexed { index, expense ->
                            // Card para cada gasto con un borde condicional azul oscuro si está seleccionado
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .border(
                                        width = if (index == selectedExpenseIndex) 2.dp else 0.dp,
                                        color = if (index == selectedExpenseIndex) selectedBorderColor else Color.Transparent,
                                        shape = RoundedCornerShape(30.dp)
                                    )
                                    .clickable {
                                        selectedExpenseIndex = index
                                        hasChanges = true
                                    },
                                shape = RoundedCornerShape(30.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = lightGrayColor
                                )
                            ) {
                                // Contenido de cada contenedor (campo de texto y otros)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Campo de nombre del gasto con límite de 50 caracteres
                                    TextField(
                                        value = expense.name,
                                        onValueChange = { newValue ->
                                            if (newValue.length <= MAX_NAME_LENGTH) {
                                                expense.name = newValue
                                                hasChanges = true
                                            }
                                        },
                                        placeholder = { Text("Expense Name", fontSize = 15.sp) },
                                        textStyle = TextStyle(
                                            fontSize = 15.sp,
                                            textAlign = TextAlign.Start
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp),
                                        colors = TextFieldDefaults.textFieldColors(
                                            containerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            disabledIndicatorColor = Color.Transparent,
                                            errorIndicatorColor = Color.Transparent,
                                        ),
                                        shape = RoundedCornerShape(30.dp),
                                        singleLine = true,
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Campo de monto con límite de 8 cifras
                                    TextField(
                                        value = expense.amount,
                                        onValueChange = { newValue ->
                                            // Filtrar para permitir solo dígitos y puntos, y limitar a 8 cifras
                                            val filtered = newValue.replace("[^\\d.]".toRegex(), "").take(MAX_AMOUNT_LENGTH)

                                            // Opcional: Eliminar puntos adicionales si solo quieres permitir un decimal
                                            val singleDecimal = filtered.split(".").let {
                                                if (it.size > 2) it[0] + "." + it[1]
                                                else filtered
                                            }

                                            // Asegurarse de que el valor no empiece con un punto
                                            val sanitized = if (singleDecimal.startsWith(".")) {
                                                singleDecimal.removePrefix(".")
                                            } else {
                                                singleDecimal
                                            }

                                            expense.amount = sanitized
                                            hasChanges = true
                                        },
                                        placeholder = { Text("Amount", fontSize = 14.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        textStyle = TextStyle(
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center
                                        ),
                                        modifier = Modifier
                                            .width(138.dp)
                                            .height(56.dp),
                                        colors = TextFieldDefaults.textFieldColors(
                                            containerColor = Color.White,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            disabledIndicatorColor = Color.Transparent,
                                            errorIndicatorColor = Color.Transparent,
                                        ),
                                        shape = RoundedCornerShape(30.dp),
                                        leadingIcon = { Text("$", fontSize = 14.sp) },
                                        singleLine = true,
                                        visualTransformation = NumberFormatTransformation()
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Botón de eliminación
                                    IconButton(
                                        onClick = {
                                            expenses.removeAt(index)
                                            hasChanges = true
                                        },
                                        modifier = Modifier
                                            .size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Delete Expense",
                                            tint = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Flechas de desplazamiento
                    if (expenses.size > 3) { // Mostrar flechas solo si hay más de 3 gastos
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Flecha hacia arriba
                            if (canExpensesScrollUp) {
                                Icon(
                                    painter = painterResource(id = R.drawable.round_arrow_up_24),
                                    contentDescription = "Scroll Up",
                                    tint = Color.Black,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            coroutineScope.launch {
                                                expensesScrollState.animateScrollBy(-100f)
                                            }
                                        }
                                )
                            }

                            // Flecha hacia abajo
                            if (canExpensesScrollDown) {
                                Icon(
                                    painter = painterResource(id = R.drawable.round_arrow_down_24),
                                    contentDescription = "Scroll Down",
                                    tint = Color.Black,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            coroutineScope.launch {
                                                expensesScrollState.animateScrollBy(100f)
                                            }
                                        }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón para agregar gasto
                Button(
                    onClick = {
                        expenses.add(Expense("", ""))
                        hasChanges = true
                    },
                    enabled = expenses.size < MAX_EXPENSES, // Deshabilitar solo el botón cuando se alcanza el máximo
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (expenses.size < MAX_EXPENSES) Color(0xFFB3CB54) else lightGrayColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add expense")
                }

                // Mostrar mensaje cuando se alcanza el límite de gastos
                if (expenses.size >= MAX_EXPENSES) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Máximo de $MAX_EXPENSES gastos alcanzado",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección By Quantity
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isByQuantityChecked,
                    onCheckedChange = {
                        isByQuantityChecked = it
                        if (it) isByExpenseChecked = false
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("By quantity")
            }

            if (isByQuantityChecked) {
                Spacer(modifier = Modifier.height(8.dp))

                // Campo de monto centrado y con límite de 8 cifras
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(cornerRadius),
                    colors = CardDefaults.cardColors(
                        containerColor = lightGrayColor
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TextField(
                            value = amountText,
                            onValueChange = { newValue ->
                                // Filtrar para permitir solo dígitos y puntos, y limitar a 8 cifras
                                val filtered = newValue.replace("[^\\d.]".toRegex(), "").take(MAX_AMOUNT_LENGTH)

                                // Opcional: Eliminar puntos adicionales si solo quieres permitir un decimal
                                val singleDecimal = filtered.split(".").let {
                                    if (it.size > 2) it[0] + "." + it[1]
                                    else filtered
                                }

                                // Asegurarse de que el valor no empiece con un punto
                                val sanitized = if (singleDecimal.startsWith(".")) {
                                    singleDecimal.removePrefix(".")
                                } else {
                                    singleDecimal
                                }

                                amountText = sanitized
                                hasChanges = true
                            },
                            placeholder = {
                                Text(
                                    "Amount",
                                    fontSize = 15.sp
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color.White,
                                    shape = RoundedCornerShape(30.dp)
                                ) // Fondo blanco en el campo de texto
                                .height(60.dp), // Ajusta la altura según sea necesario
                            shape = RoundedCornerShape(30.dp),
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = Color.Transparent,
                            ),
                            leadingIcon = {
                                Text("$", fontSize = 15.sp)
                            },
                            singleLine = true,
                            visualTransformation = NumberFormatTransformation()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón "Guardar" siempre disponible en la parte inferior
            Button(
                onClick = {
                    val limits = LimitsEntity(
                        userId = userId,
                        frequency = selectedFrequency,
                        isByExpenseChecked = isByExpenseChecked,
                        isByQuantityChecked = isByQuantityChecked,
                        expenses = expenses.map { ExpenseEntity(it.name, it.amount) },
                        totalAmount = amountText
                    )

                    // Guardar localmente
                    limitsViewModel.saveLimitsLocally(limits)

                    // Verificar conexión a Internet
                    if (isConnected(context)) {
                        // Guardar en Firebase
                        limitsViewModel.saveLimitsToFirebase(limits,
                            onSuccess = {
                                Toast.makeText(context, "Límites guardados en Firebase", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = {
                                Toast.makeText(context, "Error al guardar en Firebase", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        // Programar sincronización
                        scheduleSyncWork(context)
                        Toast.makeText(context, "Sin conexión. Se sincronizará cuando haya Internet", Toast.LENGTH_SHORT).show()
                    }

                    hasChanges = false
                },
                enabled = hasChanges,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasChanges) Color(0xFFB3CB54) else lightGrayColor,
                    contentColor = if (hasChanges) Color.Black else Color.Gray
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Guardar")
            }
        }
    }
}
