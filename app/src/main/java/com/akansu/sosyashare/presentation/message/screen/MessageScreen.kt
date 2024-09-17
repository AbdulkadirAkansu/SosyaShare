package com.akansu.sosyashare.presentation.message.screen

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.akansu.sosyashare.R
import com.akansu.sosyashare.domain.model.Message
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.presentation.home.components.NavigationBar
import com.akansu.sosyashare.presentation.message.viewmodel.MessageViewModel
import com.akansu.sosyashare.util.poppinsFontFamily
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    navController: NavHostController,
    viewModel: MessageViewModel = hiltViewModel()
) {
    val recentMessages by viewModel.recentMessages.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentUsername by viewModel.currentUsername.collectAsState()
    val currentUserProfilePictureUrl by viewModel.currentUserProfilePictureUrl.collectAsState()
    val currentUser = currentUsername ?: "Unknown"

    var searchQuery by remember { mutableStateOf("") }
    val selectedMessages = remember { mutableStateListOf<Message>() } // Seçilen mesajlar listesi

    LaunchedEffect(Unit) {
        viewModel.loadRecentChats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        AsyncImage(
                            model = currentUserProfilePictureUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = currentUser,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = poppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                        }
                    }
                },
                actions = {
                    if (selectedMessages.isNotEmpty()) {
                        IconButton(onClick = {
                            selectedMessages.forEach { message ->
                                val chatId = viewModel.getChatId(message.senderId, message.receiverId)
                                viewModel.deleteMessage(chatId, message.id)
                            }
                            selectedMessages.clear() // Tüm seçimi temizle
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.delete_icon),
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        IconButton(onClick = {
                            selectedMessages.clear() // İptal et
                        }) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Cancel",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    } else {
                        IconButton(onClick = {
                            navController.navigate("new_message")
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.square_pencil),
                                contentDescription = "New Message",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            NavigationBar(
                navController = navController,
                profilePictureUrl = currentUserProfilePictureUrl
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
            SearchBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    viewModel.searchChatsByUsername(it)
                },
                textColor = MaterialTheme.colorScheme.onBackground,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )

            val displayMessages = if (searchQuery.isEmpty()) recentMessages else searchResults

            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = "Recent Chats",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = error ?: "Unknown error",
                        color = Color.Red,
                        fontFamily = poppinsFontFamily
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (displayMessages.isEmpty()) {
                        item {
                            EmptyStateMessage(textColor = MaterialTheme.colorScheme.onBackground)
                        }
                    } else {
                        items(displayMessages) { message ->
                            MessageItem(
                                message = message,
                                onItemClick = {
                                    if (selectedMessages.isEmpty()) {
                                        val otherUserId = if (message.senderId == viewModel.currentUserId.value) message.receiverId else message.senderId
                                        navController.navigate("chat/$otherUserId")
                                    } else {
                                        toggleSelection(message, selectedMessages)
                                    }
                                },
                                onLongPress = {
                                    toggleSelection(message, selectedMessages)
                                },
                                isSelected = selectedMessages.contains(message),
                                messageViewModel = viewModel,
                                textColor = MaterialTheme.colorScheme.onBackground,
                                accentColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItem(
    message: Message,
    onItemClick: () -> Unit,
    onLongPress: () -> Unit,
    isSelected: Boolean,
    messageViewModel: MessageViewModel,
    textColor: Color,
    accentColor: Color
) {
    var user by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(message.senderId) {
        user = messageViewModel.getUserById(message.senderId)
    }

    val profilePictureUrl = user?.profilePictureUrl

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onItemClick,
                onLongClick = onLongPress
            )
            .background(if (isSelected) Color.Gray.copy(alpha = 0.2f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = profilePictureUrl,
                contentDescription = null,
                modifier = Modifier.size(48.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = user?.username ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                )

                val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val timeString = dateFormat.format(message.timestamp)

                Text(
                    text = timeString,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = poppinsFontFamily,
                        color = textColor.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

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

fun toggleSelection(message: Message, selectedMessages: MutableList<Message>) {
    if (selectedMessages.contains(message)) {
        selectedMessages.remove(message)
    } else {
        selectedMessages.add(message)
    }
}


@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    textColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clip(RoundedCornerShape(30))
            .background(backgroundColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                Icons.Rounded.Search,
                contentDescription = "Search",
                tint = textColor.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.width(5.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                modifier = Modifier
                    .weight(1f)
                    .background(Color.Transparent)
            )
        }
    }
}

@Composable
fun EmptyStateMessage(textColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_check),
            contentDescription = "No messages",
            tint = textColor.copy(alpha = 0.5f),
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No messages yet",
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = textColor
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Start a conversation with your friends!",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = poppinsFontFamily,
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.7f)
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
