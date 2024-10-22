package com.isis3510.spendiq.views.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
            modifier = Modifier
                .align(alignment = Alignment.TopCenter)
                .offset(y = 156.dp)
        )

        // Login Button
        Button(
            onClick = { navController.navigate("login") },
            shape = RoundedCornerShape(7.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff65558f)),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            modifier = Modifier
                .align(alignment = Alignment.Center)
                .offset(y = (-120).dp)
                .width(226.dp)
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

        // Register Button
        Button(
            onClick = { navController.navigate("register") },
            shape = RoundedCornerShape(7.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff65558f)),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
            modifier = Modifier
                .align(alignment = Alignment.Center)
                .offset(y = (-50).dp)
                .width(226.dp)
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