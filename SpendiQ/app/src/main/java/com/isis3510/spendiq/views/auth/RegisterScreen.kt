package com.isis3510.spendiq.views.auth

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.isis3510.spendiq.R
import com.isis3510.spendiq.viewmodel.AuthenticationViewModel
import com.isis3510.spendiq.viewmodel.AuthState
import com.isis3510.spendiq.views.theme.Purple40
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthenticationViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    val (checkedState, onStateChange) = remember { mutableStateOf(false) }

    // Variables del calendario
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Diálogo de selección de fecha
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            birthDate = dateFormatter.format(calendar.time)
        },
        year,
        month,
        day
    )

    // Establecer fecha máxima y mínima
    datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
    calendar.add(Calendar.YEAR, -100)
    datePickerDialog.datePicker.minDate = calendar.timeInMillis

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(7.dp))
            .background(color = Color.White)
    ) {
        // Imagen superior derecha
        Image(
            painter = painterResource(R.drawable.logoup),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 185.dp, y = (-70).dp)
                .size(300.dp)
        )

        // Botón de retroceso
        Image(
            painter = painterResource(id = R.drawable.leftactionable),
            contentDescription = "Back",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(24.dp)
                .clickable { navController.popBackStack() }
                .zIndex(1f) // Asegura que esté por encima de otros elementos
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp), // Añadimos padding horizontal
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally // Centramos horizontalmente
        ) {
            Text(
                text = "Create Free Account",
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 48.sp,
                    fontFamily = FontFamily.SansSerif
                ),
                modifier = Modifier
                    .padding(top = 16.dp) // Ajustamos el padding superior
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth(0.9f) // Ajustamos el ancho para que no ocupe todo el ancho
                    .padding(vertical = 4.dp)
                    .border(
                        BorderStroke(width = 2.dp, color = Purple40),
                        shape = RoundedCornerShape(50)
                    ),
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = { Text(text = "Full Name") },
                leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Person") }
            )

            OutlinedTextField(
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp)
                    .border(
                        BorderStroke(width = 2.dp, color = Purple40),
                        shape = RoundedCornerShape(50)
                    ),
                value = email,
                onValueChange = { email = it },
                placeholder = { Text(text = "Email") },
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email") }
            )

            OutlinedTextField(
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp)
                    .border(
                        BorderStroke(width = 2.dp, color = Purple40),
                        shape = RoundedCornerShape(50)
                    ),
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                placeholder = { Text(text = "Phone Number") },
                leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = "Phone") }
            )

            // Campo de fecha de nacimiento con selector de calendario
            OutlinedTextField(
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp)
                    .border(
                        BorderStroke(width = 2.dp, color = Purple40),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable { datePickerDialog.show() },
                value = birthDate,
                onValueChange = { },
                placeholder = { Text(text = "Birth Date (Click to select)") },
                leadingIcon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = "Calendar") },
                readOnly = true,
                enabled = false
            )

            OutlinedTextField(
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp)
                    .border(
                        BorderStroke(width = 2.dp, color = Purple40),
                        shape = RoundedCornerShape(50)
                    ),
                value = password,
                onValueChange = { password = it },
                placeholder = { Text(text = "Password") },
                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock1") },
                visualTransformation = PasswordVisualTransformation()
            )

            OutlinedTextField(
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp)
                    .border(
                        BorderStroke(width = 2.dp, color = Purple40),
                        shape = RoundedCornerShape(50)
                    ),
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text(text = "Confirm Password") },
                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock2") },
                visualTransformation = PasswordVisualTransformation()
            )

            Row(
                Modifier
                    .fillMaxWidth(0.9f)
                    .toggleable(
                        value = checkedState,
                        onValueChange = { onStateChange(!checkedState) },
                        role = Role.Checkbox
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = checkedState,
                    onCheckedChange = null
                )
                Text(
                    text = "Accept Terms & Conditions",
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (password == confirmPassword && checkedState) {
                        viewModel.register(
                            email = email,
                            password = password,
                            fullName = fullName,
                            phoneNumber = phoneNumber,
                            birthDate = birthDate
                        )
                    }
                },
                enabled = password == confirmPassword && checkedState &&
                        email.isNotEmpty() && fullName.isNotEmpty() &&
                        birthDate.isNotEmpty() && phoneNumber.isNotEmpty(),
                shape = RoundedCornerShape(7.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f) // Ajustamos el ancho del botón
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff65558f)),
            ) {
                Text(
                    text = "Sign Up",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 1.43.em,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 20.sp,
                        letterSpacing = 0.1.sp
                    ),
                    modifier = Modifier
                        .wrapContentHeight(align = Alignment.CenterVertically)
                )
            }

            when (authState) {
                is AuthState.Loading -> CircularProgressIndicator()
                is AuthState.Error -> Text(
                    (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
                is AuthState.Authenticated -> {
                    LaunchedEffect(Unit) {
                        navController.navigate("main") {
                            popUpTo("authentication") { inclusive = true }
                        }
                    }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Imagen inferior izquierda
        Image(
            painter = painterResource(R.drawable.logodown),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-185).dp, y = (-32).dp)
                .size(300.dp)
        )
    }
}
