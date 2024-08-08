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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.akansu.sosyashare.R
import com.akansu.sosyashare.presentation.editprofile.viewmodel.EditProfileViewModel
import com.akansu.sosyashare.util.PermissionHandler
import com.akansu.sosyashare.util.poppinsFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavHostController, viewModel: EditProfileViewModel = hiltViewModel()) {
    val context = LocalContext.current

    val user by viewModel.user.collectAsState()
    val profilePictureUrl by viewModel.profilePictureUrl.collectAsState()
    var username by remember { mutableStateOf(viewModel.username.value) }
    var bio by remember { mutableStateOf(viewModel.bio.value) }
    val posts by viewModel.posts.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val canChangeUsername by viewModel.canChangeUsername.collectAsState()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.updateProfilePicture(it)
        }
    }

    LaunchedEffect(errorMessage, successMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSaveChangesDialog by remember { mutableStateOf(false) }
    var postToDelete by remember { mutableStateOf<String?>(null) }

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

    if (showSaveChangesDialog) {
        AlertDialog(
            onDismissRequest = { showSaveChangesDialog = false },
            title = { Text("Save Changes") },
            text = { Text("Do you want to save the changes?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.saveChanges(username, bio)
                        navController.navigateUp()
                        showSaveChangesDialog = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveChangesDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontFamily = poppinsFontFamily) },
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
            Image(
                painter = rememberAsyncImagePainter(profilePictureUrl ?: R.drawable.profile),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable {
                        if (PermissionHandler.hasReadExternalStoragePermission(context as Activity)) {
                            launcher.launch("image/*")
                        } else {
                            PermissionHandler.requestReadExternalStoragePermission(context as Activity)
                        }
                    },
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", fontFamily = poppinsFontFamily) },
                modifier = Modifier.fillMaxWidth(),
                enabled = canChangeUsername
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio", fontFamily = poppinsFontFamily) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    showSaveChangesDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Save", color = MaterialTheme.colorScheme.onPrimary, fontFamily = poppinsFontFamily)
            }
            Spacer(modifier = Modifier.height(12.dp))
            PostGrid(posts = posts, onDeletePost = { postUrl ->
                postToDelete = postUrl
                showConfirmDialog = true
            })
        }
    }
}

@Composable
fun PostGrid(posts: List<String>, onDeletePost: (String) -> Unit) {
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
                    Text(text = "No posts yet", fontFamily = poppinsFontFamily, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        } else {
            items(posts.size) { index ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(1.dp)
                        .clickable {
                            onDeletePost(posts[index])
                        }
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(posts[index]),
                        contentDescription = "Post",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .clickable { onDeletePost(posts[index]) }
                    )
                }
            }
        }
    }
}
