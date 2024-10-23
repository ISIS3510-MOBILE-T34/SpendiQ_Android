package com.isis3510.spendiq.views.profile

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.isis3510.spendiq.R
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.views.common.BottomNavigation
import com.isis3510.spendiq.viewmodel.AuthViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: AuthViewModel, accountViewModel: AccountViewModel) {
    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddTransactionModal by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && profileImageUri != null) {
            viewModel.uploadProfileImage(profileImageUri!!)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getUserData()
    }

    val userDataState by viewModel.userData.collectAsState()

    LaunchedEffect(userDataState) {
        when (userDataState) {
            is AuthViewModel.UserDataState.Success -> {
                userData = (userDataState as AuthViewModel.UserDataState.Success).data
                isLoading = false
            }
            is AuthViewModel.UserDataState.Error -> {
                Toast.makeText(context, "Failed to load user data: ${(userDataState as AuthViewModel.UserDataState.Error).message}", Toast.LENGTH_LONG).show()
                isLoading = false
            }
            AuthViewModel.UserDataState.Loading -> {
                isLoading = true
            }

            AuthViewModel.UserDataState.Idle -> TODO()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigation(
                navController = navController,
                accountViewModel
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .clickable {
                            val uri = ComposeFileProvider.getImageUri(context)
                            profileImageUri = uri
                            cameraLauncher.launch(uri)
                        }
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = userData?.get("profileImageUrl") ?: R.drawable.person24
                        ),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                userData?.let { data ->
                    Text(
                        text = (data["fullName"] as? String) ?: "",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    ProfileField("Full Name", (data["fullName"] as? String) ?: "", R.drawable.person24)
                    ProfileField("Email Address", (data["email"] as? String) ?: "", R.drawable.email24)
                    ProfileField("Phone Number", (data["phoneNumber"] as? String) ?: "", R.drawable.phone24)
                    ProfileField("Birth Date", (data["birthDate"] as? String) ?: "", R.drawable.calendar24)

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Joined " + (data["registrationDate"] as? com.google.firebase.Timestamp)?.toDate()?.let {
                            SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "ES")).format(it)
                        } ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

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
}

@Composable
fun ProfileField(label: String, value: String, iconResId: Int) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = label,
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}

object ComposeFileProvider {
    fun getImageUri(context: Context): Uri {
        val file = File(context.cacheDir, "profile_image.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
