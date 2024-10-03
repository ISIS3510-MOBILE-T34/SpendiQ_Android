package com.isis3510.spendiq.views.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isis3510.spendiq.R
import com.isis3510.spendiq.viewmodel.login.LogInViewModel
import com.isis3510.spendiq.views.theme.SpendiQTheme

@Composable
fun LogInScreen(
    openAndPopUp: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LogInViewModel = hiltViewModel()
) {
    val email = viewModel.email.collectAsState()
    val password = viewModel.password.collectAsState()
    Box(
//        modifier = modifier
//            .requiredWidth(width = 393.dp)
//            .requiredHeight(height = 852.dp)
//            .background(color = Color.White)
        modifier = modifier.fillMaxSize()
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

        TextField(
            value = email.value,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text(text = "Correo") },
            modifier = Modifier
                .align(alignment = Alignment.Center)
                .fillMaxWidth(0.8f)
                .requiredHeight(50.dp)
        )

        // Password TextField
        TextField(
            value = password.value,
            onValueChange = { viewModel.updatePassword(it) },
            label = { Text(text = "Contraseña") },
            modifier = Modifier
                .align(alignment = Alignment.Center)
                .fillMaxWidth(0.8f)
                .requiredHeight(50.dp)
                .offset(y = 93.dp)
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
            text = "Privacidad",
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
            text = "Ayuda",
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
            onClick = { viewModel.onLogInClick(openAndPopUp) },
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
                        text = "Ingresar",
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

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun UserLoginPreview() {
    SpendiQTheme {
        LogInScreen({_, _ ->})
    }
}