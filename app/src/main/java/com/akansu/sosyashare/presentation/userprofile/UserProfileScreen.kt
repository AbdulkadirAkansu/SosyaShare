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
import coil.compose.rememberAsyncImagePainter
import com.akansu.sosyashare.R
import com.akansu.sosyashare.data.model.UserEntity
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.presentation.home.components.NavigationBar
import com.akansu.sosyashare.presentation.login.viewmodel.AuthViewModel
import com.akansu.sosyashare.presentation.share.viewmodel.ShareViewModel
import com.akansu.sosyashare.presentation.userprofile.viewmodel.UserProfileViewModel
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

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            profileViewModel.getUserDetails(userId) {
                userDetails = it
            }
        }
    }

    var profilePictureUrl by remember { mutableStateOf(userDetails?.profilePictureUrl) }
    var bio by remember { mutableStateOf(userDetails?.bio ?: "") }

    LaunchedEffect(userDetails) {
        profilePictureUrl = userDetails?.profilePictureUrl
        bio = userDetails?.bio ?: ""
    }

    val posts by shareViewModel.userPosts.collectAsState()

    val context = LocalContext.current as Activity

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { it ->
            profileViewModel.uploadProfilePicture(it, onSuccess = { newUrl ->
                profilePictureUrl = newUrl
                userDetails = userDetails?.copy(profilePictureUrl = newUrl)
                profileViewModel.updateUserProfilePictureUrl(newUrl)
            }, onFailure = { e ->
                Toast.makeText(context, "Failed to upload profile picture: ${e.message}", Toast.LENGTH_SHORT).show()
            })
        }
    }

    LaunchedEffect(Unit) {
        shareViewModel.refreshUserPosts()
    }

    var selectedItem by remember { mutableIntStateOf(4) }

    val username = userDetails?.username ?: "Unknown"

    Scaffold(
        topBar = {
            TopBar(navController, username)
        },
        bottomBar = {
            NavigationBar(
                selectedItem = selectedItem,
                onItemSelected = { selectedItem = it },
                navController = navController,
                profilePictureUrl = profilePictureUrl
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
                    username = username,
                    profilePictureUrl = profilePictureUrl,
                    bio = bio,
                    onProfilePictureClick = {
                        if (PermissionHandler.hasReadExternalStoragePermission(context)) {
                            launcher.launch("image/*")
                        } else {
                            PermissionHandler.requestReadExternalStoragePermission(context)
                        }
                    }
                )
                UserStatistics(
                    postCount = posts.size,
                    followersCount = userDetails?.followers?.size ?: 0,
                    followingCount = userDetails?.following?.size ?: 0
                )
                ActionButtons(navController)
                Spacer(modifier = Modifier.height(8.dp))
                PostGrid(posts = posts.mapNotNull { it.imageUrl }, userId = currentUser?.uid ?: "", navController = navController)


            }
        }
    }
}

@Composable
fun TopBar(navController: NavHostController, username: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navController.navigateUp() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            username,
            modifier = Modifier.weight(1f),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = poppinsFontFamily,
            color = MaterialTheme.colorScheme.onBackground
        )
        IconButton(onClick = { navController.navigate("settings") }) {
            Icon(Icons.Default.Menu, contentDescription = "More options", tint = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
fun ProfileInfo(username: String, profilePictureUrl: String?, bio: String, onProfilePictureClick: () -> Unit) {
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
                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { onProfilePictureClick() },
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
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun UserStatistics(postCount: Int, followersCount: Int, followingCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatColumn(postCount.toString(), "Posts")
        StatColumn(followersCount.toString(), "Followers")
        StatColumn(followingCount.toString(), "Following")
    }
}

@Composable
fun StatColumn(count: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
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
fun ActionButtons(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { navController.navigate("editprofile") },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Edit Profile", color = MaterialTheme.colorScheme.onPrimary, fontFamily = poppinsFontFamily)
        }
        Button(
            onClick = { },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Share Profile", color = MaterialTheme.colorScheme.onPrimary, fontFamily = poppinsFontFamily)
        }
    }
}

@Composable
fun PostGrid(posts: List<String>, userId: String, navController: NavHostController) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.height(500.dp)
    ) {
        if (posts.isNullOrEmpty()) {
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
                        .padding(1.dp)
                        .clickable {
                            navController.navigate("post_detail/$userId/$index")
                        }
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(posts[index]),
                        contentDescription = "Post",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}