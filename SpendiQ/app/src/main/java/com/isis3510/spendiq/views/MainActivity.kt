package com.isis3510.spendiq.views

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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
import com.isis3510.spendiq.views.accounts.TransactionDetailsScreen
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
                        composable("promos") { OffersScreen(navController, offersViewModel, accountViewModel) }
                        composable("profile") { ProfileScreen(navController, authViewModel, accountViewModel) }
                        composable("accounts") { AccountsScreen(navController, accountViewModel) }
                        composable("accountTransactions/{accountId}") { backStackEntry ->
                            val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
                            AccountTransactionsScreen(navController, accountId)
                        }
                        // Special sales detail route
                        composable("SpecialSalesDetail/{offerId}") { backStackEntry ->
                            val offerId = backStackEntry.arguments?.getString("offerId")
                            if (offerId != null) {
                                Log.d(TAG, "Navigating to SpecialSalesDetailScreen with offerId: $offerId")

                                val offers = offersViewModel.offers.collectAsState().value
                                val offer = offers.find { it.id == offerId }

                                if (offer != null) {
                                    Log.d(TAG, "Offer found: ${offer.placeName}")
                                    SpecialSalesDetail(offer = offer)
                                } else {
                                    Log.e(TAG, "Offer not found for offerId: $offerId")
                                }
                            } else {
                                Log.e(TAG, "offerId is null")
                            }
                        }
                        composable(
                            route = "transactionDetails/{accountId}/{transactionId}",
                            arguments = listOf(
                                navArgument("accountId") { type = NavType.StringType },
                                navArgument("transactionId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
                            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
                            Log.d(TAG, "Navigating to TransactionDetailsScreen with accountId: $accountId and transactionId: $transactionId")
                            TransactionDetailsScreen(
                                navController = navController,
                                accountViewModel = accountViewModel,
                                accountId = accountId,  // Corrected to use the accountId (Firestore document ID)
                                transactionId = transactionId
                            )
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
