package com.akansu.sosyashare.presentation.home

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
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
            TopBar(currentUsername, navController)
            PostsSection(posts, users, savedPosts, homeViewModel, navController, { postId ->
                homeViewModel.loadLikedUsers(postId)
                showLikedUsers = true
            })
        }
        BottomNavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            navController = navController,
            profilePictureUrl = profilePictureUrl
        )
    }
}

@Composable
fun TopBar(currentUsername: String?, navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = currentUsername ?: "Menu",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = poppinsFontFamily,
            modifier = Modifier.weight(1f, true)
        )

        // Modern Swap style buttons
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50)) // Oval shape
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 6.dp, vertical = 4.dp) // Inside padding
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Notification icon button
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface, // Background color for unselected icon
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            // Handle click for notification
                        }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.notification),
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Messenger icon button (highlighted/active)
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.inverseSurface, // Background color for selected icon
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            // Handle click for messages
                            navController.navigate("messages")
                        }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.messenger),
                        contentDescription = "Messages",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(8.dp)
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
    var showFullComment by remember { mutableStateOf(false) }
    var showFullImage by remember { mutableStateOf(false) } // For showing full image
    val scale = remember { androidx.compose.animation.core.Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    var lastTapTimestamp by remember { mutableStateOf(0L) }

    // Full image modal
    if (showFullImage) {
        FullScreenImage(
            imageUrl = post.imageUrl ?: "",
            onDismiss = { showFullImage = false }
        )
    }

    if (showFullComment) {
        AlertDialog(
            onDismissRequest = { showFullComment = false },
            title = { Text("Full Comment") },
            text = { Text(text = post.content ?: "Yorum yapılmadı") },
            confirmButton = {
                Button(onClick = { showFullComment = false }) {
                    Text("Close")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(480.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            showFullImage = true // Resme tıklandığında full screen modal açılır
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

                    // Profil resmi ve kullanıcı adı sol üst köşede
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
                                navController.navigate("profile/${user?.id}") // Kullanıcı profiline yönlendirme
                            }
                        )
                    }

                    // Kullanıcı adı ve yorumlar en alt sol köşede
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.4f))
                            .padding(16.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = user?.username ?: "Unknown",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = post.content?.takeIf { it.length > 50 }
                                    ?.take(50)?.plus("...") ?: (post.content ?: "Yorum yapılmadı"),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                maxLines = 1,
                                modifier = Modifier.clickable {
                                    if (post.content?.length ?: 0 > 50) {
                                        showFullComment = true
                                    }
                                }
                            )
                        }
                    }

                    // Sağ alt köşede ikonlar (like, comment, save)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 60.dp), // Sağ ve alt tarafa padding uygulandı
                        verticalArrangement = Arrangement.spacedBy(12.dp), // İkonlar arasındaki boşluğu ayarlamak için
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Like button and count
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33FFFFFF))
                                    .clickable {
                                        if (!liked) {
                                            likes += 1
                                            liked = true
                                            homeViewModel.likePost(post.id)
                                        } else {
                                            likes -= 1
                                            liked = false
                                            homeViewModel.unlikePost(post.id)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = if (liked) R.drawable.red_heart_icon else R.drawable.heart_icon),
                                    contentDescription = "Like",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Likes count
                            Text(
                                text = "$likes", // Sadece sayı gösterimi
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }

                        // Comment button and count
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33FFFFFF))
                                    .clickable {
                                        navController.navigate("comments/${post.id}/${user?.id}")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.comment),
                                    contentDescription = "Comment",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Comments count
                            Text(
                                text = "0", // Burada yorum sayısı değiştirilebilir
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }

                        // Save button and count
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33FFFFFF))
                                    .clickable {
                                        if (saved) {
                                            homeViewModel.removeSavedPost(post.id)
                                            saved = false
                                        } else {
                                            homeViewModel.savePost(post.id)
                                            saved = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = if (saved) R.drawable.save else R.drawable.empty_save),
                                    contentDescription = "Save Post",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Save count
                            Text(
                                text = if (saved) "1" else "0", // Sadece sayı gösterimi
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
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


@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    profilePictureUrl: String?
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .offset(y = 10.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavigationItem(
                icon = R.drawable.home,
                contentDescription = "Home",
                onClick = { navController.navigate("home") }
            )

            // Search icon büyütüldü
            BottomNavigationItem(
                icon = R.drawable.search,
                contentDescription = "Search",
                onClick = { navController.navigate("search") },
                iconSize = 33.dp // Search icon boyut büyütüldü
            )

            BottomNavigationItem(
                icon = R.drawable.more,
                contentDescription = "More",
                onClick = { navController.navigate("share") }
            )

            BottomNavigationItem(
                icon = R.drawable.trend,
                contentDescription = "Trend",
                onClick = { navController.navigate("trend") }
            )

            Image(
                painter = rememberAsyncImagePainter(profilePictureUrl ?: R.drawable.profile),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable {
                        navController.navigate("userprofile")
                    },
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun BottomNavigationItem(
    icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    iconSize: Dp = 26.dp // Varsayılan icon boyutu
) {
    Icon(
        painter = painterResource(id = icon),
        contentDescription = contentDescription,
        tint = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .size(iconSize)
            .clickable { onClick() }
    )
}