package com.isis3510.spendiq.views.profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider.getUriForFile
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.isis3510.spendiq.R
import com.isis3510.spendiq.views.main.BottomNavigation
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && profileImageUri != null) {
            coroutineScope.launch {
                uploadImageToFirebase(profileImageUri!!)
            }
        }
    }

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userDoc = FirebaseFirestore.getInstance().collection("users").document(userId).get().await()
            userData = userDoc.data
            val imageUrl = userData?.get("profileImageUrl") as? String
            if (imageUrl != null) {
                profileImageUri = Uri.parse(imageUrl)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = { BottomNavigation(navController) { /* Add transaction logic */ } }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .clickable {
                        val uri = ComposeFileProvider.getImageUri(context)
                        profileImageUri = uri
                        cameraLauncher.launch(uri)
                    }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        profileImageUri ?: R.drawable.person24
                    ),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Data
            userData?.let { data ->
                Text(
                    text = data["fullName"] as? String ?: "",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))

                ProfileField("Full Name", data["fullName"] as? String ?: "", R.drawable.person24)
                ProfileField("Email Address", data["email"] as? String ?: "", R.drawable.email24)
                ProfileField("Phone Number", data["phoneNumber"] as? String ?: "", R.drawable.phone24)
                ProfileField("Birth Date", data["birthDate"] as? String ?: "", R.drawable.calendar24)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Joined ${(data["registrationDate"] as? com.google.firebase.Timestamp)?.toDate()?.toString() ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
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

suspend fun uploadImageToFirebase(uri: Uri) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user != null) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("profile_images/${user.uid}.jpg")
        val uploadTask = imageRef.putFile(uri).await()
        val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .update("profileImageUrl", downloadUrl).await()
    }
}

// You'll need to create this class in your project
object ComposeFileProvider {
    fun getImageUri(context: Context): Uri {
        val directory = File(context.cacheDir, "images")
        directory.mkdirs()
        val file = File.createTempFile(
            "selected_image_",
            ".jpg",
            directory
        )
        val authority = context.packageName + ".fileprovider"
        return getUriForFile(
            context,
            authority,
            file
        )
    }
}