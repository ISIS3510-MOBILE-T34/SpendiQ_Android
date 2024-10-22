package com.isis3510.spendiq.views

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.isis3510.spendiq.viewmodel.AuthenticationViewModel
import com.isis3510.spendiq.views.auth.AuthenticationScreen
import com.isis3510.spendiq.views.auth.LoginScreen
import com.isis3510.spendiq.views.auth.RegisterScreen
import com.isis3510.spendiq.views.accounts.AccountTransactionsScreen
import com.isis3510.spendiq.views.accounts.AccountsScreen
import com.isis3510.spendiq.views.main.MainContent
import com.isis3510.spendiq.views.profile.ProfileScreen
import com.isis3510.spendiq.views.promos.PromosScreen
import com.isis3510.spendiq.views.splash.SplashScreen
import com.isis3510.spendiq.views.theme.SpendiQTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    private fun requestNotificationPermission() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }

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
        composable("promos") { PromosScreen(navController) }
        composable("profile") { ProfileScreen(navController, viewModel) }
        composable("accounts") { AccountsScreen(navController) }
        composable("accountTransactions/{accountName}") { backStackEntry ->
            val accountName = backStackEntry.arguments?.getString("accountName") ?: ""
            AccountTransactionsScreen(navController, accountName)
        }
    }
}