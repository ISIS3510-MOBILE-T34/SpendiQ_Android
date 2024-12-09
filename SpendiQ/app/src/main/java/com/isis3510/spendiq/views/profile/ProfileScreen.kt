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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.data.UiToolingDataApi
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.isis3510.spendiq.R
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.AuthViewModel
import com.isis3510.spendiq.viewmodel.ConnectivityViewModel
import com.isis3510.spendiq.viewmodel.ProfileViewModel
import com.isis3510.spendiq.viewmodel.TransactionViewModel
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
    profileViewModel: ProfileViewModel,
    connectivityViewModel: ConnectivityViewModel
) {
    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    val profileImageUri by profileViewModel.profileImageUri.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val isNetworkAvailable by connectivityViewModel.isConnected.observeAsState(true)

    var locationText by remember { mutableStateOf("Location not available") }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { newUri ->
            saveImageToInternalStorage(context, newUri)?.let { savedUri ->
                profileViewModel.saveProfileImage(context, savedUri)
            } ?: run {
                Toast.makeText(context, "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
            }
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
            Column {
                ConnectivityBanner(isConnected = isNetworkAvailable)
                TopAppBar(
                    title = {
                        Box(Modifier.fillMaxWidth()) {
                            Text(
                                "Profile",
                                textAlign = TextAlign.Start,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.round_arrow_back_ios_24),
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = { Spacer(modifier = Modifier.width(48.dp)) }
                )
            }
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
        // Sección de Notificaciones, Seguridad y Cuenta
        Box(modifier = Modifier.padding(vertical = 8.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEEEEEE))
                    .padding(16.dp)
            ) {
                // Botón Notifications
                ActionButtonWithArrow(
                    text = "Notifications",
                    iconResId = R.drawable.baseline_notifications_24,
                    navController = navController,
                    backgroundColor = Color(0xFFC33BA5),
                    textColor = Color.Black,
                    enabled = false
                ) {
                    navController.navigate("profileNotificationsScreen") { launchSingleTop = false }
                }
                Divider(color = Color(0xFFC5C5C5), thickness = 1.dp)

                // Botón Security - Deshabilitado
                ActionButtonWithArrow(
                    text = "Security",
                    iconResId = R.drawable.baseline_shield_24,
                    navController = navController,
                    backgroundColor = Color(0xFFC33BA5),
                    textColor = Color.Black,
                    enabled = false // Deshabilitado
                ) {
                    // Acción deshabilitada, no se ejecutará
                }
                Divider(color = Color(0xFFC5C5C5), thickness = 1.dp)

                // Botón Account - Habilitado (opcional, ya que es true por defecto)
                ActionButtonWithArrow(
                    text = "Account",
                    iconResId = R.drawable.person24,
                    navController = navController,
                    backgroundColor = Color(0xFFC33BA5),
                    textColor = Color.Black
                    // enabled = true por defecto
                ) {
                    navController.navigate("profileAccountScreen") { launchSingleTop = true }
                }
            }
        }

        // Sección de Limites y Estadísticas
        Box(modifier = Modifier.padding(vertical = 8.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEEEEEE))
                    .padding(16.dp)
            ) {
                ActionButtonWithArrow(
                    text = "Limits and Goals",
                    iconResId = R.drawable.baseline_adjust_24,
                    navController = navController,
                    backgroundColor = Color(0xFFB3CB54),
                    textColor = Color.Black
                ) {
                    navController.navigate("profileLaGScreen") { launchSingleTop = true }
                }
                Divider(color = Color(0xFFC5C5C5), thickness = 1.dp)
                ActionButtonWithArrow(
                    text = "Statistics",
                    iconResId = R.drawable.round_equalizer_24,
                    navController = navController,
                    backgroundColor = Color(0xFFB3CB54),
                    textColor = Color.Black
                ) {
                    navController.navigate("profileStatisticsScreen") { launchSingleTop = true }
                }
            }
        }

        // Sección de Ayuda e Información
        Box(modifier = Modifier.padding(vertical = 8.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEEEEEE))
                    .padding(16.dp)
            ) {
                // Botón Help - Deshabilitado
                ActionButtonWithArrow(
                    text = "Help",
                    iconResId = R.drawable.outline_question_mark_24,
                    navController = navController,
                    backgroundColor = Color(0xFF5875DD),
                    textColor = Color.Black,
                    enabled = false // Deshabilitado
                ) {
                    // Acción deshabilitada, no se ejecutará
                }
                Divider(color = Color(0xFFC5C5C5), thickness = 1.dp)

                // Botón Information - Deshabilitado
                ActionButtonWithArrow(
                    text = "Information",
                    iconResId = R.drawable.sharp_info_outline_24,
                    navController = navController,
                    backgroundColor = Color(0xFF5875DD),
                    textColor = Color.Black,
                    enabled = false // Deshabilitado
                ) {
                    // Acción deshabilitada, no se ejecutará
                }
            }
        }
    }
}

@Composable
fun ActionButtonWithArrow(
    text: String,
    iconResId: Int,
    navController: NavController,
    backgroundColor: Color = Color(0xFFB3CB54),
    textColor: Color = Color.Black,
    enabled: Boolean = true, // Nuevo parámetro
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (enabled) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(vertical = 12.dp)
            .alpha(if (enabled) 1f else 0.5f), // Cambia la opacidad para indicar estado deshabilitado
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(backgroundColor),
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
        if (enabled) { // Solo muestra la flecha si está habilitado
            Icon(
                painter = painterResource(id = R.drawable.round_arrow_forward_ios_24),
                contentDescription = "Arrow",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
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

        Box(
            modifier = Modifier
                .size(100.dp)
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


@SuppressLint("MissingPermission")
suspend fun updateLocation(context: Context, onLocationUpdated: (String) -> Unit) {
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    withContext(Dispatchers.IO) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                if (addresses != null) {
                    if (addresses.isNotEmpty()) {
                        val city = addresses[0].locality ?: "Unknown city"
                        val country = addresses[0].countryName ?: "Unknown country"
                        onLocationUpdated("$city, $country")
                    }
                }
            }
        }
    }
}


fun saveImageToInternalStorage(context: Context, uri: Uri): Uri? {
    val filename = "profile_image.png"
    val file = File(context.filesDir, filename)
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        Uri.fromFile(file)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}


