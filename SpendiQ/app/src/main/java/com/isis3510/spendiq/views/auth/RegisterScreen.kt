package com.isis3510.spendiq.views.auth

import android.app.DatePickerDialog
import android.util.Patterns
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.isis3510.spendiq.R
import com.isis3510.spendiq.viewmodel.AuthState
import com.isis3510.spendiq.viewmodel.AuthViewModel
import com.isis3510.spendiq.views.theme.Purple40
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    var checkedState by remember { mutableStateOf(false) }
    var isBackButtonEnabled by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

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

    datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
    calendar.add(Calendar.YEAR, -100)
    datePickerDialog.datePicker.minDate = calendar.timeInMillis

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(color = Color.White)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // Botón de retroceso
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = {
                    if (isBackButtonEnabled) {
                        isBackButtonEnabled = false
                        navController.popBackStack()
                    }
                },
                enabled = isBackButtonEnabled,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.round_arrow_back_ios_24),
                    contentDescription = "Back",
                    tint = Purple40,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create Free Account",
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 40.sp,
                    fontFamily = FontFamily.SansSerif
                )
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = { Text("Full Name") },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Person", tint = Color(0xffb3cb54)) },
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple40,
                    unfocusedBorderColor = Purple40,
                    errorBorderColor = MaterialTheme.colorScheme.error
                ),
                singleLine = true
            )

            val isEmailValid = remember(email) { Patterns.EMAIL_ADDRESS.matcher(email).matches() }
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Email") },
                isError = email.isNotEmpty() && !isEmailValid,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = Color(0xffb3cb54)) },
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple40,
                    unfocusedBorderColor = Purple40,
                    errorBorderColor = MaterialTheme.colorScheme.error
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
            )
            if (email.isNotEmpty() && !isEmailValid) {
                Text(
                    text = "Invalid email format",
                    color = MaterialTheme.colorScheme.error,
                    style = TextStyle(fontSize = 12.sp)
                )
            }

            val isPhoneValid = remember(phoneNumber) { phoneNumber.all { it.isDigit() } && phoneNumber.length >= 10 }
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) phoneNumber = it
                },
                placeholder = { Text("Phone Number") },
                isError = phoneNumber.isNotEmpty() && !isPhoneValid,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp),
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone", tint = Color(0xffb3cb54)) },
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple40,
                    unfocusedBorderColor = Purple40,
                    errorBorderColor = MaterialTheme.colorScheme.error
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            if (phoneNumber.isNotEmpty() && !isPhoneValid) {
                Text(
                    text = "Phone number must have at least 10 digits",
                    color = MaterialTheme.colorScheme.error,
                    style = TextStyle(fontSize = 12.sp)
                )
            }

            OutlinedTextField(
                value = birthDate,
                onValueChange = { },
                placeholder = { Text("Birth Date (Click to select)") },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp)
                    .clickable { datePickerDialog.show() },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Calendar", tint = Color(0xffc33ba5)) },
                readOnly = true,
                enabled = false,
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple40,
                    unfocusedBorderColor = Purple40
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock1", tint = Color(0xffc33ba5)) },
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple40,
                    unfocusedBorderColor = Purple40,
                    errorBorderColor = MaterialTheme.colorScheme.error
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock2", tint = Color(0xffc33ba5)) },
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple40,
                    unfocusedBorderColor = Purple40,
                    errorBorderColor = MaterialTheme.colorScheme.error
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
            )

            Row(
                Modifier
                    .fillMaxWidth(0.9f)
                    .toggleable(
                        value = checkedState,
                        onValueChange = { checkedState = it },
                        role = Role.Checkbox
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = checkedState,
                    onCheckedChange = { checkedState = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Purple40,
                        uncheckedColor = Color.Gray
                    )
                )
                Text(text = "Accept Terms & Conditions", modifier = Modifier.padding(start = 16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (password == confirmPassword && checkedState) {
                        viewModel.register(email, password, fullName, phoneNumber, birthDate)
                    }
                },
                enabled = password == confirmPassword && checkedState &&
                        email.isNotEmpty() && fullName.isNotEmpty() &&
                        birthDate.isNotEmpty() && phoneNumber.isNotEmpty() &&
                        isEmailValid && isPhoneValid,
                shape = RoundedCornerShape(7.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff65558f))
            ) {
                Text("Sign Up", color = Color.White, textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (authState) {
                is AuthState.Loading -> CircularProgressIndicator()
                is AuthState.Error -> Text(
                    (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
                is AuthState.Authenticated -> {
                    LaunchedEffect(Unit) {
                        viewModel.sendEmailVerification()
                    }
                }
                is AuthState.EmailVerificationSent -> {
                    Text("Verification email sent. Please check your inbox.")
                    Button(onClick = { viewModel.checkEmailVerification() }) {
                        Text("I've verified my email")
                    }
                }
                is AuthState.EmailVerified -> {
                    LaunchedEffect(Unit) {
                        navController.navigate("main") {
                            popUpTo("authentication") { inclusive = true }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}
