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
                items(posts) { post ->
                    val isSaved = savedPosts.any { it.id == post.id } // Check if post is saved
                    PostContent(
                        post = post,
                        username = user!!.username,
                        profilePictureUrl = user!!.profilePictureUrl,
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
    val scale = remember { androidx.compose.animation.core.Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    // Çift tıklama algılama
    var lastTapTimestamp by remember { mutableStateOf(0L) }

    val formattedDate = remember(createdAt) {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
        sdf.format(createdAt)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
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
                        // Çift tıklama algılandı
                        if (!liked) {
                            likes += 1
                            liked = true
                            onLike()
                        } else {
                            likes -= 1
                            liked = false
                            onUnlike()
                        }

                        // Kalp animasyonu tetikleniyor
                        showHeartAnimation = true

                        coroutineScope.launch {
                            scale.snapTo(0.5f)
                            scale.animateTo(
                                targetValue = 1.5f,
                                animationSpec = androidx.compose.animation.core.tween(
                                    durationMillis = 400
                                )
                            )

                            // Animasyonun bir süre sonra kaybolması
                            kotlinx.coroutines.delay(600)
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

            // Çift tıklama sonucu büyük kalp animasyonu
            if (showHeartAnimation) {
                Icon(
                    painter = painterResource(id = R.drawable.red_heart_icon),
                    contentDescription = "Liked",
                    tint = Color.Red,
                    modifier = Modifier
                        .size(100.dp)
                        .scale(scale.value)
                        .then(Modifier.align(Alignment.Center))
                )
            }
        }

        // Post content (description)
        Text(
            text = post.content,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )

        // Likes and comment section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Like butonu
            IconButton(onClick = {
                if (!liked) {
                    liked = true
                    likes += 1
                    onLike()
                } else {
                    liked = false
                    likes -= 1
                    onUnlike()
                }
            }) {
                Icon(
                    imageVector = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Like",
                    modifier = Modifier
                        .size(28.dp)
                        .scale(if (liked) 1.5f else 1f), // İkisi de var, beğeni simgesi ve animasyon
                    tint = if (liked) Color.Red else Color.Gray
                )
            }

            IconButton(onClick = {
                navController.navigate("comments/$postId/$currentUserId")
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.comment),
                    contentDescription = "Comment",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save butonu
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

        // Likes count displayed below the buttons
        Text(
            text = "$likes likes",
            fontWeight = FontWeight.Bold,
            fontFamily = poppinsFontFamily,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .clickable { onLikesClick() }
                .padding(start = 8.dp, bottom = 4.dp)
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
