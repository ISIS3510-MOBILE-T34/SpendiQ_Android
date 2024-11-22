package com.isis3510.spendiq.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.isis3510.spendiq.views.accounts.AccountsScreen
import com.isis3510.spendiq.views.main.MainContent
import com.isis3510.spendiq.views.splash.SplashScreen
import com.isis3510.spendiq.views.auth.AuthenticationScreen
import com.isis3510.spendiq.views.auth.LoginScreen
import com.isis3510.spendiq.views.auth.RegisterScreen
import com.isis3510.spendiq.views.profile.*
import com.isis3510.spendiq.views.theme.SpendiQTheme
import com.isis3510.spendiq.viewmodel.*
import com.isis3510.spendiq.views.accounts.AccountTransactionsScreen
import com.isis3510.spendiq.views.accounts.TransactionDetailsScreen
import com.isis3510.spendiq.views.offers.OffersScreen
import com.isis3510.spendiq.views.offers.SpecialSalesDetail

class MainActivity : FragmentActivity() {
    companion object {
        private const val TAG = "MainActivity" // Tag for logging purposes
    }

    // Array of permissions required by the app
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,  // Location permission
        Manifest.permission.POST_NOTIFICATIONS     // Notifications permission
    )

    // Launcher for requesting multiple permissions
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Log whether all required permissions were granted or some were denied
        if (permissions.all { it.value }) {
            Log.d(TAG, "All required permissions granted")
        } else {
            Log.d(TAG, "Some permissions were denied")
        }
    }

    // Launcher specifically for requesting location permission
    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            // Log the result of the location permission request
            if (isGranted) {
                Log.d(TAG, "Location permission granted")
            } else {
                Log.d(TAG, "Location permission denied")
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if notification service access is enabled; if not, request it
        if (!isNotificationServiceEnabled()) {
            requestNotificationPermission()
        }

        // Request location permission if it is not already granted
        requestLocationPermission()

        // Check if all required permissions are granted; if not, request them
        if (!hasRequiredPermissions()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }

        // Initialize the main content view with Jetpack Compose and theme setup
        setContent {
            SpendiQTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Set up navigation controller for managing screen navigation
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()
                    val accountViewModel: AccountViewModel = viewModel()
                    val transactionViewModel: TransactionViewModel = viewModel()
                    val offersViewModel: OffersViewModel = viewModel()
                    val profileViewModel: ProfileViewModel = viewModel()
                    val userViewModel: UserViewModel = viewModel()
                    val connectivityViewModel: ConnectivityViewModel = viewModel()
                    val userData by userViewModel.userData.collectAsState()

                    // Configure navigation destinations
                    NavHost(navController = navController, startDestination = "splash") {
                        // Define each composable destination with corresponding screen
                        composable("splash") {
                            SplashScreen(navController, authViewModel)
                        }
                        composable("authentication") {
                            AuthenticationScreen(navController)
                        }
                        composable("login") {
                            LoginScreen(navController, authViewModel, connectivityViewModel)
                        }
                        composable("register") {
                            RegisterScreen(navController, authViewModel)
                        }
                        composable("main") {
                            MainContent(navController, authViewModel, accountViewModel, offersViewModel, transactionViewModel, connectivityViewModel)
                        }
                        composable("promos") {
                            OffersScreen(navController, offersViewModel, transactionViewModel, accountViewModel)
                        }
                        composable("profile") {
                            ProfileScreen(navController, authViewModel, transactionViewModel, accountViewModel, profileViewModel)
                        }
                        composable("accounts") {
                            AccountsScreen(navController, accountViewModel, transactionViewModel)
                        }
                        composable("profileNotificationsScreen") {
                            ProfileNotificationsScreen(navController, transactionViewModel, accountViewModel)
                        }
                        composable("profileSecurityScreen") {
                            ProfileSecurityScreen(navController, transactionViewModel, accountViewModel)
                        }
                        composable("profileAccountScreen") {
                            ProfileAccountScreen(navController, userData, transactionViewModel, accountViewModel)
                        }
                        composable("profileLaGScreen") {
                            ProfileLaGScreen(navController, transactionViewModel, accountViewModel)
                        }
                        composable("profileStatisticsScreen") {
                            ProfileStatisticsScreen(navController, transactionViewModel, accountViewModel)
                        }
                        composable("profileHelpScreen") {
                            ProfileHelpScreen(navController, transactionViewModel, accountViewModel)
                        }
                        composable("profileInfoScreen") {
                            ProfileInfoScreen(navController, transactionViewModel, accountViewModel)
                        }
                        // Define destination for account transactions with accountId as argument
                        composable(
                            route = "accountTransactions/{accountId}",
                            arguments = listOf(navArgument("accountId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            AccountTransactionsScreen(
                                navController,
                                backStackEntry.arguments?.getString("accountId") ?: ""
                            )
                        }
                        // Define destination for special sales detail with offerId as argument
                        composable(
                            route = "specialSalesDetail/{offerId}",
                            arguments = listOf(navArgument("offerId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val offerId = backStackEntry.arguments?.getString("offerId")
                            if (offerId != null) {
                                // Fetch offer details based on the offerId
                                LaunchedEffect(offerId) {
                                    offersViewModel.getOfferById(offerId)
                                }

                                val selectedOffer by offersViewModel.selectedOffer.collectAsState()
                                val uiState by offersViewModel.uiState.collectAsState()

                                // Display loading, error, or success UI based on UI state
                                when (uiState) {
                                    is OffersViewModel.UiState.Loading -> {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }
                                    }
                                    is OffersViewModel.UiState.Error -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(16.dp)
                                        ) {
                                            Text(
                                                text = (uiState as OffersViewModel.UiState.Error).message,
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }
                                    }
                                    is OffersViewModel.UiState.Success -> {
                                        selectedOffer?.let { offer ->
                                            SpecialSalesDetail(
                                                offer = offer,
                                                navController = navController,
                                                accountViewModel = accountViewModel,
                                                transactionViewModel = transactionViewModel
                                            )
                                        }
                                    }
                                    else -> { /* No operation */ }
                                }
                            }
                        }
                        // Define destination for transaction details with accountId and transactionId as arguments
                        composable(
                            route = "transactionDetails/{accountId}/{transactionId}",
                            arguments = listOf(
                                navArgument("accountId") { type = NavType.StringType },
                                navArgument("transactionId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
                            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
                            TransactionDetailsScreen(
                                navController = navController,
                                accountViewModel = accountViewModel,
                                accountId = accountId,
                                transactionId = transactionId,
                                transactionViewModel = transactionViewModel
                            )
                        }
                    }
                }
            }
        }
    }

    // Check if all permissions in REQUIRED_PERMISSIONS array are granted
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun hasRequiredPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Function to request notification listener permission
    private fun requestNotificationPermission() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }

    // Function to check if notification service is enabled
    private fun isNotificationServiceEnabled(): Boolean {
        val packageName = applicationContext.packageName
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabledListeners?.contains(packageName) == true
    }

    // Function to request location permission if not already granted
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
