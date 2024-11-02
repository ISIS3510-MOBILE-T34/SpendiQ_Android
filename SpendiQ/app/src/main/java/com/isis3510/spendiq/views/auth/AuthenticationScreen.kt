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
                )
            )

            Spacer(modifier = Modifier.height(96.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                // Bug Icon
                IconButton(
                    onClick = {
                        // Simulación de un error recuperable
                        val crashlytics = FirebaseCrashlytics.getInstance()

                        try {
                            // Simular una operación que produce un error recuperable
                            throw Exception("Simulated recoverable error: Validation failed")
                        } catch (e: Exception) {
                            // Registrar la excepción no fatal en Crashlytics
                            crashlytics.recordException(e) // Esto reporta una excepción sin cerrar la app
                            crashlytics.setCustomKey("Bug_AS", "Simulated recoverable error in Authorization Screen")
                            crashlytics.log("Non-fatal exception logged")

                            // Puedes mostrar un mensaje al usuario si lo deseas, sin cerrar la app
                            println("Recoverable error caught and logged")
                            crashlytics.sendUnsentReports()
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
