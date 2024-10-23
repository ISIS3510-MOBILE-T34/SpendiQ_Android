package com.isis3510.spendiq.views.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.isis3510.spendiq.R

@Composable
fun AuthenticationScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(7.dp))
            .background(color = Color.White)
    ) {
        // Background Logo
        Image(
            painter = painterResource(id = R.drawable.logogroupstart),
            contentDescription = "Logo Group",
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = (-63).dp, y = 376.dp)
                .fillMaxWidth()
                .height(470.dp)
        )

        // Column to hold the content vertically
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp), // Add padding to both sides
            horizontalAlignment = Alignment.CenterHorizontally, // Center horizontally
            verticalArrangement = Arrangement.Center
        ) {
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
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Spacer(modifier = Modifier.height(73.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                // Bug Icon
                IconButton(
                    onClick = {
                        try {
                            // Simulando un error recuperable
                            // Supongamos que estamos intentando realizar una operación que podría fallar
                            val isNetworkAvailable = false // Simula un estado de red no disponible
                            if (!isNetworkAvailable) {
                                throw Exception("Simulated recoverable error: Network not available")
                            }

                            // Enviar información a Crashlytics si todo va bien
                            val crashlytics = FirebaseCrashlytics.getInstance()
                            crashlytics.setCustomKey("Bug_AS", "Bug in Authorization Screen")
                            crashlytics.log("Error 403")
                        } catch (e: Exception) {
                            // Manejo de la excepción
                            val crashlytics = FirebaseCrashlytics.getInstance()
                            crashlytics.recordException(e) // Registra la excepción en Crashlytics

                            // Mostrar un mensaje al usuario indicando que hubo un error recuperable
                            // Puedes usar un Toast, Snackbar o cualquier otra forma de mostrar el mensaje
                            println("Recoverable error logged to Crashlytics: ${e.message}")
                            // Aquí podrías mostrar un SnackBar o un Toast para alertar al usuario
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning, // Replace with your warning icon resource
                        contentDescription = "Bug Icon",
                        tint = Color(0xffb3cb54) // Your desired color for the warning icon
                    )
                }

                // Space between icons
                Spacer(modifier = Modifier.width(16.dp))

                // Crash Icon
                IconButton(
                    onClick = {
                        val crashlytics = FirebaseCrashlytics.getInstance()
                        crashlytics.setCustomKey("Crash_AS","Crash in Authorization Screen")
                        crashlytics.log("App Crash after pushing crash button")
                        throw RuntimeException("Forced Crash from Authorization Screen")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close, // Replace with your bus icon resource
                        contentDescription = "Crash Icon",
                        tint = Color(0xffc33ba5) // Your desired color for the bus icon
                    )
                }
            }

            // Login Button
            Button(
                onClick = { navController.navigate("login") },
                shape = RoundedCornerShape(7.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff65558f)),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                modifier = Modifier
                    .fillMaxWidth() // Make button full width
                    .height(42.dp)
            ) {
                Text(
                    text = "Log In",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 1.43.em,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 0.1.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp)) // Add space between buttons

            // Register Button
            Button(
                onClick = { navController.navigate("register") },
                shape = RoundedCornerShape(7.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff65558f)),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                modifier = Modifier
                    .fillMaxWidth() // Make button full width
                    .height(42.dp)
            ) {
                Text(
                    text = "Sign Up",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 1.43.em,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 0.1.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(260.dp))
        }
    }
}
