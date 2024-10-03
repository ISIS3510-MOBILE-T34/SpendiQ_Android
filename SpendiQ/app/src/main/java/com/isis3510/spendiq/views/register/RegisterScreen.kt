package com.isis3510.spendiq.views.register

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.isis3510.spendiq.viewmodel.AuthenticationViewModel
import com.isis3510.spendiq.viewmodel.AuthState

@Composable
fun RegisterScreen(navController: NavController, viewModel: AuthenticationViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (password == confirmPassword) {
                    viewModel.register(email, password)
                } else {
                    // Show error message
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }

        when (authState) {
            is AuthState.Loading -> CircularProgressIndicator()
            is AuthState.Error -> Text((authState as AuthState.Error).message, color = MaterialTheme.colorScheme.error)
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