package com.akansu.sosyashare.presentation.message.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
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

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = "Chat") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        LazyColumn(modifier = Modifier.weight(1f).padding(16.dp)) {
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
                    .background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.small)
                    .padding(8.dp)
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

@Composable
fun ChatBubble(message: Message, isOwnMessage: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = message.senderId, // Sender username
                color = Color.LightGray,
                style = MaterialTheme.typography.bodySmall
            )
            Box(
                modifier = Modifier
                    .background(
                        if (isOwnMessage) Color.Blue else Color.Gray,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = message.content,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
