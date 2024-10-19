package com.isis3510.spendiq.views

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
            .requiredWidth(width = 393.dp)
            .requiredHeight(height = 852.dp)
            .clip(shape = RoundedCornerShape(7.dp))
            .background(color = Color.White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logogroupstart),
            contentDescription = "Logo Group",
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = (-63).dp,
                    y = 376.dp)
                .requiredWidth(width = 488.dp)
                .requiredHeight(height = 470.dp))
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
                .align(alignment = Alignment.TopStart)
                .offset(x = 57.dp,
                    y = 156.dp)
                .requiredWidth(width = 281.dp)
                .requiredHeight(height = 72.dp))
        DarkThemeTrue(
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 0.dp,
                    y = 818.dp))
        Button(
            onClick = {navController.navigate("login")},
            shape = RoundedCornerShape(7.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff65558f)),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            modifier = Modifier
                .align(alignment = Alignment.Center)
                .offset(y = (-120).dp)
                .requiredWidth(width = 226.dp)
                .requiredHeight(height = 42.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .requiredWidth(width = 226.dp)
                    .requiredHeight(height = 42.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(shape = RoundedCornerShape(7.dp))
                ) {
                    Text(
                        text = "Log In",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 1.43.em,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 0.1.sp),
                        modifier = Modifier
                            .wrapContentHeight(align = Alignment.CenterVertically))
                }
            }
        }
        Button(
            onClick = {navController.navigate("register")},
            shape = RoundedCornerShape(7.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff65558f)),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
            modifier = Modifier
                .align(alignment = Alignment.Center)
                .offset(y = (-50).dp)
                .requiredWidth(width = 226.dp)
                .requiredHeight(height = 42.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .requiredWidth(width = 226.dp)
                    .requiredHeight(height = 42.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Text(
                        text = "Sign Up",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 1.43.em,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 0.1.sp),
                        modifier = Modifier
                            .wrapContentHeight(align = Alignment.CenterVertically))
                }
            }
        }
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