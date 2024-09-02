package com.akansu.sosyashare.presentation.share

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.akansu.sosyashare.presentation.share.viewmodel.ShareViewModel
import com.akansu.sosyashare.util.FileUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCreationScreen(
    navController: NavHostController,
    imageUri: Uri?,
    shareViewModel: ShareViewModel = hiltViewModel()
) {
    var comment by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showSnackbar by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Log.d("PostCreationScreen", "PostCreationScreen launched with imageUri: $imageUri")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yeni Gönderi") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri Dön")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                if (isUploading) {
                    Log.d("PostCreationScreen", "Image is uploading...")
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    imageUri?.let {
                        Log.d("PostCreationScreen", "Displaying selected image: $it")
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = "Seçilen Resim",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Bir başlık yazın...") }
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    isUploading = true
                    Log.d("PostCreationScreen", "Share button clicked")
                    imageUri?.let { uri ->
                        // createFileFromUri ile dosya oluşturma
                        val file = FileUtils.createFileFromUri(context, uri)
                        file?.let { selectedFile ->
                            Log.d("PostCreationScreen", "File created from URI: ${selectedFile.absolutePath}")
                            shareViewModel.uploadPostPicture(selectedFile, comment) {
                                isUploading = false
                                showSnackbar = true
                                Log.d("PostCreationScreen", "Image uploaded successfully")
                                navController.navigate("userprofile") {
                                    popUpTo("post_creation") { inclusive = true }
                                }
                            }
                        } ?: run {
                            isUploading = false
                            Log.e("PostCreationScreen", "Failed to create file from URI")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Paylaş")
            }
        }
    }

    if (showSnackbar) {
        LaunchedEffect(snackbarHostState) {
            snackbarHostState.showSnackbar("Gönderi paylaşıldı")
            showSnackbar = false
        }
    }
}
