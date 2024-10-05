package com.isis3510.spendiq.views

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
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
import com.isis3510.spendiq.views.main.MainContent
import com.isis3510.spendiq.ui.promos.PromosScreen
import com.isis3510.spendiq.views.accounts.AccountsScreen
import com.isis3510.spendiq.ui.profile.ProfileScreen
import com.isis3510.spendiq.views.splash.SplashScreen
import com.isis3510.spendiq.views.theme.SpendiQTheme
import com.isis3510.spendiq.viewmodel.AuthenticationViewModel
import com.isis3510.spendiq.views.auth.AuthenticationScreen
import com.isis3510.spendiq.views.auth.LoginScreen
import com.isis3510.spendiq.views.auth.RegisterScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission if not already enabled
        if (!isNotificationServiceEnabled()) {
            requestNotificationPermission()
        }

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
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }

    // Check if notification listener access is enabled
    private fun isNotificationServiceEnabled(): Boolean {
        val packageName = applicationContext.packageName
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabledListeners?.contains(packageName) == true
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
        composable("promos") { PromosScreen() }
        composable("accounts") { AccountsScreen(navController) }
        composable("profile") { ProfileScreen() }
    }
}
