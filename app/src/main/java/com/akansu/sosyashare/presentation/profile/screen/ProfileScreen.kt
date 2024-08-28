package com.akansu.sosyashare.presentation.profile.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.akansu.sosyashare.R
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.presentation.home.components.NavigationBar
import com.akansu.sosyashare.presentation.profile.ProfileViewModel
import com.akansu.sosyashare.presentation.userprofile.viewmodel.UserViewModel
import com.akansu.sosyashare.util.poppinsFontFamily

@Composable
fun ProfileScreen(
    navController: NavHostController,
    userId: String?,
    userViewModel: UserViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val userDetails by profileViewModel.userDetails.collectAsState()
    val isFollowing by profileViewModel.isFollowing.collectAsState()
    val isPrivateAccount by profileViewModel.isPrivateAccount.collectAsState()
    val profilePictureUrl by userViewModel.profilePictureUrl.collectAsState()
    val currentUserId by profileViewModel.currentUserId.collectAsState()
    val userPosts by profileViewModel.userPosts.collectAsState()

    LaunchedEffect(userId) {
        userId?.let { id ->
            profileViewModel.loadProfileData(currentUserId ?: "", id)
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                navController,
                userDetails?.username ?: "Unknown",
                onBlockUser = {
                    currentUserId?.let { currentId ->
                        userId?.let { id -> profileViewModel.blockUser(currentId, id) }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                selectedItem = 4,
                onItemSelected = {},
                navController = navController,
                profilePictureUrl = profilePictureUrl ?: userDetails?.profilePictureUrl
            )
        },
        modifier = Modifier.padding(WindowInsets.systemBars.asPaddingValues())
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = 72.dp)
        ) {
            item {
                ProfileInfo(
                    username = userDetails?.username ?: "Unknown",
                    profilePictureUrl = userDetails?.profilePictureUrl,
                    bio = userDetails?.bio ?: ""
                )
                UserStatistics(
                    postCount = userPosts.size,
                    followersCount = userDetails?.followers?.size ?: 0,
                    followingCount = userDetails?.following?.size ?: 0,
                    onFollowersClick = { /* followers dialog */ },
                    onFollowingClick = { /* following dialog */ }
                )
                currentUserId?.let {
                    ActionButtons(
                        profileViewModel = profileViewModel,
                        currentUserId = it,
                        userId = userId ?: "",
                        isFollowing = isFollowing
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (isPrivateAccount && !isFollowing) {
                    Text(
                        text = "Bu hesap gizlidir.",
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
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavHostController, username: String, onBlockUser: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(username, color = MaterialTheme.colorScheme.onBackground) },
        navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
        },
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = MaterialTheme.colorScheme.onBackground)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Engelle") },
                    onClick = {
                        expanded = false
                        onBlockUser()
                    }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun ProfileInfo(username: String, profilePictureUrl: String?, bio: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
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
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            username,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            fontFamily = poppinsFontFamily,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            bio,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = poppinsFontFamily,
            maxLines = 3
        )
    }
}

@Composable
fun UserStatistics(
    postCount: Int,
    followersCount: Int,
    followingCount: Int,
    onFollowersClick: () -> Unit,
    onFollowingClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatColumn(postCount.toString(), "Posts")
        StatColumn(followersCount.toString(), "Followers", onClick = onFollowersClick)
        StatColumn(followingCount.toString(), "Following", onClick = onFollowingClick)
    }
}

@Composable
fun StatColumn(count: String, label: String, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            count,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            fontFamily = poppinsFontFamily,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(label, fontFamily = poppinsFontFamily, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
    }
}

@Composable
fun ActionButtons(profileViewModel: ProfileViewModel, currentUserId: String, userId: String, isFollowing: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = {
                if (isFollowing) {
                    profileViewModel.unfollowUser(currentUserId, userId)
                } else {
                    profileViewModel.followUser(currentUserId, userId)
                }
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = if (isFollowing) Color.Gray else MaterialTheme.colorScheme.primary),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(if (isFollowing) "Unfollow" else "Follow", color = MaterialTheme.colorScheme.onPrimary, fontFamily = poppinsFontFamily)
        }
        Button(
            onClick = { /* Mesaj gönderme işlevi eklenebilir */ },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Message", color = MaterialTheme.colorScheme.onPrimary, fontFamily = poppinsFontFamily)
        }
    }
}

@Composable
fun PostGrid(posts: List<Post>, onPostClick: (Int) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(1.dp),
        modifier = Modifier.height(500.dp)
    ) {
        items(posts.reversed()) { post ->
            val index = posts.indexOf(post)

            Image(
                painter = rememberAsyncImagePainter(post.imageUrl),
                contentDescription = "Post",
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(1.dp)
                    .clickable { onPostClick(index) },
                contentScale = ContentScale.Crop
            )
        }
    }
}
