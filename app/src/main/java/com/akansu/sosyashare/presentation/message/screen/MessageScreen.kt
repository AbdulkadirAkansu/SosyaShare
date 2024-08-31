package com.akansu.sosyashare.presentation.message.screen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.akansu.sosyashare.R
import com.akansu.sosyashare.domain.model.Message
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.presentation.message.viewmodel.MessageViewModel
import com.akansu.sosyashare.util.poppinsFontFamily

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    navController: NavHostController,
    viewModel: MessageViewModel = hiltViewModel()
) {
    val recentMessages by viewModel.recentMessages.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentUsername by viewModel.currentUsername.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()

    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black

    LaunchedEffect(Unit) {
        viewModel.loadRecentChats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentUsername ?: "Messages",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = poppinsFontFamily,
                            color = textColor
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("new_message_screen") }) {
                        Icon(Icons.Default.Edit, contentDescription = "New Message", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = backgroundColor
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundColor)
        ) {
            if (error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = error ?: "Unknown error", color = Color.Red, fontFamily = poppinsFontFamily)
                }
            } else {
                LazyColumn {
                    if (recentMessages.isEmpty()) {
                        item {
                            Text(
                                text = "No messages to display.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = poppinsFontFamily,
                                    color = textColor
                                ),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(recentMessages) { message ->
                            MessageItem(
                                message = message,
                                onItemClick = {
                                    navController.navigate("chat/${message.senderId}")
                                },
                                otherUserId = if (message.senderId == viewModel.currentUserId.value) message.receiverId else message.senderId,
                                messageViewModel = viewModel,
                                textColor = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItem(
    message: Message,
    onItemClick: () -> Unit,
    otherUserId: String,
    messageViewModel: MessageViewModel,
    textColor: Color
) {
    var user by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(otherUserId) {
        user = messageViewModel.getUserById(otherUserId)
    }

    val profilePictureUrl = user?.profilePictureUrl

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberImagePainter(
                data = profilePictureUrl ?: R.drawable.profile,
                builder = {
                    crossfade(true)
                    placeholder(R.drawable.chat)
                }
            ),
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Text(
                text = user?.username ?: "Unknown",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = poppinsFontFamily,
                    color = textColor
                )
            )
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = poppinsFontFamily,
                    color = textColor.copy(alpha = 0.7f)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
