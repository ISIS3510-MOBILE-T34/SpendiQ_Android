package com.isis3510.spendiq.views // Adjusted to match the file location based on your screenshot

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.isis3510.spendiq.view.accounts.AccountsScreen
import com.isis3510.spendiq.view.offers.SpecialSalesDetail
import com.isis3510.spendiq.views.main.MainContent
import com.isis3510.spendiq.view.splash.SplashScreen
import com.isis3510.spendiq.views.auth.AuthenticationScreen
import com.isis3510.spendiq.views.auth.LoginScreen
import com.isis3510.spendiq.views.auth.RegisterScreen
import com.isis3510.spendiq.views.profile.ProfileScreen
import com.isis3510.spendiq.views.theme.SpendiQTheme
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.AuthViewModel
import com.isis3510.spendiq.viewmodel.OffersViewModel
import com.isis3510.spendiq.views.accounts.AccountTransactionsScreen
import com.isis3510.spendiq.views.offers.OffersScreen

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // ActivityResultLauncher for requesting location permissions
    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, do something with location
                // TODO: Implement logic for when location permission is granted
            } else {
                // Permission denied, show a message to the user
                // TODO: Implement logic for when location permission is denied
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure notification service is enabled
        if (!isNotificationServiceEnabled()) {
            requestNotificationPermission()
        }

        // Request location permission
        requestLocationPermission()

        // Set the main content view
        setContent {
            SpendiQTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Create navigation controller and ViewModels
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()
                    val accountViewModel: AccountViewModel = viewModel()
                    val offersViewModel: OffersViewModel = viewModel()

                    // Set up the navigation host
                    NavHost(navController = navController, startDestination = "splash") {
                        composable("splash") { SplashScreen(navController, authViewModel) }
                        composable("authentication") { AuthenticationScreen(navController) }
                        composable("login") { LoginScreen(navController, authViewModel) }
                        composable("register") { RegisterScreen(navController, authViewModel) }
                        composable("main") {
                            MainContent(navController, authViewModel, accountViewModel, offersViewModel)
                        }
                        composable("promos") { OffersScreen(navController, offersViewModel) }
                        composable("profile") { ProfileScreen(navController, authViewModel) }
                        composable("accounts") { AccountsScreen(navController, accountViewModel) }
                        composable("accountTransactions/{accountName}") { backStackEntry ->
                            val accountName = backStackEntry.arguments?.getString("accountName") ?: ""
                            AccountTransactionsScreen(navController, accountViewModel, accountName)
                        }
                        // Add new route for special sales detail
                        composable("SpecialSalesDetail/{offerId}") { backStackEntry ->
                            val offerId = backStackEntry.arguments?.getString("offerId")
                            if (offerId != null) {
                                // Add logging to debug if offerId is received
                                Log.d(TAG, "Navigating to SpecialSalesDetailScreen with offerId: $offerId")

                                // Collect the offers from OffersViewModel
                                val offers = offersViewModel.offers.collectAsState().value
                                val offer = offers.find { it.id == offerId }

                                // Log if the offer is found or not
                                if (offer != null) {
                                    Log.d(TAG, "Offer found: ${offer.placeName}")
                                    SpecialSalesDetail(offer = offer)
                                } else {
                                    Log.e(TAG, "Offer not found for offerId: $offerId")
                                    // Optionally show an error screen or navigate back
                                }
                            } else {
                                Log.e(TAG, "offerId is null")
                            }
                        }
                    }
                }
            }
        }
    }

    // Method to request notification access permission
    private fun requestNotificationPermission() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }

    // Check if notification listener service is enabled
    private fun isNotificationServiceEnabled(): Boolean {
        val packageName = applicationContext.packageName
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabledListeners?.contains(packageName) == true
    }

    // Request location permission if not granted
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}
