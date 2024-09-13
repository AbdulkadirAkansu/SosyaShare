package com.akansu.sosyashare.presentation.postdetail.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.akansu.sosyashare.R
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.presentation.home.components.LikedUsersDialog
import com.akansu.sosyashare.presentation.home.components.NavigationBar
import com.akansu.sosyashare.presentation.home.viewmodel.HomeViewModel
import com.akansu.sosyashare.presentation.postdetail.viewmodel.PostDetailViewModel
import com.akansu.sosyashare.util.poppinsFontFamily
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    navController: NavHostController,
    userId: String,
    initialPostIndex: Int,
    showSaveIcon: Boolean,
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel() // Shared ViewModel
) {
    val user by postDetailViewModel.user.collectAsState()
    val posts by postDetailViewModel.posts.collectAsState()
    val savedPosts by homeViewModel.savedPosts.collectAsState() // Shared state
    val listState = rememberLazyListState()
    val currentUserId by postDetailViewModel.currentUserId.collectAsState()
    var showLikedUsers by remember { mutableStateOf(false) }

    // Postları en son eklenenden başlayarak sıralıyoruz
    val sortedPosts = posts.sortedByDescending { it.createdAt }

    LaunchedEffect(userId) {
        postDetailViewModel.loadUserDetails(userId)
    }

    LaunchedEffect(initialPostIndex) {
        listState.scrollToItem(initialPostIndex)
    }

    if (showLikedUsers) {
        LikedUsersDialog(
            users = postDetailViewModel.likedUsers.collectAsState().value,
            onDismiss = { showLikedUsers = false },
            navController = navController,
            currentUserId = currentUserId ?: ""
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Posts", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            NavigationBar(
                navController = navController,
                profilePictureUrl = user?.profilePictureUrl
            )
        },
        content = { paddingValues ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(sortedPosts) { post ->
                    val isSaved = savedPosts.any { it.id == post.id } // Check if post is saved
                    PostContent(
                        post = post,
                        username = user?.username ?: "",
                        profilePictureUrl = user?.profilePictureUrl,
                        createdAt = post.createdAt,
                        isLiked = post.isLiked,
                        onLike = { postDetailViewModel.likePost(post.id) },
                        onUnlike = { postDetailViewModel.unlikePost(post.id) },
                        onSaveClick = {
                            postDetailViewModel.savePost(post.id)
                            homeViewModel.savePost(post.id) // Sync with HomeViewModel
                        },
                        onUnsaveClick = {
                            postDetailViewModel.removeSavedPost(post.id)
                            homeViewModel.removeSavedPost(post.id) // Sync with HomeViewModel
                        },
                        isSaved = isSaved,
                        showSaveIcon = showSaveIcon,
                        navController = navController,
                        currentUserId = currentUserId ?: "",
                        postId = post.id,
                        onLikesClick = {
                            postDetailViewModel.loadLikedUsers(post.id)
                            showLikedUsers = true
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun PostContent(
    post: Post,
    username: String,
    profilePictureUrl: String?,
    createdAt: Date,
    isLiked: Boolean,
    onLike: () -> Unit,
    onUnlike: () -> Unit,
    onSaveClick: () -> Unit,
    onUnsaveClick: () -> Unit,
    isSaved: Boolean,
    showSaveIcon: Boolean,
    navController: NavHostController,
    currentUserId: String,
    postId: String,
    onLikesClick: () -> Unit
) {
    var liked by remember { mutableStateOf(isLiked) }
    var likes by remember { mutableIntStateOf(post.likeCount) }
    var saved by remember { mutableStateOf(isSaved) }
    var showHeartAnimation by remember { mutableStateOf(false) }
    var showFullImage by remember { mutableStateOf(false) }
    val scale = remember { androidx.compose.animation.core.Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    var lastTapTimestamp by remember { mutableStateOf(0L) }

    val formattedDate = remember(createdAt) {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
        sdf.format(createdAt)
    }

    if (showFullImage) {
        FullScreenImage(imageUrl = post.imageUrl ?: "", onDismiss = { showFullImage = false })
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Profile information and timestamp
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
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { navController.navigate("profile/$currentUserId") },
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = username,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }

        // Post image with double-tap to like
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastTapTimestamp < 300) {
                        if (!liked) {
                            likes += 1
                            liked = true
                            onLike()
                        } else {
                            likes -= 1
                            liked = false
                            onUnlike()
                        }
                        showHeartAnimation = true
                        coroutineScope.launch {
                            scale.snapTo(0f)
                            scale.animateTo(1.5f, tween(300))
                            scale.animateTo(0f, tween(300))
                            delay(600)
                            showHeartAnimation = false
                        }
                    }
                    lastTapTimestamp = currentTime
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = "Post Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (showHeartAnimation) {
                Icon(
                    painter = painterResource(id = R.drawable.red_heart_icon),
                    contentDescription = "Liked",
                    tint = Color.Red,
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale.value)
                        .align(Alignment.Center)
                )
            }

            // Full screen icon on top-right
            Icon(
                painter = painterResource(id = R.drawable.fullscreen),
                contentDescription = "Fullscreen Icon",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(24.dp)
                    .clickable { showFullImage = true }
            )
        }

        // Box for icons (like, comment, save) and text (likes count, content)
        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like and comment icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Like button and count
                    IconButton(
                        onClick = {
                            if (!liked) {
                                liked = true
                                likes += 1
                                onLike()
                            } else {
                                liked = false
                                likes -= 1
                                onUnlike()
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = if (liked) R.drawable.red_heart_icon else R.drawable.heart_icon),
                            contentDescription = "Like",
                            tint = if (liked) Color.Red else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(text = " $likes", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)

                    Spacer(modifier = Modifier.width(16.dp)) // Like ve Comment ikonları arasındaki boşluk

                    // Comment button and count
                    IconButton(
                        onClick = {
                            navController.navigate("comments/$postId/$currentUserId")
                        },
                        modifier = Modifier.size(19.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.commentt),
                            contentDescription = "Comment",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(text = "  ${post.commentCount}", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                }

                // Save button
                if (showSaveIcon) {
                    IconButton(onClick = {
                        if (saved) {
                            onUnsaveClick()
                            saved = false
                        } else {
                            onSaveClick()
                            saved = true
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = if (saved) R.drawable.save else R.drawable.empty_save),
                            contentDescription = "Save Post",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Post content or "See comments" if empty
            val contentText = if (post.content.isNullOrEmpty()) "See comments" else post.content
            Text(
                text = contentText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .clickable {
                        if (post.content.isNullOrEmpty()) {
                            navController.navigate("comments/$postId/$currentUserId")
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
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
