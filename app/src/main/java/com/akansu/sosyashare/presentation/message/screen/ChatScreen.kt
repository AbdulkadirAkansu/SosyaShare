package com.akansu.sosyashare.presentation.message.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.akansu.sosyashare.R
import com.akansu.sosyashare.domain.model.Message
import com.akansu.sosyashare.presentation.message.viewmodel.ChatViewModel
import com.akansu.sosyashare.util.poppinsFontFamily
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavHostController,
    userId: String,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState(initial = emptyList())
    val otherUser by viewModel.otherUser.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var newMessage by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        viewModel.loadMessages(userId)
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                onBackClick = { navController.popBackStack() },
                userProfileUrl = otherUser?.profilePictureUrl,
                username = otherUser?.username
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                LazyColumn {
                    items(messages) { message ->
                        ChatBubble(
                            message = message,
                            isOwnMessage = message.senderId == currentUser?.id,
                            ownUserProfileUrl = currentUser?.profilePictureUrl,
                            otherUserProfileUrl = otherUser?.profilePictureUrl
                        )
                    }
                }
            }

            ChatInputField(
                value = newMessage,
                onValueChange = { newMessage = it },
                onSendClick = {
                    if (newMessage.isNotBlank()) {
                        viewModel.sendMessage(userId, newMessage)
                        newMessage = ""
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    onBackClick: () -> Unit,
    userProfileUrl: String?,
    username: String?
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleAvatar(
                    size = 40.dp,
                    imageUrl = userProfileUrl
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = username ?: "Unknown",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        fontFamily = poppinsFontFamily,
                        color = Color.White
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        },
        actions = {
            IconButton(onClick = { /* TODO: Handle more options */ }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "More", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = Color(0xFF121212)
        )
    )
}

@Composable
fun ChatBubble(
    message: Message,
    isOwnMessage: Boolean,
    ownUserProfileUrl: String?,
    otherUserProfileUrl: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        if (!isOwnMessage) {
            CircleAvatar(size = 32.dp, imageUrl = otherUserProfileUrl)
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .background(
                        if (isOwnMessage) Color(0xFFFFF6E2) else Color(0xFF252525),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = message.content,
                    color = if (isOwnMessage) Color.Black else Color.White,
                    fontSize = 14.sp,
                    fontFamily = poppinsFontFamily
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.timestamp),
                color = Color.Gray,
                fontSize = 10.sp,
                fontFamily = poppinsFontFamily,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        if (isOwnMessage) {
            Spacer(modifier = Modifier.width(8.dp))
            CircleAvatar(size = 32.dp, imageUrl = ownUserProfileUrl)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color(0xFF252525), RoundedCornerShape(24.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { /* TODO: Handle voice message */ }) {
            Icon(Icons.Rounded.MoreVert, contentDescription = "Voice", tint = Color(0xFFB388FF))
        }

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Message...", fontFamily = poppinsFontFamily, color = Color.Gray) },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontFamily = poppinsFontFamily)
        )

        IconButton(onClick = onSendClick) {
            Icon(Icons.Rounded.Send, contentDescription = "Send", tint = Color(0xFFB388FF))
        }
    }
}

@Composable
fun CircleAvatar(size: Dp, imageUrl: String?) {
    if (imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Avatar",
            modifier = Modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "Default Avatar",
            modifier = Modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}