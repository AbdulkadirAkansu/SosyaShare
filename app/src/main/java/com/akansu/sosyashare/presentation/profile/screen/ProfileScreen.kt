package com.akansu.sosyashare.presentation.profile.screen

import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.material.icons.filled.*
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
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.presentation.home.components.FollowersFollowingDialog
import com.akansu.sosyashare.presentation.home.components.NavigationBar
import com.akansu.sosyashare.presentation.profile.ProfileViewModel
import com.akansu.sosyashare.presentation.userprofile.viewmodel.UserViewModel
import com.akansu.sosyashare.util.poppinsFontFamily

@SuppressLint("StateFlowValueCalledInComposition")
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
    val currentUserId = profileViewModel.currentUserId.value

    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId, currentUserId) {
        Log.d("ProfileScreen", "LaunchedEffect triggered with userId: $userId, currentUserId: $currentUserId")
        if (userId != null && currentUserId != null) {
            Log.d("ProfileScreen", "Loading profile data...")
            isLoading = true
            profileViewModel.loadProfileData(currentUserId, userId)
            isLoading = false
            Log.d("ProfileScreen", "Profile data loaded")
        } else {
            Log.d("ProfileScreen", "UserId or CurrentUserId is null")
        }
    }

    if (isLoading) {
        Log.d("ProfileScreen", "Loading Indicator displayed")
        LoadingIndicator()
    } else {
        userDetails?.let { user ->
            Log.d("ProfileScreen", "User details available, checking view permissions...")
            val canViewProfile = canViewProfile(isPrivateAccount, isFollowing, currentUserId, userId)
            Log.d("ProfileScreen", "Can view profile: $canViewProfile")
            ProfileContent(
                userDetails = user,
                canViewProfile = canViewProfile,
                isFollowing = isFollowing,
                currentUserId = currentUserId,
                userId = userId,
                profileViewModel = profileViewModel,
                navController = navController,
                profilePictureUrl = profilePictureUrl
            )
        } ?: run {
            Log.d("ProfileScreen", "Error: User details are null")
            ErrorText()
        }
    }
}

fun canViewProfile(
    isPrivateAccount: Boolean,
    isFollowing: Boolean,
    currentUserId: String?,
    userId: String?
): Boolean {
    return !isPrivateAccount || isFollowing || userId == currentUserId
}



@Composable
fun LoadingIndicator() {
    CircularProgressIndicator(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center))
}

@Composable
fun ErrorText() {
    Text(
        text = "Error loading user details",
        modifier = Modifier.padding(16.dp),
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
fun ProfileContent(
    userDetails: User,
    canViewProfile: Boolean,
    isFollowing: Boolean,
    currentUserId: String?,
    userId: String?,
    profileViewModel: ProfileViewModel,
    navController: NavHostController,
    profilePictureUrl: String?
) {
    Scaffold(
        topBar = {
            TopBar(navController, userDetails.username)
        },
        bottomBar = {
            NavigationBar(
                selectedItem = 4,
                onItemSelected = { /* Handle item selection */ },
                navController = navController,
                profilePictureUrl = profilePictureUrl ?: userDetails.profilePictureUrl,
                modifier = Modifier.height(65.dp)
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item {
                    ProfileInfo(
                        username = userDetails.username,
                        profilePictureUrl = userDetails.profilePictureUrl,
                        bio = userDetails.bio
                    )
                    if (canViewProfile) {
                        UserStatistics(
                            postCount = profileViewModel.userPosts.collectAsState().value.size,
                            followersCount = userDetails.followers.size,
                            followingCount = userDetails.following.size,
                            onFollowersClick = { /* Handle followers click */ },
                            onFollowingClick = { /* Handle following click */ }
                        )
                        PostGrid(
                            posts = profileViewModel.userPosts.collectAsState().value,
                            onPostClick = { postIndex ->
                                navController.navigate("post_detail/${userId}/${postIndex}/true")
                            }
                        )
                    } else {
                        PrivateProfileMessage()
                    }
                    currentUserId?.let {
                        ActionButtons(
                            profileViewModel = profileViewModel,
                            currentUserId = it,
                            userId = userId ?: "",
                            isFollowing = isFollowing
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    )
}

@Composable
fun PrivateProfileMessage() {
    Text(
        text = "This account is private. Follow to see their posts.",
        modifier = Modifier.padding(16.dp),
        style = MaterialTheme.typography.bodySmall
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavHostController, username: String) {
    TopAppBar(
        title = { Text(username, color = MaterialTheme.colorScheme.onBackground) },
        navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
        },
        actions = {
            IconButton(onClick = { navController.navigate("settings") }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = MaterialTheme.colorScheme.onBackground)
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
        StatColumn(postCount.toString(), "Posts", onClick = {})
        StatColumn(followersCount.toString(), "Followers", onClick = onFollowersClick)
        StatColumn(followingCount.toString(), "Following", onClick = onFollowingClick)
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
