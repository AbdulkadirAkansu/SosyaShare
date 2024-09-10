package com.akansu.sosyashare.presentation.message.screen

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.akansu.sosyashare.R
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.presentation.home.components.NavigationBar
import com.akansu.sosyashare.presentation.message.viewmodel.NewMessageViewModel
import com.akansu.sosyashare.util.poppinsFontFamily

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMessageScreen(
    navController: NavHostController,
    viewModel: NewMessageViewModel = hiltViewModel(),
    messageContent: String? = null // forward edilen içerik
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val searchResults by viewModel.searchResults.collectAsState(initial = emptyList())
    val isDarkTheme = isSystemInDarkTheme()
    val errorState by viewModel.error.collectAsState()  // Error handling
    val context = LocalContext.current

    val backgroundColor = if (isDarkTheme) MaterialTheme.colorScheme.background else Color.White
    val textColor = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onBackground
    val currentUserProfilePictureUrl by viewModel.currentUserProfilePictureUrl.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yeni Mesaj", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        /* bottomBar = {
            NavigationBar(
                navController = navController,
                profilePictureUrl = currentUserProfilePictureUrl
            )
        }, */
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundColor)
                .padding(16.dp)
        ) {
            SearchBar(
                query = searchQuery.text,
                onQueryChange = { query ->
                    searchQuery = TextFieldValue(query)
                    viewModel.searchUsers(query)
                },
                textColor = textColor,
                backgroundColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else Color.LightGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(searchResults) { user ->
                    UserItem(
                        user = user,
                        textColor = textColor,
                        onClick = {
                            // Eğer forward edilen bir mesaj varsa onu ChatScreen'e ilet
                            messageContent?.let { content ->
                                navController.navigate("chat/${user.id}?forwardedMessage=${Uri.encode(content)}") {
                                    popUpTo("new_message_screen") { inclusive = true }
                                }
                            } ?: run {
                                // Eğer forward edilen mesaj yoksa direkt chat ekranına git
                                navController.navigate("chat/${user.id}") {
                                    popUpTo("new_message_screen") { inclusive = true }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    textColor: Color,
    backgroundColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Rounded.Search,
                contentDescription = "Search",
                tint = textColor.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                modifier = Modifier
                    .weight(1f)
                    .background(Color.Transparent)
            ) {
                if (query.isEmpty()) {
                    Text("Kullanıcı Ara...", color = textColor.copy(alpha = 0.5f))
                } else {
                    it()
                }
            }
        }
    }
}

@Composable
fun UserItem(user: User, textColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.profilePictureUrl ?: R.drawable.profile,
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = user.username,
            color = textColor,
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = poppinsFontFamily)
        )
    }
}
