package com.isis3510.spendiq.views.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.isis3510.spendiq.viewmodel.AuthState
import com.isis3510.spendiq.viewmodel.AuthViewModel

/**
 * SplashScreen composable function
 *
 * Displays a splash screen with a loading indicator while waiting for authentication status.
 * Based on the authentication state, it navigates to different parts of the application.
 *
 * @param navController [NavController] used for navigation
 * @param viewModel [AuthViewModel] that provides the current authentication state
 */
@Composable
fun SplashScreen(navController: NavController, viewModel: AuthViewModel) {
    // Collect the current authentication state from the viewModel
    val authState = viewModel.authState.collectAsState()

    // Main box layout for centering the splash screen content
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Render UI elements based on the authentication state
        when (authState.value) {
            AuthState.Loading -> {
                // Loading state UI: show progress indicator and loading text
                CircularProgressIndicator()
                Text(text = "Loading...", modifier = Modifier.align(Alignment.BottomCenter))
            }
            else -> {
                // Fallback UI: show progress indicator while awaiting state change
                CircularProgressIndicator()
            }
        }
    }

    // Handle side effects based on changes in the authentication state
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                // Navigate to main screen if authenticated
                navController.navigate("main") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            is AuthState.EmailVerified -> {
                // Navigate to main screen if email is verified
                navController.navigate("main") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            is AuthState.EmailNotVerified -> {
                // Navigate to authentication screen if email is not verified
                navController.navigate("authentication") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            is AuthState.EmailVerificationSent -> {
                // Navigate to authentication screen after verification email is sent
                navController.navigate("authentication") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            is AuthState.Error,
            AuthState.Idle,
            AuthState.BiometricEnabled -> {
                // Handle other states by navigating to authentication screen
                navController.navigate("authentication") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            AuthState.Loading -> {
                // Do nothing while in loading state
            }
            AuthState.BiometricAlreadyEnabled -> {
                // Navigate to authentication screen if biometrics are already enabled
                navController.navigate("authentication") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            AuthState.PasswordResetEmailSent -> {
                // Placeholder for handling password reset email sent state
                TODO() // Implement as needed
            }
        }
    }
}
