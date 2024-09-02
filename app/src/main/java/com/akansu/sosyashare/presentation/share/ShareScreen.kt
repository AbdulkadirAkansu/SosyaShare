package com.akansu.sosyashare.presentation.share

import android.net.Uri
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.akansu.sosyashare.R
import com.akansu.sosyashare.presentation.share.viewmodel.ShareViewModel
import com.akansu.sosyashare.util.FileUtils
import com.akansu.sosyashare.util.PermissionHandler
import com.akansu.sosyashare.util.poppinsFontFamily
import java.io.File


@Composable
fun ShareScreen(
    navController: NavHostController,
    shareViewModel: ShareViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    Log.d("ShareScreen", "ShareScreen launched")

    val openDocumentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            // Kullanıcıya seçilen URI için kalıcı erişim izni veriyoruz
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)

            // Seçilen URI'den dosya oluşturuyoruz
            val file = FileUtils.createFileFromUri(context, it)

            // Dosya başarılı bir şekilde oluşturulduysa PostCreationScreen'e yönlendiriyoruz
            file?.let { selectedFile ->
                navController.navigate("post_creation?imageUri=${Uri.fromFile(selectedFile)}")
            } ?: run {
                Toast.makeText(context, "Failed to create file from URI", Toast.LENGTH_SHORT).show()
            }
        }
    }


    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        Log.d("ShareScreen", "Camera photo taken, success: $success")
        if (success) {
            navController.navigate("post_creation?imageUri=$selectedImageUri")
        }
    }

    fun createImageFileUri(): Uri {
        Log.d("ShareScreen", "Creating image file URI")
        val storageDir: File = context.getExternalFilesDir(null) ?: throw IllegalStateException("External storage not available")
        val file = File.createTempFile("IMG_", ".jpg", storageDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {}
    ) {
        if (selectedImageUri != null) {
            Log.d("ShareScreen", "Displaying selected image: $selectedImageUri")
            Image(
                painter = rememberAsyncImagePainter(selectedImageUri),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        Log.d("ShareScreen", "Image clicked, launching gallery")
                        openDocumentLauncher.launch(arrayOf("image/*"))
                    },
                contentScale = ContentScale.Crop
            )
        } else {
            Log.d("ShareScreen", "Displaying default share photo")
            Image(
                painter = painterResource(id = R.drawable.sharephoto),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 0.6f
                    },
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White
                        ),
                        startY = 0f,
                        endY = 1000f
                    ), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            ShareCard(
                text = "Galeriden Seç",
                gradient = Brush.horizontalGradient(listOf(Color(0xFF56CCF2), Color(0xFF2F80ED))),
                onClick = {
                    Log.d("ShareScreen", "Launching gallery to select image")
                    openDocumentLauncher.launch(arrayOf("image/*"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )
            ShareCard(
                text = "Fotoğraf Çek",
                gradient = Brush.horizontalGradient(listOf(Color(0xFFF2994A), Color(0xFFF2C94C))),
                onClick = {
                    Log.d("ShareScreen", "Attempting to launch camera")
                    if (PermissionHandler.hasCameraPermission(context as Activity)) {
                        selectedImageUri = createImageFileUri()
                        cameraLauncher.launch(selectedImageUri!!)
                    } else {
                        PermissionHandler.requestCameraPermission(context)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShareCard(
    text: String,
    gradient: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Box(
        modifier = modifier
            .scale(scale)
            .background(gradient, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .pointerInteropFilter {
                when (it.action) {
                    MotionEvent.ACTION_DOWN -> isPressed = true
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> isPressed = false
                }
                false
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = poppinsFontFamily
        )
    }
}