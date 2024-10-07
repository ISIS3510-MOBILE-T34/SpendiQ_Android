package com.isis3510.spendiq.views.auth

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
fun LoginScreen(navController: NavController, viewModel: AuthenticationViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SpendiQ",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 30.dp)
        )

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
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.login(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log In")
        }

        when (authState) {
            is AuthState.Loading -> CircularProgressIndicator()
            is AuthState.Error -> Text(
                (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
            is AuthState.Authenticated -> {
                LaunchedEffect(Unit) {
                    viewModel.checkEmailVerification()
                }
            }
            is AuthState.EmailNotVerified -> {
                Text("Please verify your email to continue.")
                Button(onClick = { viewModel.sendEmailVerification() }) {
                    Text("Resend verification email")
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

        TextButton(onClick = { }) {
            Text("Forgot your ID or password?", color = MaterialTheme.colorScheme.primary)
        }
    }
}