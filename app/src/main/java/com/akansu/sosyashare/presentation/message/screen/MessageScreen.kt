package com.akansu.sosyashare.presentation.message.screen

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
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
    val currentUser = currentUsername ?: "SosyaShare"

    var searchQuery by remember { mutableStateOf("") }

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
                    IconButton(onClick = { navController.navigate("new_message_screen") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.pen_square),
                            contentDescription = "New Message",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            NavigationBar(
                selectedItem = 0,
                onItemSelected = { /* Handle item selection */ },
                navController = navController,
                profilePictureUrl = currentUserProfilePictureUrl,
                modifier = Modifier.height(60.dp)
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
                                    val otherUserId = if (message.senderId == viewModel.currentUserId.value) message.receiverId else message.senderId
                                    navController.navigate("chat/$otherUserId")
                                },
                                otherUserId = if (message.senderId == viewModel.currentUserId.value) message.receiverId else message.senderId,
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

@Composable
fun MessageItem(
    message: Message,
    onItemClick: () -> Unit,
    otherUserId: String,
    messageViewModel: MessageViewModel,
    textColor: Color,
    accentColor: Color
) {
    var user by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(otherUserId) {
        user = messageViewModel.getUserById(otherUserId)
    }

    val profilePictureUrl = user?.profilePictureUrl
    val currentUserId = messageViewModel.currentUserId.collectAsState().value

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = poppinsFontFamily,
                        color = textColor.copy(alpha = 0.7f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f) // Mesaj metni alanı dolduracak şekilde
                )

                if (!message.isRead) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .offset(x = (-8).dp, y = (-3).dp)
                            .background(Color(0xFF1E88E5), CircleShape)
                    )
                }

            }
        }

        Spacer(modifier = Modifier.width(8.dp))
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
