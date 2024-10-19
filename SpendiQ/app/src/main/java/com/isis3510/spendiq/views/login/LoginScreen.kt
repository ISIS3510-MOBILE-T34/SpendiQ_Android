package com.isis3510.spendiq.views.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.isis3510.spendiq.R
import com.isis3510.spendiq.viewmodel.AuthenticationViewModel
import com.isis3510.spendiq.viewmodel.AuthState

@Composable
fun LoginScreen(navController: NavController,
                viewModel: AuthenticationViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_log_in),
            contentDescription = "logo log in",
            modifier = Modifier
                .fillMaxSize()
                .align(alignment = Alignment.Center)
                .requiredWidth(width = 708.dp)
                .requiredHeight(height = 357.dp))
        Text(
            text = "SpendiQ",
            color = Color.Black,
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 73.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .fillMaxWidth()
                .align(alignment = Alignment.TopStart)
                .offset(y = 93.dp)
                .requiredHeight(77.dp)
        )

        // User/email TextField
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "E-mail") },
            modifier = Modifier
                .align(alignment = Alignment.Center)
                .fillMaxWidth(0.8f)
                .requiredHeight(50.dp)
        )

        // Password TextField
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .align(alignment = Alignment.Center)
                .fillMaxWidth(0.8f)
                .requiredHeight(50.dp)
                .offset(y = 93.dp),
            trailingIcon = {
                IconButton(onClick = { /* Handle icon click here */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.fingerprint), // Example: replace with any vector
                        contentDescription = "Search Icon",
                        tint = Color.Gray,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

        )


        Text(
            text = "Forgot your ID or password?",
            color = Color(0xff5875dd),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 16.sp),
            modifier = Modifier
                .align(alignment = Alignment.Center)
                .offset(y = 220.dp)
                .requiredWidth(width = 208.dp)
                .requiredHeight(height = 28.dp))
        Text(
            text = "Privacy",
            color = Color(0xffc33ba5),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 16.sp),
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 90.dp,
                    y = 591.dp)
                .requiredWidth(width = 80.dp)
                .requiredHeight(height = 19.dp))
        Text(
            text = "Help",
            color = Color(0xffb3cb54),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 16.sp),
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 190.dp,
                    y = 591.dp)
                .requiredWidth(width = 50.dp)
                .requiredHeight(height = 19.dp))
        DarkThemeTrue(
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 0.dp,
                    y = 818.dp))
        Button(
            onClick = { viewModel.login(email, password) },
            shape = RoundedCornerShape(7.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff65558f)),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
            modifier = Modifier
                .align(alignment = Alignment.Center)
                .offset(y = 170.dp)
                .requiredWidth(width = 268.dp)
                .requiredHeight(height = 42.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .requiredWidth(width = 268.dp)
                    .requiredHeight(height = 42.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Text(
                        text = "Log In",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 8.75.em,
                        style = TextStyle(
                            fontSize = 16.sp),
                        modifier = Modifier
                            .wrapContentHeight(align = Alignment.CenterVertically))
                }
            }
        }
        when (authState) {
            is AuthState.Loading -> CircularProgressIndicator()
            is AuthState.Error -> Text(
                (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
            is AuthState.Authenticated -> LaunchedEffect(Unit) {
                navController.navigate("main") {
                    popUpTo("authentication") { inclusive = true }
                }
            }
            else -> {}
        }

        Divider(
            color = Color(0xff5875dd).copy(alpha = 0.53f),
            modifier = Modifier
                .align(alignment = Alignment.Center)
                .offset(y = 250.dp)
                .requiredWidth(width = 20.dp)
                .rotate(degrees = 90f))
    }
}

@Composable
fun DarkThemeTrue(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .requiredWidth(width = 393.dp)
            .requiredHeight(height = 34.dp)
            .background(color = Color(0xffe5e5e5).copy(alpha = 0.29f))
    ) {
        Box(
            modifier = Modifier
                .align(alignment = Alignment.BottomCenter)
                .offset(x = 0.5.dp,
                    y = (-8).dp)
                .requiredWidth(width = 134.dp)
                .requiredHeight(height = 5.dp)
                .clip(shape = RoundedCornerShape(100.dp))
                .background(color = Color(0xff0f172a)))
    }
}

