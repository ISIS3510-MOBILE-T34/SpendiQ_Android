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
import com.isis3510.spendiq.viewmodel.AuthState
import com.isis3510.spendiq.viewmodel.AuthViewModel
import com.isis3510.spendiq.views.theme.Purple40
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
    val (checkedState, onStateChange) = remember { mutableStateOf(false) }

    // Calendar setup for date picker
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Date picker dialog
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

    // Set max and min dates for the date picker
    datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
    calendar.add(Calendar.YEAR, -100)
    datePickerDialog.datePicker.minDate = calendar.timeInMillis

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(7.dp))
            .background(color = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create Free Account",
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 48.sp,
                    fontFamily = FontFamily.SansSerif
                ),
                modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Full name input field
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = { Text("Full Name") },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp)
                    .border(BorderStroke(2.dp, Purple40), RoundedCornerShape(50)),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Person") }
            )

            // Email input field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp)
                    .border(BorderStroke(2.dp, Purple40), RoundedCornerShape(50)),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") }
            )

            // Phone number input field
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                placeholder = { Text("Phone Number") },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp)
                    .border(BorderStroke(2.dp, Purple40), RoundedCornerShape(50)),
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") }
            )

            // Birth date field
            OutlinedTextField(
                value = birthDate,
                onValueChange = { },
                placeholder = { Text("Birth Date (Click to select)") },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp)
                    .border(BorderStroke(2.dp, Purple40), RoundedCornerShape(50))
                    .clickable { datePickerDialog.show() },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Calendar") },
                readOnly = true,
                enabled = false
            )

            // Password input fields
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp)
                    .border(BorderStroke(2.dp, Purple40), RoundedCornerShape(50)),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock1") }
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp)
                    .border(BorderStroke(2.dp, Purple40), RoundedCornerShape(50)),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock2") }
            )

            // Checkbox for terms & conditions
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
                Checkbox(checked = checkedState, onCheckedChange = null)
                Text(text = "Accept Terms & Conditions", modifier = Modifier.padding(start = 16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Register button
            Button(
                onClick = {
                    if (password == confirmPassword && checkedState) {
                        viewModel.register(email, password, fullName, phoneNumber, birthDate)
                    }
                },
                enabled = password == confirmPassword && checkedState &&
                        email.isNotEmpty() && fullName.isNotEmpty() &&
                        birthDate.isNotEmpty() && phoneNumber.isNotEmpty(),
                shape = RoundedCornerShape(7.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff65558f))
            ) {
                Text("Sign Up", color = Color.White, textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Auth state handling
            when (authState) {
                is AuthState.Loading -> CircularProgressIndicator()
                is AuthState.Error -> Text(
                    (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
                is AuthState.Authenticated -> {
                    LaunchedEffect(Unit) {
                        // Trigger email verification
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