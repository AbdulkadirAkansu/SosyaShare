package com.akansu.sosyashare.presentation.profile.screen

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.akansu.sosyashare.R
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.presentation.home.components.FollowersFollowingDialog
import com.akansu.sosyashare.presentation.home.components.NavigationBar
import com.akansu.sosyashare.presentation.profile.ProfileViewModel
import com.akansu.sosyashare.presentation.userprofile.viewmodel.UserViewModel
import com.akansu.sosyashare.util.poppinsFontFamily

@Composable
fun ProfileScreen(
    navController: NavHostController,
    userId: String?,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val userDetails by profileViewModel.userDetails.collectAsState()
    val backgroundImageUrl by profileViewModel.backgroundImageUrl.collectAsState()
    val userPosts by profileViewModel.userPosts.collectAsState()
    val profilePictureUrl by userViewModel.profilePictureUrl.collectAsState()
    val isFollowing by profileViewModel.isFollowing.collectAsState()
    val isPrivateAccount by profileViewModel.isPrivateAccount.collectAsState()
    val currentUserId by profileViewModel.currentUserId.collectAsState()
    var showFollowersDialog by remember { mutableStateOf(false) }
    var showFollowingDialog by remember { mutableStateOf(false) }
    var followers by remember { mutableStateOf<List<User>>(emptyList()) }
    var following by remember { mutableStateOf<List<User>>(emptyList()) }

    // Profil verilerini yükleme
    LaunchedEffect(userId) {
        userId?.let { id ->
            profileViewModel.loadProfileData(currentUserId ?: "", id)
        }
    }

    LaunchedEffect(userDetails) {
        userDetails?.followers?.let { followerIds ->
            followers = followerIds.mapNotNull { followerId ->
                profileViewModel.getUserById(followerId)
            }
        }

        userDetails?.following?.let { followingIds ->
            following = followingIds.mapNotNull { followingId ->
                profileViewModel.getUserById(followingId)
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                navController = navController,
                profilePictureUrl = profilePictureUrl ?: userDetails?.profilePictureUrl
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding() + 72.dp)
        ) {
            item {
                BackgroundWithProfile(
                    backgroundImageUrl = userDetails?.backgroundImageUrl,
                    profilePictureUrl = userDetails?.profilePictureUrl,
                    onBackClick = { navController.navigateUp() },
                    onSettingsClick = {
                        currentUserId?.let { currentId ->
                            userId?.let { id ->
                                profileViewModel.blockUser(currentId, id)
                                navController.navigate("home") {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Profil istatistikleri
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatColumn(
                        count = userDetails?.followers?.size?.toString() ?: "0",
                        label = "Followers",
                        onClick = { showFollowersDialog = true }
                    )
                    StatColumn(
                        count = userDetails?.following?.size?.toString() ?: "0",
                        label = "Following",
                        onClick = { showFollowingDialog = true }
                    )
                }

                ProfileInfo(
                    username = userDetails?.username ?: "Unknown",
                    bio = userDetails?.bio ?: ""
                )

                Spacer(modifier = Modifier.height(20.dp))

                ActionButtons(
                    profileViewModel = profileViewModel,
                    currentUserId = currentUserId ?: "",
                    userId = userId ?: "",
                    isFollowing = isFollowing
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Posts",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = userPosts.size.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isPrivateAccount && !isFollowing) {
                    Text(
                        text = "This account is private.",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                } else {
                    PostGrid(posts = userPosts) { postIndex ->
                        navController.navigate("post_detail/${userId}/${postIndex}/true")
                    }
                }
            }
        }

        // Dialogları gösterme
        if (showFollowersDialog) {
            FollowersFollowingDialog(
                users = followers,
                title = "Followers",
                onDismiss = { showFollowersDialog = false },
                navController = navController,
                currentUserId = currentUserId ?: ""
            )
        }

        if (showFollowingDialog) {
            FollowersFollowingDialog(
                users = following,
                title = "Following",
                onDismiss = { showFollowingDialog = false },
                navController = navController,
                currentUserId = currentUserId ?: ""
            )
        }
    }
}

@Composable
fun BackgroundWithProfile(
    backgroundImageUrl: String?,
    profilePictureUrl: String?,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val offsetValue = 8.dp // Dropdown menünün yukarı veya aşağı konumunu ayarlamak için

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Arka plan resmi
        Card(
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(backgroundImageUrl ?: R.drawable.pic3),
                contentDescription = "Background Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Profil resmi
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 60.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(profilePictureUrl ?: R.drawable.profile),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        // Geri ve ayarlar ikonları
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .offset(y = 16.dp) // Bu satır ikonları aşağı çeker
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = MaterialTheme.colorScheme.onBackground)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    offset = DpOffset(x = 0.dp, y = offsetValue) // Dropdown menünün konumunu ayarlıyoruz
                ) {
                    DropdownMenuItem(
                        text = { Text("Block User") },
                        onClick = {
                            expanded = false
                            onSettingsClick()
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun ActionButtons(profileViewModel: ProfileViewModel, currentUserId: String, userId: String, isFollowing: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = {
                if (isFollowing) {
                    profileViewModel.unfollowUser(currentUserId, userId)
                } else {
                    profileViewModel.followUser(currentUserId, userId)
                }
            },
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFollowing) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary,
                contentColor = if (isFollowing) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                if (isFollowing) "Unfollow" else "Follow",
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
        Button(
            onClick = { /* TODO: Message functionality */ },
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        ) {
            Text(
                "Message",
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun StatColumn(count: String, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            count,
            fontWeight = FontWeight.Bold,
            fontFamily = poppinsFontFamily,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            label,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun PostGrid(posts: List<Post>, onPostClick: (Int) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (posts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No posts yet")
                }
            }
        } else {
            items(posts.size) { index ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPostClick(index) }
                ) {
                    AsyncImage(
                        model = posts[posts.size - 1 - index].imageUrl,
                        contentDescription = "Post",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileInfo(username: String, bio: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
    ) {
        Text(
            text = username,
            fontWeight = FontWeight.Bold,
            fontFamily = poppinsFontFamily,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = bio,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.Normal,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}
