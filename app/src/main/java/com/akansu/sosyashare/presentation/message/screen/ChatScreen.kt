package com.akansu.sosyashare.presentation.message.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.akansu.sosyashare.R
import com.akansu.sosyashare.domain.model.Message
import com.akansu.sosyashare.presentation.message.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavHostController,
    userId: String,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState(initial = emptyList())
    var newMessage by remember { mutableStateOf(TextFieldValue("")) }
    val currentUserId by viewModel.currentUserId.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadMessages(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Chat", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message = message, isOwnMessage = message.senderId == currentUserId)
                }
            }

            Row(modifier = Modifier.padding(16.dp)) {
                BasicTextField(
                    value = newMessage,
                    onValueChange = { newMessage = it },
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
                        .padding(8.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    viewModel.sendMessage(userId, newMessage.text)
                    newMessage = TextFieldValue("")
                }) {
                    Text("Send")
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message, isOwnMessage: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = message.senderId,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
            Box(
                modifier = Modifier
                    .background(
                        if (isOwnMessage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = message.content,
                    color = if (isOwnMessage) Color.White else MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (!isOwnMessage && !message.isRead) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF1E88E5), CircleShape)
                )
            } else if (!isOwnMessage && message.isRead) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check), // Assuming ic_check is the modern tick icon
                    contentDescription = "Read",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
