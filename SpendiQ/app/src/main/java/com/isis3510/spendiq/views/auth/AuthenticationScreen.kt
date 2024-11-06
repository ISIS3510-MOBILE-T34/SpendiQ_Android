package com.isis3510.spendiq.views.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

/**
 * AuthenticationScreen composable function
 *
 * Serves as the entry point for the application's authentication workflow, presenting the user with options
 * to either log in or sign up for a new account. It includes visual elements such as the app title and a
 * background logo image, providing a welcoming layout to guide users in accessing the application.
 * Additionally, it incorporates Firebase Crashlytics integration, allowing for testing of error handling
 * (both recoverable and non-recoverable errors) by simulating a logged recoverable error and a forced crash.
 *
 * Key Features:
 * - App Branding: Displays the app title ("SpendiQ") in a prominent, bold font, paired with a background logo.
 * - Authentication Navigation:
 *   - Login: Navigates to the Login screen.
 *   - Register: Navigates to the Register screen.
 * - Firebase Crashlytics Integration:
 *   - Simulated Recoverable Error: Logs a non-fatal error to Firebase Crashlytics, simulating recoverable error handling.
 *   - Forced Crash: Triggers a forced app crash, useful for testing error tracking setup.
 *
 * UI Structure:
 * - Background logo image for branding.
 * - Centralized layout with app title, login, and register buttons.
 * - Row layout for icons that allow simulated bug/error handling and crash testing.
 *
 * Supporting Components:
 * - Buttons for Login and Register actions, both spanning the full width of the screen.
 * - Error handling icons that log simulated errors or trigger a crash.
 *
 * @param navController [NavController] used to navigate to either the Login or Register screen.
 */

@Composable
fun AuthenticationScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(7.dp))
            .background(color = Color.White)
    ) {
        // Background Logo Image
        Image(
            painter = painterResource(id = R.drawable.logogroupstart),
            contentDescription = "Logo Group",
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = (-63).dp, y = 376.dp)
                .fillMaxWidth()
                .height(470.dp)
        )

        // Main Column Layout for Centered Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp), // Padding for horizontal sides
            horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
            verticalArrangement = Arrangement.Center
        ) {
            // App Title "SpendiQ"
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

            Spacer(modifier = Modifier.height(96.dp)) // Space below title

            // Row for Simulated Error and Crash Icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                // Recoverable Error (Bug) Icon
                IconButton(
                    onClick = {
                        // Set up Firebase Crashlytics instance
                        val crashlytics = FirebaseCrashlytics.getInstance()

                        try {
                            // Simulate an operation causing a recoverable error
                            throw Exception("Simulated recoverable error: Validation failed")
                        } catch (e: Exception) {
                            // Log the recoverable error in Crashlytics
                            crashlytics.recordException(e) // Logs non-fatal error without crashing
                            crashlytics.setCustomKey("Bug_AS", "Simulated recoverable error in Authorization Screen")
                            crashlytics.log("Non-fatal exception logged")

                            // Optionally show a user-facing message here
                            println("Recoverable error caught and logged")
                            crashlytics.sendUnsentReports()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Bug Icon",
                        tint = Color(0xffb3cb54) // Color for warning icon
                    )
                }

                Spacer(modifier = Modifier.width(16.dp)) // Space between icons

                // Forced Crash Icon
                IconButton(
                    onClick = {
                        val crashlytics = FirebaseCrashlytics.getInstance()
                        crashlytics.setCustomKey("Crash_AS", "Crash in Authorization Screen")
                        crashlytics.log("App Crash after pushing crash button")
                        throw RuntimeException("Forced Crash from Authorization Screen")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Crash Icon",
                        tint = Color(0xffc33ba5) // Color for crash icon
                    )
                }
            }

            // Log In Button
            Button(
                onClick = { navController.navigate("login") },
                shape = RoundedCornerShape(7.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff65558f)),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
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

            Spacer(modifier = Modifier.height(16.dp)) // Space between buttons

            // Register Button
            Button(
                onClick = { navController.navigate("register") },
                shape = RoundedCornerShape(7.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff65558f)),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
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

            Spacer(modifier = Modifier.height(260.dp)) // Space below buttons
        }
    }
}