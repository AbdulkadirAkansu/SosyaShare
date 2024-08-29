package com.akansu.sosyashare.presentation.message.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.akansu.sosyashare.presentation.userprofile.viewmodel.UserViewModel

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    navController: NavHostController,
    viewModel: MessageViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val recentMessages by viewModel.recentMessages.collectAsState(initial = emptyList())
    val error by viewModel.error.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    val currentUsername by userViewModel.username.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRecentMessages() // Mesajları yükle
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentUsername ?: "",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("new_message_screen") }) {
                        Icon(Icons.Default.Edit, contentDescription = "New Message")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = error ?: "Unknown error", color = Color.Red)
                }
            } else {
                LazyColumn {
                    if (recentMessages.isEmpty()) {
                        item {
                            Text(
                                text = "No messages to display.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(recentMessages) { message ->
                            val otherUserId = if (message.senderId == userViewModel.userId.value) {
                                message.receiverId
                            } else {
                                message.senderId
                            }

                            MessageItem(
                                message = message,
                                onItemClick = {
                                    navController.navigate("chat/$otherUserId")
                                },
                                otherUserId = otherUserId,
                                messageViewModel = viewModel  // Bu kısmı düzelttik
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
    messageViewModel: MessageViewModel
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
                data = profilePictureUrl ?: R.drawable.profile, // Placeholder resim
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
                text = user?.username ?: "Bilinmeyen",  // Kullanıcı adı
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Text(
                text = message.content,  // Son mesaj içeriği
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis  // Uzun mesajları kes
            )
        }
    }
}
