package com.isis3510.spendiq.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.isis3510.spendiq.views.theme.SpendiQTheme
import dagger.hilt.android.AndroidEntryPoint

//@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpendiQTheme {
                MainScreen()
            }
        }
    }
}
