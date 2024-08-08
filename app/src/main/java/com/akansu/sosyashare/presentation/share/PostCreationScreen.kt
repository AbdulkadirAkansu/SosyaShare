package com.akansu.sosyashare.presentation.share

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import coil.request.ImageRequest
import com.akansu.sosyashare.R
import com.akansu.sosyashare.presentation.share.viewmodel.ShareViewModel
import com.akansu.sosyashare.util.poppinsFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCreationScreen(
    navController: NavHostController,
    imageUri: Uri,
    shareViewModel: ShareViewModel = hiltViewModel()
) {
    var comment by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showSnackbar by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            navController.navigate("post_creation?imageUri=$it") {
                popUpTo("post_creation") { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Post", fontFamily = poppinsFontFamily, fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (isUploading) {
                    CircularProgressIndicator()
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current).data(data = imageUri)
                                .apply {
                                    crossfade(true)
                                    placeholder(R.drawable.profile)
                                }.build()
                        ),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("Write a caption...", fontFamily = poppinsFontFamily, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    focusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = poppinsFontFamily, color = MaterialTheme.colorScheme.onBackground)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    isUploading = true
                    shareViewModel.uploadPostPicture(imageUri, comment) {
                        isUploading = false
                        showSnackbar = true
                        navController.navigate("userprofile") {
                            popUpTo("post_creation") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Share", color = MaterialTheme.colorScheme.onPrimary, fontFamily = poppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }

    if (showSnackbar) {
        LaunchedEffect(snackbarHostState) {
            snackbarHostState.showSnackbar("Post paylaşıldı")
            showSnackbar = false
        }
    }
}
