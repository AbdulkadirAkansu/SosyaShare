package com.akansu.sosyashare.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.akansu.sosyashare.R
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.presentation.home.components.LikedUsersDialog
import com.akansu.sosyashare.presentation.home.components.NavigationBar
import com.akansu.sosyashare.presentation.home.viewmodel.HomeViewModel
import com.akansu.sosyashare.presentation.userprofile.viewmodel.UserViewModel
import com.akansu.sosyashare.util.poppinsFontFamily
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

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = currentUsername ?: "SosyaShare",
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.offset(x = (-8).dp)
                        )
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = { /* Bildirimler */ },
                                modifier = Modifier
                                    .size(24.dp)
                                    .offset(y = (-4).dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.notification),
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            IconButton(
                                onClick = { navController.navigate("messages") },
                                modifier = Modifier
                                    .size(36.dp)
                                    .offset(y = (-4).dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.chat),
                                    contentDescription = "Messages",
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                selectedItem = selectedItem,
                onItemSelected = { selectedItem = it },
                navController = navController,
                profilePictureUrl = profilePictureUrl,
                modifier = Modifier.height(65.dp)
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
            Spacer(modifier = Modifier.height(8.dp))
            PostsList(
                posts = posts,
                users = users,
                savedPosts = savedPosts,
                homeViewModel = homeViewModel,
                navController = navController,
                onLikesClick = { postId ->
                    homeViewModel.loadLikedUsers(postId)
                    showLikedUsers = true
                }
            )
        }
    }
}

@Composable
fun PostsList(
    posts: List<Post>,
    users: Map<String, User>,
    savedPosts: List<Post>,
    homeViewModel: HomeViewModel,
    navController: NavHostController,
    onLikesClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        items(posts, key = { it.id }) { post ->
            val user = users[post.userId]
            PostItem(
                postUrl = post.imageUrl ?: "",
                postId = post.id,
                postUserId = post.userId,
                username = user?.username ?: "Unknown",
                profilePictureUrl = user?.profilePictureUrl,
                comment = post.content,
                isLiked = post.isLiked,
                likeCount = post.likeCount,
                isSaved = savedPosts.any { it.id == post.id },
                onLikeClick = { homeViewModel.likePost(post.id) },
                onUnlikeClick = { homeViewModel.unlikePost(post.id) },
                onSaveClick = { homeViewModel.savePost(post.id) },
                onUnsaveClick = { homeViewModel.removeSavedPost(post.id) },
                navController = navController,
                onLikesClick = onLikesClick
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
fun PostItem(
    postUrl: String,
    postId: String,
    postUserId: String,
    username: String,
    profilePictureUrl: String?,
    comment: String?,
    isLiked: Boolean,
    likeCount: Int,
    isSaved: Boolean,
    onLikeClick: () -> Unit,
    onUnlikeClick: () -> Unit,
    onSaveClick: () -> Unit,
    onUnsaveClick: () -> Unit,
    navController: NavHostController,
    onLikesClick: (String) -> Unit
) {
    var liked by remember { mutableStateOf(isLiked) }
    var likes by remember { mutableIntStateOf(likeCount) }
    var saved by remember { mutableStateOf(isSaved) }
    var showHeartAnimation by remember { mutableStateOf(false) }  // Animasyon durumu
    val scale = remember { androidx.compose.animation.core.Animatable(0f) }
    val coroutineScope = rememberCoroutineScope() // Coroutine scope oluşturuyoruz

    // Çift tıklama algılama
    var lastTapTimestamp by remember { mutableStateOf(0L) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navController.navigate("profile/$postUserId")
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(profilePictureUrl ?: R.drawable.profile),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .clickable {
                        navController.navigate("profile/$postUserId")
                    },
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.clickable {
                    navController.navigate("profile/$postUserId")
                }
            ) {
                Text(
                    text = username,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "5 min ago",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .clip(MaterialTheme.shapes.medium)
                .clickable {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastTapTimestamp < 300) {
                        // Çift tıklama algılandı
                        if (!liked) {
                            likes += 1
                            liked = true
                            onLikeClick()
                        } else {
                            likes -= 1
                            liked = false
                            onUnlikeClick()
                        }

                        // Kalp animasyonu tetikleniyor
                        showHeartAnimation = true

                        coroutineScope.launch {
                            scale.snapTo(0.5f)  // scale değeri 0.5'e anında geçiyor
                            scale.animateTo(
                                targetValue = 1.5f,
                                animationSpec = androidx.compose.animation.core.tween(
                                    durationMillis = 400
                                )
                            )

                            // Animasyonun bir süre sonra kaybolması
                            kotlinx.coroutines.delay(600)  // 600ms bekleme
                            showHeartAnimation = false  // Kalbi gizliyoruz
                        }
                    }
                    lastTapTimestamp = currentTime
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(postUrl),
                contentDescription = "Post Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Kalp animasyonu
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

        Spacer(modifier = Modifier.height(8.dp))

        if (!comment.isNullOrEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = comment,
                    fontFamily = poppinsFontFamily,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // İkonlar satırı
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (liked) {
                        likes -= 1
                        onUnlikeClick()
                    } else {
                        likes += 1
                        onLikeClick()
                    }
                    liked = !liked
                }) {
                    Icon(
                        painter = if (liked) painterResource(id = R.drawable.red_heart_icon) else painterResource(id = R.drawable.heart_icon),
                        contentDescription = "Like",
                        tint = if (liked) Color.Red else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = likes.toString(),
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.clickable { onLikesClick(postId) }
                )
                Spacer(modifier = Modifier.width(24.dp))
                IconButton(onClick = {
                    navController.navigate("comments/$postId/$postUserId")
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.comment),
                        contentDescription = "Comment",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }

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
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
