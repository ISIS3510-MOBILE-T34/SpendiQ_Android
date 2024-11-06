package com.isis3510.spendiq.views.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.TransactionViewModel
import com.isis3510.spendiq.views.common.BottomNavigation
import java.text.NumberFormat
import java.util.Locale

// Clase Expense con propiedades reactivas
class Expense(
    name: String = "",
    amount: String = ""
) {
    var name by mutableStateOf(name)
    var amount by mutableStateOf(amount)
}

// VisualTransformation para formatear números con comas
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
    // Variables de estado
    var isByExpenseChecked by remember { mutableStateOf(true) }
    var isByQuantityChecked by remember { mutableStateOf(false) }
    val expenses = remember { mutableStateListOf<Expense>() }
    var selectedFrequency by remember { mutableStateOf("Daily") }
    var amountText by remember { mutableStateOf("") }
    val lightGrayColor = Color(0xFFD9D9D9)
    val cornerRadius = 30.dp // Definimos el radio de los bordes redondeados

    // Estado de desplazamiento para controlar las sombras
    val scrollState = rememberScrollState()
    val canScrollUp by remember { derivedStateOf { scrollState.value > 0 } }
    val canScrollDown by remember { derivedStateOf { scrollState.value < scrollState.maxValue } }

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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
        ) {
            // Sección principal con scroll
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
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
                        // Contenedor scrollable
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                                .padding(8.dp)
                        ) {
                            expenses.forEach { expense ->
                                // Card para cada gasto con fondo gris claro
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(30.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = lightGrayColor
                                    )
                                ) {
                                    // Row para los campos de texto
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Campo de nombre del gasto
                                        TextField(
                                            value = expense.name,
                                            onValueChange = { newValue ->
                                                expense.name = newValue
                                            },
                                            placeholder = {
                                                Text(
                                                    "Expense Name",
                                                    fontSize = 15.sp
                                                )
                                            },
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

                                        // Campo de monto con formato de número
                                        TextField(
                                            value = expense.amount,
                                            onValueChange = { newValue ->
                                                // Permitir solo dígitos y puntos
                                                val filtered = newValue.replace("[^\\d.]".toRegex(), "")
                                                expense.amount = filtered
                                            },
                                            placeholder = {
                                                Text(
                                                    "Amount",
                                                    fontSize = 14.sp
                                                )
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            textStyle = TextStyle(
                                                fontSize = 14.sp,
                                                textAlign = TextAlign.Center
                                            ),
                                            modifier = Modifier
                                                .width(140.dp)
                                                .height(56.dp),
                                            colors = TextFieldDefaults.textFieldColors(
                                                containerColor = Color.White,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent,
                                                disabledIndicatorColor = Color.Transparent,
                                                errorIndicatorColor = Color.Transparent,
                                            ),
                                            shape = RoundedCornerShape(30.dp),
                                            leadingIcon = {
                                                Text("$", fontSize = 14.sp)
                                            },
                                            singleLine = true,
                                            visualTransformation = NumberFormatTransformation()
                                        )
                                    }
                                }
                            }
                        }

                        // Sombras de indicación de scroll con bordes redondeados y opacidad más tenue
                        if (canScrollUp) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(30.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Gray.copy(alpha = 0.15f), Color.Transparent),
                                            startY = 0f,
                                            endY = 30f
                                        )
                                    )
                                    .clip(RoundedCornerShape(cornerRadius))
                                    .align(Alignment.TopCenter)
                            )
                        }

                        if (canScrollDown) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(30.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Gray.copy(alpha = 0.15f)),
                                            startY = 0f,
                                            endY = 30f
                                        )
                                    )
                                    .clip(RoundedCornerShape(cornerRadius))
                                    .align(Alignment.BottomCenter)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón Add Expense
                    Button(
                        onClick = {
                            expenses.add(Expense("", ""))
                        },
                        enabled = isByExpenseChecked,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = lightGrayColor
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add expense")
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
                    // Selector de frecuencia
                    Row {
                        listOf("Daily", "Weekly", "Monthly").forEach { frequency ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 16.dp)
                            ) {
                                RadioButton(
                                    selected = selectedFrequency == frequency,
                                    onClick = { selectedFrequency = frequency }
                                )
                                Text(frequency)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Campo de texto para monto con formato de número
                    TextField(
                        value = amountText,
                        onValueChange = { newValue ->
                            // Permitir solo dígitos y puntos
                            val filtered = newValue.replace("[^\\d.]".toRegex(), "")
                            amountText = filtered
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
                            .height(60.dp),
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
    }

    // Función de formateo de número (opcional, no utilizada en el último código)
    fun formatCurrency(value: String): String {
        return if (value.isNotEmpty()) {
            try {
                NumberFormat.getNumberInstance(Locale.US).format(value.toDouble())
            } catch (e: NumberFormatException) {
                value
            }
        } else value
    }
}
