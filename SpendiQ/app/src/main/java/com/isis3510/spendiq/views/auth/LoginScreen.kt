package com.isis3510.spendiq.views.auth

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.isis3510.spendiq.R
import com.isis3510.spendiq.viewmodel.AuthenticationViewModel
import com.isis3510.spendiq.viewmodel.AuthState
import com.isis3510.spendiq.views.theme.Purple40

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthenticationViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Background Logo
        Image(
            painter = painterResource(id = R.drawable.logo_log_in),
            contentDescription = "Background Logo",
            modifier = Modifier
                .fillMaxSize()
                .align(alignment = Alignment.Center)
        )

        // Back Button
        Image(
            painter = painterResource(id = R.drawable.leftactionable),
            contentDescription = "Back",
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .padding(16.dp)
                .size(24.dp)
                .clickable { navController.popBackStack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(93.dp))

            // App Title
            Text(
                text = "SpendiQ",
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 73.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 48.dp)
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(
                        BorderStroke(width = 2.dp, color = Purple40),
                        shape = RoundedCornerShape(50)
                    ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email Icon"
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(
                        BorderStroke(width = 2.dp, color = Purple40),
                        shape = RoundedCornerShape(50)
                    ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password Icon"
                    )
                },
                trailingIcon = {
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
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            Button(
                onClick = { viewModel.login(email, password) },
                shape = RoundedCornerShape(7.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff65558f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Log In",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Forgot Password
            Text(
                text = "Forgot your ID or password?",
                color = Color(0xff5875dd),
                modifier = Modifier.clickable { /* Handle forgot password */ },
                style = TextStyle(fontSize = 16.sp)
            )

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
                        Log.d("LoginScreen", "Enable Biometrics clicked with email: $email")
                        viewModel.enableBiometricLogin(email, password)
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

        // Authentication State Handling
        when (authState) {
            is AuthState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is AuthState.Error -> {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
            is AuthState.Authenticated -> {
                LaunchedEffect(Unit) {
                    navController.navigate("main") {
                        popUpTo("authentication") { inclusive = true }
                    }
                }
            }
            is AuthState.BiometricEnabled -> {
                Log.d("LoginScreen", "Biometrics enabled successfully")
            }
            else -> { /* Handle other states */ }
        }

        // Bottom Bar
        Box(
            modifier = Modifier
                .align(alignment = Alignment.BottomCenter)
                .fillMaxWidth()
                .height(34.dp)
                .background(color = Color(0xffe5e5e5).copy(alpha = 0.29f))
        ) {
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.BottomCenter)
                    .offset(y = (-8).dp)
                    .width(134.dp)
                    .height(5.dp)
                    .clip(shape = RoundedCornerShape(100.dp))
                    .background(color = Color(0xff0f172a))
            )
        }
    }
}