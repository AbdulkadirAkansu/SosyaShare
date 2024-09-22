package com.akansu.sosyashare.presentation.userprofile

import android.app.Activity
import android.net.Uri
import android.os.Build
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.akansu.sosyashare.R
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.presentation.home.components.FollowersFollowingDialog
import com.akansu.sosyashare.presentation.home.components.NavigationBar
import com.akansu.sosyashare.presentation.login.viewmodel.AuthViewModel
import com.akansu.sosyashare.presentation.share.viewmodel.ShareViewModel
import com.akansu.sosyashare.presentation.userprofile.viewmodel.UserProfileViewModel
import com.akansu.sosyashare.util.FileUtils
import com.akansu.sosyashare.util.poppinsFontFamily
import com.google.accompanist.systemuicontroller.rememberSystemUiController


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
    var followers by remember { mutableStateOf<List<User>>(emptyList()) }
    var following by remember { mutableStateOf<List<User>>(emptyList()) }
    var profilePictureUrl by remember { mutableStateOf(userDetails?.profilePictureUrl) }
    var bio by remember { mutableStateOf(userDetails?.bio ?: "") }
    var followersCount by remember { mutableIntStateOf(userDetails?.followers?.size ?: 0) }
    var followingCount by remember { mutableIntStateOf(userDetails?.following?.size ?: 0) }
    val posts by shareViewModel.userPosts.collectAsState() // Postlar LiveData'dan veya Flow'dan alınıyor
    var sortedPosts = remember(posts) { posts.sortedByDescending { it.createdAt } }
    val systemUiController = rememberSystemUiController()
    val context = LocalContext.current as Activity
    val view = LocalView.current

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(context.window, false)
        } else {
            WindowInsetsControllerCompat(context.window, view).isAppearanceLightStatusBars = false
        }
        systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = false)
    }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            profileViewModel.getUserDetails(userId) {
                userDetails = it
            }
        }
    }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            profileViewModel.loadCurrentUser(userId)
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

    LaunchedEffect(userDetails) {
        profilePictureUrl = userDetails?.profilePictureUrl
        bio = userDetails?.bio ?: ""
        followersCount = userDetails?.followers?.size ?: 0
        followingCount = userDetails?.following?.size ?: 0
    }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val file = FileUtils.createFileFromUri(context, it)
                file?.let {
                    profileViewModel.uploadProfilePicture(file, onSuccess = { newUrl ->
                        profilePictureUrl = newUrl
                        userDetails = userDetails?.copy(profilePictureUrl = newUrl)
                        profileViewModel.updateUserProfilePictureUrl(newUrl)
                    }, onFailure = { e ->
                        Toast.makeText(
                            context,
                            "Failed to upload profile picture: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                } ?: run {
                    Toast.makeText(context, "Failed to convert URI to File", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    val backgroundLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val file = FileUtils.createFileFromUri(context, it)
                file?.let {
                    profileViewModel.uploadBackgroundImage(file, onSuccess = { newUrl ->
                        profileViewModel.updateBackgroundImageUrl(newUrl)
                    }, onFailure = { e ->
                        Toast.makeText(
                            context,
                            "Failed to upload background image: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                } ?: run {
                    Toast.makeText(context, "Failed to convert URI to File", Toast.LENGTH_SHORT)
                        .show()
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
                .padding(bottom = paddingValues.calculateBottomPadding() + 72.dp)
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

                Spacer(modifier = Modifier.height(20.dp))

                ActionButtons(navController = navController)

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
                        text = posts.size.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                PostGrid(
                    posts = sortedPosts.mapNotNull { it.imageUrl },
                    userId = currentUser?.uid ?: "",
                    navController = navController,
                    gridHeight = 500.dp
                )
            }
        }
    }
}


@Composable
fun PostGrid(
    posts: List<String>,
    userId: String,
    navController: NavHostController,
    gridHeight: Dp
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxWidth()
            .height(gridHeight),
        contentPadding = PaddingValues(16.dp),
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
                        .clickable {
                            navController.navigate("post_detail/${userId}/${index}/false")
                        }
                ) {
                    AsyncImage(
                        model = posts[index],
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
            Text(
                "Edit Profile",
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
        Button(
            onClick = { /* TODO: Share functionality */ },
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
                "Share Profile",
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
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

@Composable
fun BackgroundWithProfile(
    backgroundImageUrl: String?,
    profilePictureUrl: String?,
    onBackgroundClick: () -> Unit,
    onProfilePictureClick: () -> Unit,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Card(
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clickable { onBackgroundClick() }
        ) {
            AsyncImage(
                model = backgroundImageUrl ?: R.drawable.pic2,
                contentDescription = "Background Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

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
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onBackground
                )
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

