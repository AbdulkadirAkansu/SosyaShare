package com.akansu.sosyashare.presentation.postdetail.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val user by postDetailViewModel.user.collectAsState()
    val posts by postDetailViewModel.posts.collectAsState()
    val savedPosts by homeViewModel.savedPosts.collectAsState()
    val listState = rememberLazyListState()
    val currentUserId by postDetailViewModel.currentUserId.collectAsState()
    var showLikedUsers by remember { mutableStateOf(false) }
    val sortedPosts = posts.sortedByDescending { it.createdAt }
    val context = LocalContext.current

    LaunchedEffect(userId) {
        postDetailViewModel.loadUserDetails(userId)
    }

    LaunchedEffect(initialPostIndex) {
        if (initialPostIndex >= 0 && initialPostIndex < sortedPosts.size) {
            listState.scrollToItem(initialPostIndex)
        }
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
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Posts",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                navController = navController,
                profilePictureUrl = user?.profilePictureUrl
            )
        },
        content = { paddingValues ->
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    items(sortedPosts) { post ->
                        val isSaved = savedPosts.any { it.id == post.id }
                        PostContent(
                            post = post,
                            username = user?.username ?: "",
                            profilePictureUrl = user?.profilePictureUrl,
                            createdAt = post.createdAt,
                            isLiked = post.isLiked,
                            onLike = { postDetailViewModel.likePost(post.id, post.userId, context) },
                            onUnlike = { postDetailViewModel.unlikePost(post.id) },
                            onSaveClick = {
                                postDetailViewModel.savePost(post.id)
                                homeViewModel.savePost(post.id)
                            },
                            onUnsaveClick = {
                                postDetailViewModel.removeSavedPost(post.id)
                                homeViewModel.removeSavedPost(post.id)
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
                    // Enhanced bottom space for better UX with navigation
                    item { Spacer(modifier = Modifier.height(70.dp)) }
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
    
    // Enhanced responsive sizing for 2025 design
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val dynamicImageHeight = (screenWidth * 1.15f).coerceAtMost(520.dp)
    
    val formattedDate = remember(createdAt) {
        val sdf = SimpleDateFormat("dd MMMM", Locale.ENGLISH) // Simplified date format
        sdf.format(createdAt)
    }

    if (showFullImage) {
        ImmersiveFullScreenImage(imageUrl = post.imageUrl ?: "", onDismiss = { showFullImage = false })
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp // Flat design trend for 2025
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // User header - Ultra modern with minimalist approach
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile picture with minimalist border 
                Surface(
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .shadow(2.dp, CircleShape),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AsyncImage(
                        model = profilePictureUrl,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .clickable { navController.navigate("profile/$currentUserId") },
                        contentScale = ContentScale.Crop
                    )
                }
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = username,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
                
                // Header save button has been removed as requested
            }

            // Post image with enhanced experience
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dynamicImageHeight)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastTapTimestamp < 300) {
                            // Double tap logic with enhanced feel
                            if (!liked) {
                                likes += 1
                                liked = true
                                onLike()
                            }
                            showHeartAnimation = true
                            coroutineScope.launch {
                                scale.snapTo(0f)
                                scale.animateTo(1.5f, 
                                    spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                                delay(600)
                                scale.animateTo(0f, tween(300))
                                showHeartAnimation = false
                            }
                        }
                        lastTapTimestamp = currentTime
                    },
                contentAlignment = Alignment.Center
            ) {
                // Post image with subtle rounding
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = "Post",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Fullscreen button - frosted glass effect
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                        .blur(0.5.dp)
                        .clickable { showFullImage = true }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.fullscreen),
                        contentDescription = "Fullscreen",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                // Save button at bottom right corner of image
                if (showSaveIcon) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (saved) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.3f))
                            .clickable {
                                if (saved) {
                                    onUnsaveClick()
                                    saved = false
                                } else {
                                    onSaveClick()
                                    saved = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = if (saved) R.drawable.save else R.drawable.empty_save),
                            contentDescription = "Save",
                            tint = if (saved) MaterialTheme.colorScheme.primary else Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                
                // Heart animation with improved visual effect
                if (showHeartAnimation) {
                    Icon(
                        painter = painterResource(id = R.drawable.red_heart_icon),
                        contentDescription = "Liked",
                        tint = Color.Red,
                        modifier = Modifier
                            .size(120.dp)
                            .scale(scale.value)
                    )
                }
            }

            // Engagement section - 2025 design inspiration with pill-shaped interaction elements
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 14.dp)
            ) {
                // Interaction row with modern spacing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left group with pill-shaped containers
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Like pill
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .clickable { 
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
                            color = if (liked) 
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            else 
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = if (liked) R.drawable.red_heart_icon else R.drawable.heart_icon),
                                    contentDescription = "Like",
                                    tint = if (liked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$likes",
                                    color = if (liked) 
                                        MaterialTheme.colorScheme.onPrimaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.clickable { onLikesClick() }
                                )
                            }
                        }

                        // Comment pill
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .clickable { navController.navigate("comments/$postId/$currentUserId") },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.commentt),
                                    contentDescription = "Comment",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${post.commentCount}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Modern save button has been moved to the image bottom right corner
                }

                // Post content with improved spacing and typography
                if (!post.content.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = post.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                        lineHeight = 22.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Comments section with enhanced design
                if (post.commentCount > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { navController.navigate("comments/$postId/$currentUserId") },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.07f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.commentt),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(end = 4.dp)
                            )
                            Text(
                                text = "View all ${post.commentCount} comments",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else if (post.content.isNullOrEmpty()) {
                    // Add comment prompt with modern pill shape
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .clickable { navController.navigate("comments/$postId/$currentUserId") },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.commentt),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add a comment...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImmersiveFullScreenImage(imageUrl: String, onDismiss: () -> Unit) {
    val systemUiController = rememberSystemUiController()
    val isLight = !isSystemInDarkTheme()
    var isLoading by remember { mutableStateOf(true) }

    // Immersive mode for fullscreen image
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Black,
            darkIcons = false
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            // Enhanced loading state
            if (isLoading) {
                // Blurred preview
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(10.dp)
                )
                
                // Modern loader
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center),
                    strokeWidth = 2.dp
                )
            }
            
            // Main high-res image
            AsyncImage(
                model = imageUrl,
                contentDescription = "Full Screen Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                onSuccess = { isLoading = false }
            )
            
            // Subtle indicator at bottom with gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f)
                            )
                        )
                    )
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tap to close",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }

    // Reset system bars when dialog is dismissed
    DisposableEffect(Unit) {
        onDispose {
            systemUiController.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = isLight
            )
        }
    }
}
