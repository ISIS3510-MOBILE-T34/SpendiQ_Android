package com.isis3510.spendiq.views.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.isis3510.spendiq.R
import com.isis3510.spendiq.viewmodel.AuthState
import com.isis3510.spendiq.viewmodel.AuthViewModel
import com.isis3510.spendiq.viewmodel.ConnectivityViewModel
import com.isis3510.spendiq.views.theme.Purple40
import kotlinx.coroutines.delay

/**
 * LoginScreen composable function
 *
 * Provides the user interface for user login, including fields for email and password,
 * support for biometric login, and a password reset option. The login screen also reacts
 * to network connectivity changes and presents feedback messages based on authentication state.
 *
 * Key Features:
 * - Email and Password Input: Users enter their login credentials with real-time password visibility toggle.
 * - Biometric Authentication: Users can enable or log in using biometric authentication (e.g., fingerprint).
 * - Password Reset: Includes a "Forgot your password?" option, prompting a dialog for password reset.
 * - Network Connectivity Awareness: Displays a toast message when the network connection status changes.
 * - Authentication Status Handling: Manages states for loading, success, errors, email verification,
 *   biometric status, and password reset confirmation.
 *
 * UI Structure:
 * - Box layout for background logo alignment.
 * - Column layout containing:
 *   - App title.
 *   - Input fields for email and password.
 *   - Button to trigger login and text for password reset.
 *   - Conditional AlertDialogs for resetting passwords and enabling biometrics.
 * - Bottom Row layout with options for enabling biometrics and accessing help.
 *
 * Supporting Components:
 * - `OutlinedTextField`: Email and password fields with specific styling and icons.
 * - `AlertDialog`: Used for both password reset and biometric enabling dialogs.
 * - Authentication State Handling: Reacts to state changes with toast messages or by showing dialogs.
 *
 * @param navController [NavController] to navigate to other screens upon successful login.
 * @param viewModel [AuthViewModel] responsible for handling authentication logic and states.
 * @param connectivityViewModel [ConnectivityViewModel] observes network status for enabling/disabling the login button.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel,
    connectivityViewModel: ConnectivityViewModel
) {
    // State variables for input fields, visibility toggle, focus, and auth state
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    // Reset password and biometric dialog visibility
    var showResetPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var isBackButtonEnabled by remember { mutableStateOf(true) }
    var showBiometricDialog by remember { mutableStateOf(false) }

    //Connection & Eventual Connectivity
    val isLogInButtonEnable by connectivityViewModel.isConnected.observeAsState(true)
    var previousConnectionState by remember { mutableStateOf(isLogInButtonEnable) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Background logo image
        Image(
            painter = painterResource(id = R.drawable.logo_log_in),
            contentDescription = "Background Logo",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .align(Alignment.TopCenter)
                .offset(y = 75.dp)
        )

        // Back button at the top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 24.dp),
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
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.round_arrow_back_ios_24),
                    contentDescription = "Back",
                    tint = Purple40,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(113.dp))

            // App title
            Text(
                text = "SpendiQ",
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 73.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(210.dp))

            // Email input field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = Color(0xFFD9D9D9)) },
                shape = RoundedCornerShape(50),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Purple40,
                    unfocusedBorderColor = Purple40
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password input field with visibility toggle and biometric icon
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password Icon",
                        tint = Color(0xFFD9D9D9)
                    )
                },
                trailingIcon = {
                    Row {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible }
                        ) {
                            Icon(
                                painter = painterResource(id = if (passwordVisible) R.drawable.round_visibility_24 else R.drawable.baseline_visibility_off_24),
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = Color.Gray
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.setupBiometricPrompt(
                                    context as FragmentActivity,
                                    onSuccess = { viewModel.loginWithBiometrics() },
                                    onError = { /* Handle error */ }
                                )
                                viewModel.showBiometricPrompt()
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.fingerprint),
                                contentDescription = "Fingerprint",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(50),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Purple40,
                    unfocusedBorderColor = Purple40
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login button
            Button(
                onClick = { viewModel.login(email, password) },
                shape = RoundedCornerShape(7.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff65558f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = isLogInButtonEnable
            ) {
                Text(
                    text = "Log In",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Forgot password option
            Text(
                text = "Forgot your password?",
                color = Color(0xff589ddd),
                modifier = Modifier.clickable { showResetPasswordDialog = true },
                style = TextStyle(fontSize = 16.sp)
            )

            // Reset password dialog
            if (showResetPasswordDialog) {
                AlertDialog(
                    onDismissRequest = { showResetPasswordDialog = false },
                    title = {
                        Text(text = "Reset Password")
                    },
                    text = {
                        Column {
                            Text(
                                text = "Enter your email address to receive a password reset link.",
                                style = TextStyle(fontSize = 14.sp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = resetEmail,
                                onValueChange = { resetEmail = it },
                                label = { Text("Email") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.sendPasswordResetEmail(resetEmail)
                                showResetPasswordDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff65558f))
                        ) {
                            Text("Send", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResetPasswordDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Authentication and error handling messages
            when (authState) {
                is AuthState.PasswordResetEmailSent -> {
                    Text(
                        text = "Password reset email sent successfully.",
                        color = Color(0xffb3cb54),
                        textAlign = TextAlign.Center,
                        style = TextStyle(fontSize = 16.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
                is AuthState.Error -> {
                    val errorMessage = if ((authState as AuthState.Error).message.contains("network", ignoreCase = true)) {
                        "It looks like you're offline. Please check your network connection and try again to log in."
                    } else {
                        (authState as AuthState.Error).message
                    }
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        style = TextStyle(fontSize = 16.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
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
                is AuthState.BiometricAlreadyEnabled ->
                {
                    Toast.makeText(context, "Biometrics Already Enabled", Toast.LENGTH_SHORT).show()
                }
                else -> { /* Handle other states */ }
            }

            // Spacer for adjusting layout
            Spacer(modifier = Modifier.weight(1f))

            // Enable biometrics and help options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Enable Biometrics",
                    color = Color(0xffc33ba5),
                    modifier = Modifier.clickable {
                        showBiometricDialog = true
                    },
                    style = TextStyle(fontSize = 16.sp)
                )

                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(Color(0xff5875dd).copy(alpha = 0.53f))
                )

                Text(
                    text = "Help",
                    color = Color(0xffb3cb54),
                    modifier = Modifier.clickable { /* Handle help */ },
                    style = TextStyle(fontSize = 16.sp)
                )
            }
        }

        // Biometric enable dialog
        if (showBiometricDialog) {
            AlertDialog(
                onDismissRequest = { showBiometricDialog = false },
                title = { Text(text = "Enable Biometrics") },
                text = { Text("Are you sure you want to enable biometric login?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.enableBiometricLogin(email, password)
                            showBiometricDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xff65558f))
                    ) {
                        Text("Accept", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBiometricDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (isLogInButtonEnable != previousConnectionState) {
            if (isLogInButtonEnable) {
                Toast.makeText(context, "Back Online!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "It looks like you're offline. Please check your network connection", Toast.LENGTH_SHORT).show()
            }
            previousConnectionState = isLogInButtonEnable
        }

        // Reset AuthState after displaying messages
        LaunchedEffect(authState) {
            if (authState is AuthState.PasswordResetEmailSent || authState is AuthState.Error) {
                delay(3000)
                viewModel.resetAuthState()
            }
        }
    }
}
