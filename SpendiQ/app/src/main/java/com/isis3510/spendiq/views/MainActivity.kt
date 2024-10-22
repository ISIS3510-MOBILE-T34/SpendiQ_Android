package com.isis3510.spendiq.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.isis3510.spendiq.viewmodel.AuthenticationViewModel
import com.isis3510.spendiq.views.accounts.AccountTransactionsScreen
import com.isis3510.spendiq.views.accounts.AccountsScreen
import com.isis3510.spendiq.views.auth.AuthenticationScreen
import com.isis3510.spendiq.views.auth.LoginScreen
import com.isis3510.spendiq.views.auth.RegisterScreen
import com.isis3510.spendiq.views.main.MainContent
import com.isis3510.spendiq.views.profile.ProfileScreen
import com.isis3510.spendiq.views.promos.PromosScreen
import com.isis3510.spendiq.views.splash.SplashScreen
import com.isis3510.spendiq.views.theme.SpendiQTheme

class MainActivity : ComponentActivity() {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check and request notification listener permission
        if (!isNotificationServiceEnabled()) {
            requestNotificationPermission()
        }

        // Request location permissions if not already granted
        requestLocationPermission()

        // Set up the content with Jetpack Compose
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

    // Request the user to enable notification listener permission
    private fun requestNotificationPermission() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }

    // Check if the notification listener service is enabled
    private fun isNotificationServiceEnabled(): Boolean {
        val packageName = applicationContext.packageName
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabledListeners?.contains(packageName) == true
    }

    // Request fine location permission if it is not granted
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    // Handle the result of location permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Location permission granted, continue with location-based services
                } else {
                    // Location permission denied, handle accordingly (e.g., show a message to the user)
                }
            }
        }
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
