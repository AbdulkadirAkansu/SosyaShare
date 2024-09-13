package com.akansu.sosyashare.presentation.editprofile

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.akansu.sosyashare.R
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.presentation.editprofile.viewmodel.EditProfileViewModel
import com.akansu.sosyashare.util.FileUtils
import com.akansu.sosyashare.util.PermissionHandler
import com.akansu.sosyashare.util.poppinsFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavHostController,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val profilePictureUrl by viewModel.profilePictureUrl.collectAsState()
    val backgroundImageUrl by viewModel.backgroundImageUrl.collectAsState()
    var username by remember { mutableStateOf(viewModel.username.value) }
    var bio by remember { mutableStateOf(viewModel.bio.value) }
    val posts by viewModel.posts.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val canChangeUsername by viewModel.canChangeUsername.collectAsState()

    val profileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val file = FileUtils.createFileFromUri(context, it)
                file?.let { viewModel.uploadProfilePicture(file, {}, {}) }
            }
        }

    val backgroundLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val file = FileUtils.createFileFromUri(context, it)
                file?.let { viewModel.uploadBackgroundImage(file, {}, {}) }
            }
        }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showChangeDialog by remember { mutableStateOf(false) }
    var changeField by remember { mutableStateOf("") }
    var postToDelete by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(errorMessage, successMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Delete Post") },
            text = { Text("Are you sure you want to delete this post?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        postToDelete?.let { viewModel.deletePost(it) }
                        showConfirmDialog = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showChangeDialog) {
        AlertDialog(
            onDismissRequest = { showChangeDialog = false },
            title = { Text("Confirm Changes") },
            text = { Text("Are you sure you want to change the $changeField?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        when (changeField) {
                            "Username" -> viewModel.updateUsername(username)
                            "Bio" -> viewModel.updateBio(bio)
                        }
                        showChangeDialog = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Profile",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { paddingValues ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            // Background Image Section - Tam genişlik sığdırma
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Yüksekliği sabitleyerek tam genişlik ayarlandı
                    .clip(RoundedCornerShape(bottomEnd = 24.dp, bottomStart = 24.dp))
                    .clickable {
                        backgroundLauncher.launch("image/*")
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(backgroundImageUrl ?: R.drawable.pic2),
                    contentDescription = "Background Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = "Tap to Change Background",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(8.dp)
                )
            }

            // Profil Resmi ve Offset Ayarlama
            Column(
                modifier = Modifier
                    .offset(y = (-50).dp) // Profil resmini yukarı taşımak için offset
                    .align(Alignment.CenterHorizontally)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(profilePictureUrl ?: R.drawable.profile),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable {
                            profileLauncher.launch("image/*")
                        },
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Username Input Field with Check Icon - Padding ve Margin ile UI Sorunu Çözüldü
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = {
                        Text(
                            "Username",
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = canChangeUsername,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray
                    ),
                    singleLine = true
                )
                IconButton(onClick = {
                    changeField = "Username"
                    showChangeDialog = true
                }) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Update Username")
                }
            }

            // Bio Input Field with Check Icon - Padding ile UI Sorunu Çözüldü
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = {
                        Text(
                            "Bio",
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray
                    ),
                    singleLine = true
                )
                IconButton(onClick = {
                    changeField = "Bio"
                    showChangeDialog = true
                }) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Update Bio")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Post Grid with Posts Title
            Text(
                text = "Posts",
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(8.dp)
            )
            PostGrid(posts = posts, onDeletePost = { postId ->
                postToDelete = postId
                showConfirmDialog = true
            })

            Spacer(modifier = Modifier.height(16.dp))

            // General Save Button
            Button(
                onClick = {
                    Toast.makeText(context, "Changes saved successfully!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Save All Changes",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PostGrid(posts: List<Post>, onDeletePost: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(0.dp),
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
                    Text(
                        text = "No posts yet",
                        fontFamily = poppinsFontFamily,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        } else {
            items(posts.size) { index ->
                val post = posts[index]
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.dp)
                        .clickable { onDeletePost(post.id) }
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(post.imageUrl),
                        contentDescription = "Post",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clickable { onDeletePost(post.id) }
                    )
                }
            }
        }
    }
}
