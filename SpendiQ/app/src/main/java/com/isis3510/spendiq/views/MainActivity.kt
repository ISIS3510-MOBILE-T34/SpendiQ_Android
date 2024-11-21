package com.isis3510.spendiq.views

import android.Manifest
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.work.Configuration
import androidx.work.WorkManager
import com.isis3510.spendiq.model.repository.AccountRepository
import com.isis3510.spendiq.services.LocationBasedOfferService
import com.isis3510.spendiq.utils.DatabaseTestUtility
import com.isis3510.spendiq.views.accounts.*
import com.isis3510.spendiq.views.auth.*
import com.isis3510.spendiq.views.main.MainContent
import com.isis3510.spendiq.views.offers.*
import com.isis3510.spendiq.views.profile.*
import com.isis3510.spendiq.views.splash.SplashScreen
import com.isis3510.spendiq.views.theme.SpendiQTheme
import com.isis3510.spendiq.viewmodel.*

class MainActivity : FragmentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var locationService: LocationBasedOfferService

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS
    )

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            Log.d(TAG, "All initial permissions granted")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestBackgroundLocationWithRationale()
            } else {
                initializeLocationService()
            }
        } else {
            Log.d(TAG, "Some permissions were denied")
            showPermissionSettingsDialog()
        }
    }

    private val requestBackgroundLocationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Background location permission granted")
            initializeLocationService()
        } else {
            Log.d(TAG, "Background location permission denied")
            showPermissionSettingsDialog()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure WorkManager
        val workManagerConfig = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()
        WorkManager.initialize(this, workManagerConfig)

        AccountRepository.initialize(this)

        // Initialize LocationService
        locationService = LocationBasedOfferService(this)

        // Request initial permissions
        if (!hasRequiredPermissions()) {
            requestPermissionLauncher.launch(requiredPermissions)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasBackgroundLocationPermission()) {
                requestBackgroundLocationWithRationale()
            } else {
                initializeLocationService()
            }
        }

        // Ensure notification service is enabled
        if (!isNotificationServiceEnabled()) {
            showNotificationServiceDialog()
        }

        // Debug database content
        DatabaseTestUtility.verifyDatabaseContent(this)

        // Observe WorkManager tasks
        observeWorkInfo()

        // Initialize the app UI
        initializeUI()
    }

    private fun initializeLocationService() {
        Log.d(TAG, "Initializing location service")
        locationService.startMonitoring()
        DatabaseTestUtility.insertTestOffer(this)
    }

    private fun observeWorkInfo() {
        WorkManager.getInstance(this)
            .getWorkInfosByTagLiveData("location_notification_work")
            .observe(this) { workInfoList ->
                workInfoList?.forEach { workInfo ->
                    Log.d(TAG, "Work info state: ${workInfo.state}")
                }
            }
    }

    private fun requestBackgroundLocationWithRationale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AlertDialog.Builder(this)
                .setTitle("Background Location Required")
                .setMessage("This app needs background location access to notify you about nearby offers even when the app is closed. Please grant 'Allow all the time' in the next screen.")
                .setPositiveButton("OK") { _, _ ->
                    requestBackgroundLocationLauncher.launch(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    showPermissionSettingsDialog()
                }
                .create()
                .show()
        }
    }

    private fun showPermissionSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("Required permissions are needed for full app functionality. Please grant them in Settings.")
            .setPositiveButton("Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showNotificationServiceDialog() {
        AlertDialog.Builder(this)
            .setTitle("Notification Access Required")
            .setMessage("Please enable notification access for automatic transaction tracking.")
            .setPositiveButton("Enable") { _, _ ->
                requestNotificationPermission()
            }
            .setNegativeButton("Later") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            startActivity(this)
        }
    }

    private fun requestNotificationPermission() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        if (!flat.isNullOrEmpty()) {
            val names = flat.split(":").toTypedArray()
            for (name in names) {
                val componentName = ComponentName.unflattenFromString(name)
                if (componentName != null && componentName.packageName == pkgName) {
                    return true
                }
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun hasRequiredPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun hasBackgroundLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeUI() {
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
                    val profileViewModel: ProfileViewModel = viewModel()
                    val userViewModel: UserViewModel = viewModel()
                    val connectivityViewModel: ConnectivityViewModel = viewModel()
                    val userData by userViewModel.userData.collectAsState()

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
                            MainContent(navController, authViewModel, accountViewModel, transactionViewModel)
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

    override fun onDestroy() {
        super.onDestroy()
        // Service will continue running in the background
    }
}
