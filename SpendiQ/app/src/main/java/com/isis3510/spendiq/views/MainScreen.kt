package com.isis3510.spendiq.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.isis3510.spendiq.views.login.LogInScreen
import com.isis3510.spendiq.views.theme.SpendiQTheme

@Composable
fun MainScreen() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Greeting(
            name = "Android",
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Heslo $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SpendiQTheme {
        MainScreen()
    }
}
