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
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.isis3510.spendiq.views.common.AlertDialogCreate
import com.isis3510.spendiq.views.theme.Purple40
import java.text.SimpleDateFormat
import java.util.*

/**
 * RegisterScreen composable function
 *
 * Provides the user interface for the account registration process, allowing users to input their personal
 * information and create an account. The form includes fields for the user's full name, email, phone number,
 * birth date, and password. A checkbox for agreeing to terms and conditions and password confirmation are
 * also required to complete the registration.
 *
 * Key Features:
 * - Input Validation: Real-time validation for email format and phone number length.
 * - Date Picker: Allows users to select a birth date using a DatePicker dialog.
 * - Form Validation: Ensures that all required fields are filled out and meet validation criteria before
 *   enabling the "Sign Up" button.
 * - Authentication Status Handling: Manages the different states of authentication, such as loading,
 *   error, email verification, and success.
 *
 * UI Structure:
 * - Column layout that vertically scrolls for comfortable input on smaller screens.
 * - Fields for full name, email, phone number, birth date, password, and confirm password.
 * - Checkbox for terms and conditions agreement.
 * - "Sign Up" button that triggers registration if all criteria are met.
 *
 * Supporting Components:
 * - `OutlinedTextField`: Used for user input fields, each with specific styling and icons.
 * - `DatePickerDialog`: Allows selection of birth date.
 * - Authentication State Handling: Renders feedback or actions based on current authentication state,
 *   such as showing a progress indicator or error message.
 *
 * @param navController [NavController] to navigate within the app after registration.
 * @param viewModel [AuthViewModel] that handles registration logic and authentication states.
 */

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    // State variables for user inputs
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    var checkedState by remember { mutableStateOf(false) } // Terms and conditions checkbox
    var isBackButtonEnabled by remember { mutableStateOf(true) } // Back button control
    var enableEmailVerSentDialog by remember { mutableStateOf(false) }
    var isBirthdateValid by remember { mutableStateOf(false) }

    // DatePicker setup for birth date
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun isDateMoreThan16YearsOld(date: Calendar): Boolean {
        val today = Calendar.getInstance()
        today.add(Calendar.YEAR, -16)
        return date.before(today)
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            birthDate = dateFormatter.format(calendar.time)

            // Check if the birthdate is more than 16 years ago
            isBirthdateValid = isDateMoreThan16YearsOld(calendar)
        },
        year,
        month,
        day
    )

    datePickerDialog.datePicker.maxDate = System.currentTimeMillis() // Restricts date selection to today or earlier
    calendar.add(Calendar.YEAR, -100) // Sets minimum date to 100 years ago
    datePickerDialog.datePicker.minDate = calendar.timeInMillis

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(color = Color.White)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // Back button
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

        // Title
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

        // Form fields
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Full Name field
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
                    unfocusedBorderColor = Purple40
                ),
                singleLine = true
            )

            // Email field with validation
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
                    unfocusedBorderColor = Purple40
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

            // Phone number field with validation
            val isPhoneValid = remember(phoneNumber) { phoneNumber.all { it.isDigit() } && phoneNumber.length >= 10 }
            if (phoneNumber.isNotEmpty() && !isPhoneValid) {
                Text(
                    text = "Phone number must have at least 10 digits",
                    color = MaterialTheme.colorScheme.error,
                    style = TextStyle(fontSize = 12.sp)
                )
            }
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 15) phoneNumber = it },
                placeholder = { Text("Phone Number") },
                isError = phoneNumber.isNotEmpty() && !isPhoneValid,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 4.dp),
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone", tint = Color(0xffb3cb54)) },
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple40,
                    unfocusedBorderColor = Purple40
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            // Birth Date field
            if (birthDate.isNotEmpty() && !isBirthdateValid) {
                Text(
                    text = "You must be at least 16 years old",
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
                    disabledBorderColor = Purple40,
                    disabledTextColor = Color.Black,
                    disabledPlaceholderColor = Color.Black
                ),
                singleLine = true
            )

            // Password and Confirm Password fields
            val isPasswordValid = remember(password) { password.length >= 6 }
            if (password.isNotEmpty() && !isPasswordValid) {
                Text(
                    text = "Password must at least have 6 letters",
                    color = MaterialTheme.colorScheme.error,
                    style = TextStyle(fontSize = 12.sp)
                )
            }
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
                    unfocusedBorderColor = Purple40
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
            )


            val isConfirmPasswordValid = remember(confirmPassword) {confirmPassword == password}
            if (confirmPassword.isNotEmpty() && !isConfirmPasswordValid) {
                Text(
                    text = "Passwords do not match",
                    color = MaterialTheme.colorScheme.error,
                    style = TextStyle(fontSize = 12.sp)
                )
            }
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
                    unfocusedBorderColor = Purple40
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
            )


            // Terms and Conditions Checkbox
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

            // Sign Up Button
            Button(
                onClick = {
                    if (password == confirmPassword && checkedState) {
                        viewModel.register(email, password, fullName, phoneNumber, birthDate)
                    }
                },
                enabled = isConfirmPasswordValid && checkedState &&
                        email.isNotEmpty() && fullName.isNotEmpty() && isBirthdateValid
                        && isPasswordValid && birthDate.isNotEmpty() && phoneNumber.isNotEmpty()
                        && isEmailValid && isPhoneValid,
                shape = RoundedCornerShape(7.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff65558f))
            ) {
                Text("Sign Up", color = Color.White, textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (enableEmailVerSentDialog)
            {
                AlertDialogCreate({enableEmailVerSentDialog = false},
                    { enableEmailVerSentDialog = false
                        viewModel.checkEmailVerification() },
                    "Verification Email Sent",
                    "Please check your inbox. After verifying, click on CONFIRM to continue.",
                    Icons.AutoMirrored.Filled.Send,
                    false)
            }

            // Authentication state feedback
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
                    enableEmailVerSentDialog = true
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
