package com.akansu.sosyashare.presentation.userprofile

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.akansu.sosyashare.R
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.presentation.home.components.FollowersFollowingDialog
import com.akansu.sosyashare.presentation.home.components.NavigationBar
import com.akansu.sosyashare.presentation.login.viewmodel.AuthViewModel
import com.akansu.sosyashare.presentation.share.viewmodel.ShareViewModel
import com.akansu.sosyashare.presentation.userprofile.viewmodel.UserProfileViewModel
import com.akansu.sosyashare.util.FileUtils
import com.akansu.sosyashare.util.PermissionHandler
import com.akansu.sosyashare.util.poppinsFontFamily

@Composable
fun UserProfileScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: UserProfileViewModel = hiltViewModel(),
    shareViewModel: ShareViewModel = hiltViewModel()
) {
    val currentUser = authViewModel.getCurrentUser()
    var userDetails by remember { mutableStateOf<User?>(null) }
    var showFollowersDialog by remember { mutableStateOf(false) }
    var showFollowingDialog by remember { mutableStateOf(false) }

    val backgroundImageUrl by profileViewModel.backgroundImageUrl.collectAsState()

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            profileViewModel.getUserDetails(userId) {
                userDetails = it
            }
        }
    }

    var followers by remember { mutableStateOf<List<User>>(emptyList()) }
    var following by remember { mutableStateOf<List<User>>(emptyList()) }

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

    var profilePictureUrl by remember { mutableStateOf(userDetails?.profilePictureUrl) }
    var bio by remember { mutableStateOf(userDetails?.bio ?: "") }
    var followersCount by remember { mutableStateOf(userDetails?.followers?.size ?: 0) }
    var followingCount by remember { mutableStateOf(userDetails?.following?.size ?: 0) }



    LaunchedEffect(userDetails) {
        profilePictureUrl = userDetails?.profilePictureUrl
        bio = userDetails?.bio ?: ""
        followersCount = userDetails?.followers?.size ?: 0
        followingCount = userDetails?.following?.size ?: 0
    }

    val posts by shareViewModel.userPosts.collectAsState()

    val context = LocalContext.current as Activity

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val file = FileUtils.createFileFromUri(context, it)
            file?.let {
                profileViewModel.uploadProfilePicture(file, onSuccess = { newUrl ->
                    profilePictureUrl = newUrl
                    userDetails = userDetails?.copy(profilePictureUrl = newUrl)
                    profileViewModel.updateUserProfilePictureUrl(newUrl)
                }, onFailure = { e ->
                    Toast.makeText(context, "Failed to upload profile picture: ${e.message}", Toast.LENGTH_SHORT).show()
                })
            } ?: run {
                Toast.makeText(context, "Failed to convert URI to File", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val backgroundLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val file = FileUtils.createFileFromUri(context, it)
            file?.let {
                profileViewModel.uploadBackgroundImage(file, onSuccess = { newUrl ->
                    profileViewModel.updateBackgroundImageUrl(newUrl)
                }, onFailure = { e ->
                    Toast.makeText(context, "Failed to upload background image: ${e.message}", Toast.LENGTH_SHORT).show()
                })
            } ?: run {
                Toast.makeText(context, "Failed to convert URI to File", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showFollowersDialog) {
        FollowersFollowingDialog(
            users = followers,
            title = "Followers",
            onDismiss = { showFollowersDialog = false },
            navController = navController,
            currentUserId = currentUser?.uid ?: ""
        )
    }

    if (showFollowingDialog) {
        FollowersFollowingDialog(
            users = following,
            title = "Following",
            onDismiss = { showFollowingDialog = false },
            navController = navController,
            currentUserId = currentUser?.uid ?: ""
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                navController = navController,
                profilePictureUrl = profilePictureUrl
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = 72.dp)
        ) {
            item {
                BackgroundWithProfile(
                    backgroundImageUrl = backgroundImageUrl,
                    profilePictureUrl = profilePictureUrl,
                    onBackgroundClick = {
                        backgroundLauncher.launch("image/*")
                    },
                    onProfilePictureClick = {
                        launcher.launch("image/*")
                    },
                    onBackClick = { navController.navigateUp() },
                    onSettingsClick = { navController.navigate("settings") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatColumn(followersCount.toString(), "Followers") {
                        showFollowersDialog = true
                    }
                    StatColumn(followingCount.toString(), "Following") {
                        showFollowingDialog = true
                    }
                }

                ProfileInfo(
                    username = userDetails?.username ?: "Unknown",
                    bio = bio
                )

                Spacer(modifier = Modifier.height(20.dp)) // ProfileInfo'nun altına spacer ekliyoruz.

                ActionButtons(navController = navController)

                Spacer(modifier = Modifier.height(16.dp))

                // "Posts" başlığı ve en sağında postCount
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
                        text = posts.size.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                PostGrid(posts = posts.mapNotNull { it.imageUrl }, userId = currentUser?.uid ?: "", navController = navController)
            }
        }
    }
}

@Composable
fun PostGrid(posts: List<String>, userId: String, navController: NavHostController) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.height(500.dp)
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
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp)) // Resimlere çok az radius ekleniyor.
                        .clickable {
                            navController.navigate("post_detail/${userId}/${index}/false")
                        }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(posts[posts.size - 1 - index])
                            .crossfade(true)
                            .build(),
                        contentDescription = "Post",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButtons(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = { navController.navigate("editprofile") },
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Text("Edit Profile")
        }
        Button(
            onClick = { /* TODO: Add share functionality */ },
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        ) {
            Text("Share Profile")
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
            fontSize = 22.sp,
            fontFamily = poppinsFontFamily,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = bio,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = poppinsFontFamily,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BackgroundWithProfile(
    backgroundImageUrl: String?,
    profilePictureUrl: String?,
    onBackgroundClick: () -> Unit,
    onProfilePictureClick: () -> Unit,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box {
        // Arka plan resmi
        Card(
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clickable { onBackgroundClick() }
        ) {
            AsyncImage(
                model = backgroundImageUrl ?: R.drawable.pic2,
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
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { onProfilePictureClick() },
                contentScale = ContentScale.Crop
            )
        }

        // Geri ve ayarlar ikonları
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Menu, contentDescription = "More options", tint = MaterialTheme.colorScheme.onBackground)
            }
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
            fontSize = 18.sp,
            fontFamily = poppinsFontFamily,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(label, fontFamily = poppinsFontFamily, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
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
