package com.isis3510.spendiq.views.register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.isis3510.spendiq.R
import com.isis3510.spendiq.viewmodel.AuthenticationViewModel
import com.isis3510.spendiq.viewmodel.AuthState
import com.isis3510.spendiq.views.theme.Purple40

@Composable
fun RegisterScreen(navController: NavController,
                   viewModel: AuthenticationViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    val (checkedState, onStateChange) = remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(7.dp))
            .background(color = Color.White)
    ) {
        Image(
            painter = painterResource(R.drawable.logoup),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 185.dp,
                    y = (-70).dp)
                .size(300.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.leftactionable),
            contentDescription = "Left Actionable",
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 16.dp,
                    y = 50.dp)
                .requiredSize(size = 24.dp)
                .clip(shape = RoundedCornerShape(7.dp))
        )
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create Free Account",
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 48.sp,
                    fontFamily = FontFamily.SansSerif),
                modifier = Modifier
                    .offset(x = (-2).dp,
                        y = 38.dp)
                    .requiredWidth(width = 393.dp)
                    .requiredHeight(height = 112.dp)
            )

            Spacer(modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp))

            Spacer(modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp))

            Spacer(modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp))

            OutlinedTextField(
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()

                    .padding(16.dp, 4.dp)
                    .border(
                        BorderStroke(width = 2.dp, color = Purple40),
                        shape = RoundedCornerShape(50)
                    ),
                value = email,
                onValueChange = {email = it},
                placeholder = {  Text(text = "E-mail") },
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email") }
            )

            OutlinedTextField(
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 4.dp)
                    .border(
                        BorderStroke(width = 2.dp, color = Purple40),
                        shape = RoundedCornerShape(50)
                    ),
                value = password,
                onValueChange = { password = it },
                placeholder = { Text(text = "Password") },
                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock1") },
                visualTransformation = PasswordVisualTransformation()
            )

            OutlinedTextField(
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 4.dp)
                    .border(
                        BorderStroke(width = 2.dp, color = Purple40),
                        shape = RoundedCornerShape(50)
                    ),
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text(text = "Confirm Password") },
                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock2") },
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = checkedState,
                        onValueChange = { onStateChange(!checkedState) },
                        role = Role.Checkbox
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = checkedState,
                    onCheckedChange = null
                )
                Text(
                    text = "Accept Terms & Conditions",
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp))

            Button(
                onClick = { viewModel.register(email, password) },
                shape = RoundedCornerShape(7.dp),
                modifier = Modifier
                    .requiredWidth(width = 280.dp)
                    .padding(16.dp, 0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff65558f)),
            ) {
                Text(
                    text = "Sign Up",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 1.43.em,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 20.sp,
                        letterSpacing = 0.1.sp),
                    modifier = Modifier
                        .wrapContentHeight(align = Alignment.CenterVertically)
                )
            }

            when (authState) {
                is AuthState.Loading -> CircularProgressIndicator()
                is AuthState.Error -> Text((authState as AuthState.Error).message, color = MaterialTheme.colorScheme.error)
                is AuthState.Authenticated -> LaunchedEffect(Unit) {
                    navController.navigate("main") {
                        popUpTo("authentication") { inclusive = true }
                    }
                }
                else -> {}
            }

        }
        Image(
            painter = painterResource(R.drawable.logodown),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-185).dp,
                    y = 500.dp)
                .size(300.dp)
        )

    }

}
