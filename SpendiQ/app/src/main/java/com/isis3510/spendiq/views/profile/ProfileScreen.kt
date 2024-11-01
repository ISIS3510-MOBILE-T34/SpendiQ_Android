package com.isis3510.spendiq.views.profile

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.data.UiToolingDataApi
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.isis3510.spendiq.R
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.AuthViewModel
import com.isis3510.spendiq.viewmodel.TransactionViewModel
import com.isis3510.spendiq.viewmodel.ProfileViewModel
import com.isis3510.spendiq.views.common.BottomNavigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: AuthViewModel,
    transactionViewModel: TransactionViewModel,
    accountViewModel: AccountViewModel,
    profileViewModel: ProfileViewModel
) {
    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    val profileImageUri by profileViewModel.profileImageUri.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    var locationText by remember { mutableStateOf("Location not available") }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { newUri ->
            saveImageToInternalStorage(context, newUri)
            profileViewModel.saveProfileImage(context, newUri)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getUserData()
        profileViewModel.loadProfileImage(context)
        updateLocation(context) { location -> locationText = location }
    }

    val userDataState by viewModel.userData.collectAsState()
    LaunchedEffect(userDataState) {
        when (userDataState) {
            is AuthViewModel.UserDataState.Success -> {
                userData = (userDataState as AuthViewModel.UserDataState.Success).data
                isLoading = false
            }
            is AuthViewModel.UserDataState.Error -> {
                Toast.makeText(
                    context,
                    "Error loading data: ${(userDataState as AuthViewModel.UserDataState.Error).message}",
                    Toast.LENGTH_LONG
                ).show()
                isLoading = false
            }
            AuthViewModel.UserDataState.Loading -> isLoading = true
            AuthViewModel.UserDataState.Idle -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("main") { launchSingleTop = true } }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigation(
                navController = navController,
                transactionViewModel = transactionViewModel,
                accountViewModel = accountViewModel
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clickable {
                            galleryLauncher.launch("image/*")
                        }
                ) {
                    ProfileImageWithMultiColorBorder(profileImageUri)
                }

                Spacer(modifier = Modifier.height(12.dp))

                userData?.let { data ->
                    Text(
                        text = (data["fullName"] as? String) ?: "",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_location_pin_24),
                            contentDescription = "Location",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = locationText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section with buttons
                SectionWithButtons(navController)

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.logout()
                        navController.navigate("authentication") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Logout")
                }
            }
        }
    }
}

@Composable
fun SectionWithButtons(navController: NavController) {
    Column {
        // Notifications, Security, and Account Section
        Box(modifier = Modifier.padding(vertical = 8.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEEEEEE))
                    .padding(16.dp)
            ) {
                ActionButtonWithArrow("Notifications", R.drawable.baseline_notifications_24, navController, backgroundColor = Color(0xFFC33BA5), textColor = Color.Black) {
                    navController.navigate("profileNotificationsScreen") { launchSingleTop = true }
                }
                Divider(color = Color(0xFFC5C5C5), thickness = 1.dp)
                ActionButtonWithArrow("Security", R.drawable.baseline_shield_24, navController, backgroundColor = Color(0xFFC33BA5), textColor = Color.Black) {
                    navController.navigate("profileSecurityScreen") { launchSingleTop = true }
                }
                Divider(color = Color(0xFFC5C5C5), thickness = 1.dp)
                ActionButtonWithArrow("Account", R.drawable.person24, navController, backgroundColor = Color(0xFFC33BA5), textColor = Color.Black) {
                    navController.navigate("profileAccountScreen") { launchSingleTop = true }
                }
            }
        }

        // Limits and Goals, Statistics Section
        Box(modifier = Modifier.padding(vertical = 8.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEEEEEE))
                    .padding(16.dp)
            ) {
                ActionButtonWithArrow("Limits and Goals", R.drawable.baseline_adjust_24, navController, backgroundColor = Color(0xFFB3CB54), textColor = Color.Black) {
                    navController.navigate("profileLaGScreen") { launchSingleTop = true }
                }
                Divider(color = Color(0xFFC5C5C5), thickness = 1.dp)
                ActionButtonWithArrow("Statistics", R.drawable.round_equalizer_24, navController, backgroundColor = Color(0xFFB3CB54), textColor = Color.Black) {
                    navController.navigate("profileStatisticsScreen") { launchSingleTop = true }
                }
            }
        }

        // Help and Information Section
        Box(modifier = Modifier.padding(vertical = 8.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEEEEEE))
                    .padding(16.dp)
            ) {
                ActionButtonWithArrow("Help", R.drawable.outline_question_mark_24, navController, backgroundColor = Color(0xFF5875DD), textColor = Color.Black) {
                    navController.navigate("profileHelpScreen") { launchSingleTop = true }
                }
                Divider(color = Color(0xFFC5C5C5), thickness = 1.dp)
                ActionButtonWithArrow("Information", R.drawable.sharp_info_outline_24, navController, backgroundColor = Color(0xFF5875DD), textColor = Color.Black) {
                    navController.navigate("profileInfoScreen") { launchSingleTop = true }
                }
            }
        }
    }
}

@Composable
fun ActionButtonWithArrow(text: String, iconResId: Int, navController: NavController, backgroundColor: Color = Color(0xFFB3CB54), textColor: Color = Color.Black, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(backgroundColor), // Circular background for the icon
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(id = R.drawable.round_arrow_forward_ios_24),
            contentDescription = "Arrow",
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
    }
}

@SuppressLint("ResourceAsColor")
@Composable
fun ProfileImageWithMultiColorBorder(profileImageUri: Uri?) {
    Box(
        modifier = Modifier
            .size(106.dp) // Ajuste para dar espacio a los bordes
            .clip(CircleShape)
    ) {
        // Dibujo de los arcos de colores
        Canvas(modifier = Modifier.matchParentSize()) {
            drawArc(
                color = Color(0xFFB3CB54),
                startAngle = 0f,
                sweepAngle = 90f,
                useCenter = false,
                style = Stroke(8.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = Color(0xFFC33BA5),
                startAngle = 90f,
                sweepAngle = 90f,
                useCenter = false,
                style = Stroke(8.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = Color(0xFFB3CB54),
                startAngle = 180f,
                sweepAngle = 90f,
                useCenter = false,
                style = Stroke(8.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = Color(0xFFC33BA5),
                startAngle = 270f,
                sweepAngle = 90f,
                useCenter = false,
                style = Stroke(8.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Imagen de perfil (si existe)
        Box(
            modifier = Modifier
                .size(100.dp) // Tamaño más pequeño para la imagen de perfil, respetando los bordes
                .clip(CircleShape)
                .align(Alignment.Center)
                .background(Color.Gray), // Fondo gris por defecto
            contentAlignment = Alignment.Center
        ) {
            if (profileImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(profileImageUri),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.baseline_add_a_photo_24),
                    contentDescription = "Add Photo",
                    modifier = Modifier.size(50.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }
    }
}

@Composable
fun ActionButton(text: String) {
    Button(onClick = { /* Acciones de botón */ }) {
        Text(text)
    }
}


@OptIn(UiToolingDataApi::class)
@SuppressLint("MissingPermission") // Asegúrate de manejar permisos en el nivel de actividad
suspend fun updateLocation(context: Context, onLocationUpdated: (String) -> Unit) {
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    withContext(Dispatchers.IO) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                if (addresses != null) {
                    if (addresses.isNotEmpty()) {
                        val city = addresses[0].locality ?: "Ciudad desconocida"
                        val country = addresses[0].countryName ?: "País desconocido"
                        onLocationUpdated("$city, $country")
                    }
                }
            }
        }
    }
}

fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): Uri? {
    val filename = "profile_image.png"
    val file = File(context.filesDir, filename)
    return try {
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        Uri.fromFile(file)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

// Función para guardar la imagen en el almacenamiento interno
fun saveImageToInternalStorage(context: Context, uri: Uri) {
    val file = File(context.filesDir, "profile_image.png")
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }
}

// Función para guardar la URI de la imagen de perfil de manera persistente
fun saveProfileImageUri(context: Context, uri: Uri) {
    val sharedPreferences = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString("profile_image_uri", uri.toString()).apply()
}

// Función para obtener la URI de la imagen de perfil almacenada de manera persistente
fun getProfileImageUri(context: Context): Uri? {
    val sharedPreferences = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    val uriString = sharedPreferences.getString("profile_image_uri", null)
    return uriString?.let { Uri.parse(it) }
}
