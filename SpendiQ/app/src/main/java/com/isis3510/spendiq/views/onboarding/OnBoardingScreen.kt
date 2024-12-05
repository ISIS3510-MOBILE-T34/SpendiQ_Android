package com.isis3510.spendiq.views.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.isis3510.spendiq.R

@Composable
fun OnBoardingScreen(
    navController: NavController
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 28.dp, end = 28.dp, top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your AI Assistant",
                    color = Color(0xFF5875DD),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 23.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Using this software,you can ask any\n" +
                            "questions you have about your finances.\n" +
                            "Remember this are suggestions not a direct recommendation.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 15.sp,
                        color = Color(0xA3A3ACCC)
                    )
                )

                Image(
                    modifier = Modifier
                        .height(400.dp)
                        .padding(top = 84.dp)
                        .fillMaxWidth(),
                    painter = painterResource(id = R.drawable.img_on_boarding),
                    contentDescription = ""
                )
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 34.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5875DD)),
                onClick = {
                    navController.navigate("chatbot")
                }
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    text = "Continue",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    ),
                    color = Color.White
                )

                Image(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Default.ArrowForward,
                    colorFilter = ColorFilter.tint(color = Color.White),
                    contentDescription = ""
                )

            }
        }
    }
}

@Preview
@Composable
fun OnBoardingScreePreview() {
    OnBoardingScreen(navController = rememberNavController())
}