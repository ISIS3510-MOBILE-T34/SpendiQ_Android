package com.isis3510.spendiq.views.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.isis3510.spendiq.viewmodel.AuthenticationViewModel
import com.isis3510.spendiq.viewmodel.AuthState

@Composable
fun SplashScreen(navController: NavController, viewModel: AuthenticationViewModel) {
    val authState = viewModel.authState.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> navController.navigate("main") {
                popUpTo("splash") { inclusive = true }
            }
            is AuthState.Error,
            AuthState.Idle,
            is AuthState.EmailNotVerified,
            is AuthState.EmailVerificationSent,
            is AuthState.EmailVerified -> navController.navigate("authentication") {
                popUpTo("splash") { inclusive = true }
            }
            AuthState.Loading -> {} // Do nothing, wait for the state to change
        }
    }
}
