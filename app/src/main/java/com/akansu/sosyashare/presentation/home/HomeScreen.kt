package com.akansu.sosyashare.presentation.home

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.akansu.sosyashare.R
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.presentation.home.components.LikedUsersDialog
import com.akansu.sosyashare.presentation.home.components.NavigationBar
import com.akansu.sosyashare.presentation.home.viewmodel.HomeViewModel
import com.akansu.sosyashare.presentation.userprofile.viewmodel.UserViewModel
import com.akansu.sosyashare.util.poppinsFontFamily
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    homeViewModel: HomeViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val posts by homeViewModel.posts.collectAsState()
    val users by homeViewModel.users.collectAsState()
    val profilePictureUrl by userViewModel.profilePictureUrl.collectAsState()
    val currentUsername by userViewModel.username.collectAsState()
    val currentUserId by userViewModel.userId.collectAsState()
    val savedPosts by homeViewModel.savedPosts.collectAsState()
    var selectedItem by remember { mutableIntStateOf(0) }
    var showLikedUsers by remember { mutableStateOf(false) }

    if (showLikedUsers) {
        LikedUsersDialog(
            users = homeViewModel.likedUsers.collectAsState().value,
            onDismiss = { showLikedUsers = false },
            navController = navController,
            currentUserId = currentUserId ?: ""
        )
    }

    LaunchedEffect(Unit) {
        homeViewModel.loadFollowedUsersPosts()
        homeViewModel.loadSavedPosts()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))
            TopBar(currentUsername, currentUserId, navController) // `currentUserId`'yi buradan geçiyoruz
            PostsSection(posts, users, savedPosts, homeViewModel, navController, { postId ->
                homeViewModel.loadLikedUsers(postId)
                showLikedUsers = true
            })
        }
        NavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            navController = navController,
            profilePictureUrl = profilePictureUrl
        )
    }
}

@Composable
fun TopBar(currentUsername: String?, currentUserId: String?, navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = currentUsername ?: "Menu",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Notification icon
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            Log.d("Navigation", "Navigating to notifications screen with userId: $currentUserId")
                            currentUserId?.let {
                                navController.navigate("notifications/$it")
                            } ?: Log.e("Navigation", "User ID is null, cannot navigate to notifications.")
                        }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.notification),
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(4.dp)
                    )
                }


                // Messenger icon
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.inverseSurface,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            navController.navigate("messages")
                        }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.messenger),
                        contentDescription = "Messages",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun PostsSection(
    posts: List<Post>,
    users: Map<String, User>,
    savedPosts: List<Post>,
    homeViewModel: HomeViewModel,
    navController: NavHostController,
    onLikesClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(posts) { post ->
            val user = users[post.userId]
            PostItem(post, user, savedPosts.any { it.id == post.id }, homeViewModel, navController, onLikesClick)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostItem(
    post: Post,
    user: User?,
    isSaved: Boolean,
    homeViewModel: HomeViewModel,
    navController: NavHostController,
    onLikesClick: (String) -> Unit
) {
    var liked by remember { mutableStateOf(post.isLiked) }
    var likes by remember { mutableIntStateOf(post.likeCount) }
    var saved by remember { mutableStateOf(isSaved) }
    var showHeartAnimation by remember { mutableStateOf(false) }
    var showFullContent by remember { mutableStateOf(false) } // For full content dialog
    var showFullImage by remember { mutableStateOf(false) }   // For full-screen image
    val scale = remember { androidx.compose.animation.core.Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val lastTapTimestamp = remember { mutableStateOf(0L) }   // For double-tap detection
    val doubleTapTimeout = 300L  // Double-tap timeout in ms
    val context = LocalContext.current

    // Full screen image functionality
    if (showFullImage) {
        FullScreenImage(
            imageUrl = post.imageUrl ?: "",
            onDismiss = { showFullImage = false }
        )
    }

    // Full content dialog
    if (showFullContent) {
        AlertDialog(
            onDismissRequest = { showFullContent = false },
            title = { Text("Full Content") },
            text = {
                Text(
                    post.content ?: "No content available",
                    color = MaterialTheme.colorScheme.onBackground // Use theme color
                )
            },
            confirmButton = {
                Button(onClick = { showFullContent = false }) {
                    Text("Close")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Post image
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(
                topStart = 30.dp,
                topEnd = 30.dp,
                bottomEnd = 0.dp,
                bottomStart = 0.dp
            ),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(480.dp)
                            .clickable {
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastTapTimestamp.value < doubleTapTimeout) {
                                    if (!liked) {
                                        likes += 1
                                        liked = true
                                        showHeartAnimation = true
                                        coroutineScope.launch {
                                            scale.snapTo(0f)
                                            scale.animateTo(1f)
                                            scale.animateTo(0f)
                                        }
                                        homeViewModel.likePost(post.id, post.userId,context)

                                    } else {
                                        likes -= 1
                                        liked = false
                                        homeViewModel.unlikePost(post.id)
                                    }
                                }
                                lastTapTimestamp.value = currentTime
                            }
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(post.imageUrl),
                            contentDescription = "Post Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        if (showHeartAnimation) {
                            Icon(
                                painter = painterResource(id = R.drawable.red_heart_icon),
                                contentDescription = "Liked",
                                tint = Color.Red,
                                modifier = Modifier
                                    .size(100.dp)
                                    .scale(scale.value)
                                    .align(Alignment.Center)
                            )
                        }

                        // Fullscreen icon at the top-right corner of the image
                        Icon(
                            painter = painterResource(id = R.drawable.fullscreen),
                            contentDescription = "Fullscreen Icon",
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .size(20.dp)
                                .clickable {
                                    showFullImage = true
                                }
                        )

                        // Profile picture and username at the top-left corner
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(user?.profilePictureUrl ?: R.drawable.profile),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = user?.username ?: "Unknown",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.clickable {
                                    navController.navigate("profile/${user?.id}")
                                }
                            )
                        }
                    }
                }
            }
        }

        // Post content and icons in the same card
        Card(
            shape = RoundedCornerShape(
                topStart = 0.dp,
                topEnd = 0.dp,
                bottomEnd = 30.dp,
                bottomStart = 30.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomEnd = 30.dp,
                        bottomStart = 30.dp
                    ),
                    clip = false
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Icons and labels above the content
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Like button and count
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (!liked) {
                                    likes += 1
                                    liked = true
                                    homeViewModel.likePost(post.id, post.userId,context)
                                } else {
                                    likes -= 1
                                    liked = false
                                    homeViewModel.unlikePost(post.id)
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = if (liked) R.drawable.red_heart_icon else R.drawable.heart_icon),
                                contentDescription = "Like",
                                tint = MaterialTheme.colorScheme.onBackground // Correct color based on theme
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "$likes likes", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                    }

                    // Comment button and count
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                navController.navigate("comments/${post.id}/${user?.id}")
                            },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.commentt),
                                contentDescription = "Comment",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = " ${post.commentCount} comments", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                    }


                    // Save button and status
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (saved) {
                                    homeViewModel.removeSavedPost(post.id)
                                    saved = false
                                } else {
                                    homeViewModel.savePost(post.id)
                                    saved = true
                                }
                            },
                            modifier = Modifier.size(23.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = if (saved) R.drawable.save else R.drawable.empty_save),
                                contentDescription = "Save Post",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (saved) "Saved" else "Save", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Content text with the username at the beginning
                val contentText = if (post.content.isNullOrEmpty()) "See comments" else post.content
                Text(
                    text = "${user?.username ?: "Unknown"}: $contentText",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (post.content.isNullOrEmpty()) {
                                navController.navigate("comments/${post.id}/${user?.id}")
                            } else if (post.content?.length ?: 0 > 50) {
                                showFullContent = true
                            }
                        },
                    maxLines = 1
                )
            }
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
