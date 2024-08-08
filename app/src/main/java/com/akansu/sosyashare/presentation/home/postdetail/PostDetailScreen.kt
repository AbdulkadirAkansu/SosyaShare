package com.akansu.sosyashare.presentation.home.postdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.akansu.sosyashare.data.model.UserEntity
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.presentation.home.components.NavigationBar
import com.akansu.sosyashare.presentation.userprofile.viewmodel.UserProfileViewModel
import com.akansu.sosyashare.presentation.userprofile.viewmodel.UserViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    navController: NavHostController,
    userId: String,
    initialPostIndex: Int,
    userProfileViewModel: UserProfileViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    var user by remember { mutableStateOf<User?>(null) }
    val profilePictureUrl by userViewModel.profilePictureUrl.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(userId) {
        userProfileViewModel.getUserDetails(userId) {
            user = it
        }
    }

    LaunchedEffect(initialPostIndex) {
        listState.scrollToItem(initialPostIndex)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Gönderiler", color = MaterialTheme.colorScheme.onBackground)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                selectedItem = 0,
                onItemSelected = { /* TODO */ },
                navController = navController,
                profilePictureUrl = profilePictureUrl
            )
        },
        modifier = Modifier.padding(WindowInsets.systemBars.asPaddingValues())
    ) { paddingValues ->
        user?.let {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(it.posts.size) { index ->
                    PostContent(
                        post = it.posts[index],
                        comment = it.comments.getOrNull(index) ?: "",
                        username = it.username,
                        profilePictureUrl = it.profilePictureUrl,
                        timestamp = "1 saat önce", // timestamp'i buraya geçici olarak ekliyorum, gerçek veriyle değiştirilmeli
                        onLike = { /* TODO: Beğeni işlemi */ },
                        isLiked = false // Bu örnekte sabit false, değişken yapılabilir
                    )
                }
            }
        }
    }
}

@Composable
fun PostContent(
    post: String,
    comment: String,
    username: String,
    profilePictureUrl: String?,
    timestamp: String,
    onLike: () -> Unit,
    isLiked: Boolean
) {
    var isImageClicked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = profilePictureUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = username,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Post resmi
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable { isImageClicked = true }
        ) {
            AsyncImage(
                model = post,
                contentDescription = "Post Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Modal görüntüleme
        if (isImageClicked) {
            FullScreenImage(imageUrl = post, onDismiss = { isImageClicked = false })
        }

        // Beğeni, yorum ve paylaşım butonları
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InteractionButton(icon = Icons.Default.FavoriteBorder, onClick = onLike, isLiked = isLiked)
            InteractionButton(icon = Icons.Outlined.Create, onClick = { /* TODO: Yorum işlemi */ })
            InteractionButton(icon = Icons.Default.Send, onClick = { /* TODO: Paylaşım işlemi */ })
            Spacer(modifier = Modifier.weight(1f))
            InteractionButton(icon = Icons.Default.FavoriteBorder, onClick = { /* TODO: Kaydetme işlemi */ })
        }

        if (comment.isNotEmpty()) {
            Text(
                text = comment,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        Text(
            text = timestamp,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Left,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }
}

@Composable
fun FullScreenImage(imageUrl: String, onDismiss: () -> Unit) {
    val systemUiController = rememberSystemUiController()
    val isLight = !isSystemInDarkTheme()

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Black.copy(alpha = 0.8f),
            darkIcons = false
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable { onDismiss() }
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Full Screen Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            systemUiController.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = isLight
            )
        }
    }
}

@Composable
fun InteractionButton(icon: ImageVector, onClick: () -> Unit, isLiked: Boolean = false) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f), CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
    }
}
