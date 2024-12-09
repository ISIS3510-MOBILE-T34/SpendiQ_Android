package com.isis3510.spendiq.views.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.isis3510.spendiq.viewmodel.ChatbotViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.text.style.TextAlign
import com.google.firebase.analytics.FirebaseAnalytics
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.ConnectivityViewModel

@Composable
fun ChatbotView(
    navController: NavController,
    chatbotViewModel: ChatbotViewModel,
    connectivityViewModel: ConnectivityViewModel,
    firebaseAnalytics: FirebaseAnalytics,
    accountViewModel: AccountViewModel,
) {
    var userInput by remember { mutableStateOf("") }
    val isNetworkAvailable by connectivityViewModel.isConnected.observeAsState(true)
    val isBotActive = chatbotViewModel.isBotActive.value
    val currentMoney by accountViewModel.currentMoney.collectAsState()

    // Verificar el estado del bot al iniciar la vista
    LaunchedEffect(Unit) {
        chatbotViewModel.checkBotStatus()
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            ToolbarMessage(
                modifier = Modifier,
                navController = navController,
                isNetworkAvailable = isNetworkAvailable,
                isBotActive = isBotActive)
        },
        floatingActionButton = {
            WriteMessageCard(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                value = userInput,
                onValueChange = { userInput = it },
                onClickSend = {
                    if (userInput.isNotEmpty()) {
                        chatbotViewModel.sendMessage(userInput, firebaseAnalytics, currentMoney)
                        userInput = ""
                    }
                },
                isNetworkAvailable = isNetworkAvailable,
                isBotActive = isBotActive
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues = paddingValues)
                .fillMaxSize()
                .imePadding()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(space = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                items(chatbotViewModel.messages) { message ->
                    if (message.fromUser) {
                        MessengerItemCard(
                            modifier = Modifier.align(Alignment.End),
                            message = message.content
                        )
                    } else {
                        ReceiverMessageItemCard(message = message.content)
                    }
                }
            }
        }
    }
}

@Composable
fun ToolbarMessage(
    modifier: Modifier = Modifier,
    navController: NavController,
    isNetworkAvailable: Boolean,
    isBotActive: Boolean
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            IconButton(
                onClick = {
                    navController.navigate("main") { launchSingleTop = true }
                }
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            Column(
                modifier = Modifier.padding(start = 20.dp)
            ) {
                Text(
                    text = "GemiQ",
                    fontSize = 20.sp,
                    color = Color(0xFF5875DD),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(6.dp)
                            .background(color = if (!isNetworkAvailable) Color.Red else if (!isBotActive) Color(0xFFDAA70C) else Color(0xffb3cb54), shape = CircleShape)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = if (!isNetworkAvailable) "Offline" else if (!isBotActive) "Bot Inactive" else "Online",
                        color = if (!isNetworkAvailable) Color.Red else if (!isBotActive) Color(0xFFDAA70C) else Color(0xffb3cb54),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp),
            color = Color(0xA3A3ACCC),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteMessageCard(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onClickSend: () -> Unit,
    isNetworkAvailable: Boolean,
    isBotActive: Boolean
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 6.dp,
        color = Color.White,
        shape = RoundedCornerShape(30.dp),
    ) {
        TextField(
            modifier = Modifier.background(color = Color.White),
            value = value,
            onValueChange = { value ->
                onValueChange(value)
            },
            placeholder = {
                Text(
                    text = "Write your message",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xA3A3ACCC)
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = { onClickSend() },
                    enabled = isNetworkAvailable && isBotActive
                ){
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Sending Button",
                        modifier = Modifier
                            .size(24.dp),
                        tint = if (isNetworkAvailable && isBotActive) Color(0xFF5875DD) else Color(0xA1858586),
                    )
                }

            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                errorContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            )

        )
    }
}

@Composable
fun MessengerItemCard(
    modifier: Modifier = Modifier,
    message: String = ""
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF5875DD),
        shape = RoundedCornerShape(topStart = 25.dp, bottomEnd = 25.dp, bottomStart = 25.dp)
    ) {
        Text(
            modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            text = message,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge.copy(color = Color.White)
        )
    }
}

@Composable
fun ReceiverMessageItemCard(
    modifier: Modifier = Modifier,
    message: String = ""
) {
    Row(
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.Bottom),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Icon(
                Icons.Default.Face,
                "La Cara del Bot"
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp, bottomEnd = 25.dp),
            color = Color(0xFFA9B2C9)
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 24.dp),
                text = message,
                style = MaterialTheme.typography.labelLarge.copy(color = Color(0xFF505050))
            )
        }
    }
}
