package com.isis3510.spendiq.views

import ConnectivityViewModel
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
import com.isis3510.spendiq.view.accounts.AccountsScreen
import com.isis3510.spendiq.views.main.MainContent
import com.isis3510.spendiq.view.splash.SplashScreen
import com.isis3510.spendiq.views.auth.AuthenticationScreen
import com.isis3510.spendiq.views.auth.LoginScreen
import com.isis3510.spendiq.views.auth.RegisterScreen
import com.isis3510.spendiq.views.profile.ProfileScreen
import com.isis3510.spendiq.views.profile.ProfileAccountScreen
import com.isis3510.spendiq.views.profile.ProfileHelpScreen
import com.isis3510.spendiq.views.profile.ProfileInfoScreen
import com.isis3510.spendiq.views.profile.ProfileLaGScreen
import com.isis3510.spendiq.views.profile.ProfileNotificationsScreen
import com.isis3510.spendiq.views.profile.ProfileStatisticsScreen
import com.isis3510.spendiq.views.theme.SpendiQTheme
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.AuthViewModel
import com.isis3510.spendiq.viewmodel.OffersViewModel
import com.isis3510.spendiq.viewmodel.TransactionViewModel
import com.isis3510.spendiq.viewmodel.ProfileViewModel
import com.isis3510.spendiq.views.accounts.AccountTransactionsScreen
import com.isis3510.spendiq.views.accounts.TransactionDetailsScreen
import com.isis3510.spendiq.views.offers.OffersScreen
import com.isis3510.spendiq.views.offers.SpecialSalesDetail
import com.isis3510.spendiq.views.profile.ProfileSecurityScreen

class MainActivity : FragmentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    // Permissions required for the app
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS
    )

    // Location permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            Log.d(TAG, "All required permissions granted")
        } else {
            Log.d(TAG, "Some permissions were denied")
        }
    }

    // Launcher for location permission request
    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d(TAG, "Location permission granted")
            } else {
                Log.d(TAG, "Location permission denied")
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if notifications service is enabled, if not, request
        if (!isNotificationServiceEnabled()) {
            requestNotificationPermission()
        }

        // Request location permission
        requestLocationPermission()

        // Check and request permissions if needed
        if (!hasRequiredPermissions()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }

        // Initialize the content view with Jetpack Compose and navigation
        setContent {
            SpendiQTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()
                    val accountViewModel: AccountViewModel = viewModel()
                    val transactionViewModel: TransactionViewModel = viewModel()
                    val offersViewModel: OffersViewModel = viewModel()
                    val connectivityViewModel: ConnectivityViewModel = viewModel()
                    val profileViewModel: ProfileViewModel = viewModel()

                    NavHost(navController = navController, startDestination = "splash") {
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
                            MainContent(navController, authViewModel, accountViewModel, offersViewModel, transactionViewModel)
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
                            ProfileNotificationsScreen(navController)
                        }
                        composable("profileSecurityScreen") {
                            ProfileSecurityScreen(navController)
                        }
                        composable("profileAccountScreen") {
                            ProfileAccountScreen(navController)
                        }
                        composable("profileLaGScreen") {
                            ProfileLaGScreen(navController)
                        }
                        composable("profileStatisticsScreen") {
                            ProfileStatisticsScreen(navController, transactionViewModel, accountViewModel)
                        }
                        composable("profileHelpScreen") {
                            ProfileHelpScreen(navController)
                        }
                        composable("profileInfoScreen") {
                            ProfileInfoScreen(navController)
                        }
                        composable(
                            route = "accountTransactions/{accountId}",
                            arguments = listOf(navArgument("accountId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            AccountTransactionsScreen(
                                navController,
                                backStackEntry.arguments?.getString("accountId") ?: ""
                            )
                        }
                        composable(
                            route = "specialSalesDetail/{offerId}",
                            arguments = listOf(navArgument("offerId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val offerId = backStackEntry.arguments?.getString("offerId")
                            if (offerId != null) {
                                LaunchedEffect(offerId) {
                                    offersViewModel.getOfferById(offerId)
                                }

                                val selectedOffer by offersViewModel.selectedOffer.collectAsState()
                                val uiState by offersViewModel.uiState.collectAsState()

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
                                                navController = navController
                                            )
                                        }
                                    }
                                    else -> { }
                                }
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

    // Function to check if all required permissions are granted
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

    // Function to request location permission
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
