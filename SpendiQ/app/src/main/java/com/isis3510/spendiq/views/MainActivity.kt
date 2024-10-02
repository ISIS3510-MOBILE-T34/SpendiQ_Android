package com.isis3510.spendiq.views

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.isis3510.spendiq.viewmodel.AuthenticationViewModel
import com.isis3510.spendiq.views.login.LoginScreen
import com.isis3510.spendiq.views.register.RegisterScreen
import com.isis3510.spendiq.views.theme.SpendiQTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Optionally, guide the user to enable Notification Listener settings if required
        requestNotificationPermission()

        setContent {
            SpendiQTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    // This function guides the user to notification access settings if required
    private fun requestNotificationPermission() {
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        startActivity(intent)
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val viewModel: AuthenticationViewModel = viewModel()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController, viewModel) }
        composable("authentication") { AuthenticationScreen(navController) }
        composable("login") { LoginScreen(navController, viewModel) }
        composable("register") { RegisterScreen(navController, viewModel) }
        composable("main") { MainContent(navController, viewModel) }
    }
}
