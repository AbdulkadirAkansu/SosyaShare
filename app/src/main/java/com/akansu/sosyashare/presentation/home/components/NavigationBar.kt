package com.akansu.sosyashare.presentation.home.components

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.akansu.sosyashare.R
import com.akansu.sosyashare.util.PermissionHandler
import java.io.File


@SuppressLint("RememberReturnType")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    profilePictureUrl: String?
) {
    val isMenuExtended = remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    var lastClickTime by remember { mutableStateOf(0L) }
    val handler = remember { Handler(Looper.getMainLooper()) }
    var isDoubleClick by remember { mutableStateOf(false) }

    // Galeri launcher
    val openDocumentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                navController.navigate("post_creation?imageUri=${Uri.encode(it.toString())}")
            }
        }

    // Kamera launcher
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                navController.navigate("post_creation?imageUri=$selectedImageUri")
            }
        }

    // Kamera resmi için geçici bir dosya oluşturur
    fun createImageFileUri(): Uri {
        val storageDir: File = context.getExternalFilesDir(null)
            ?: throw IllegalStateException("External storage not available")
        val file = File.createTempFile("IMG_", ".jpg", storageDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    // Navigation Bar'ın düzenlemesi
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavigationItem(
                    icon = R.drawable.home,
                    contentDescription = "Home",
                    onClick = { navController.navigate("home") }
                )

                BottomNavigationItem(
                    icon = R.drawable.search,
                    contentDescription = "Search",
                    onClick = { navController.navigate("search") },
                    iconSize = 30.dp,
                    modifier = Modifier.padding(start = 12.dp)
                )

                IconButton(
                    onClick = { isMenuExtended.value = !isMenuExtended.value },
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.more),
                        contentDescription = "More",
                        modifier = Modifier.size(26.dp),
                    )
                }

                BottomNavigationItem(
                    icon = R.drawable.trend,
                    contentDescription = "Trend",
                    onClick = { navController.navigate("trend") },
                    modifier = Modifier.padding(end = 12.dp)
                )

                Image(
                    painter = rememberAsyncImagePainter(profilePictureUrl ?: R.drawable.profile),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .pointerInteropFilter { event ->
                            if (event.action == MotionEvent.ACTION_DOWN) {
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastClickTime < 300) {
                                    isDoubleClick = true
                                    handler.removeCallbacksAndMessages(null)
                                    navController.navigate("settings")
                                } else {
                                    isDoubleClick = false
                                    lastClickTime = currentTime
                                    handler.postDelayed({
                                        if (!isDoubleClick) {
                                            navController.navigate("userprofile")
                                        }
                                    }, 300)
                                }
                            }
                            true
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }

        if (isMenuExtended.value) {
            ModernFabGroup(
                onCameraClick = {
                    if (PermissionHandler.hasCameraPermission(context as Activity)) {
                        selectedImageUri = createImageFileUri()
                        cameraLauncher.launch(selectedImageUri!!)
                    } else {
                        PermissionHandler.requestCameraPermission(context)
                    }
                },
                onGalleryClick = {
                    openDocumentLauncher.launch(arrayOf("image/*"))
                },
                isExtended = isMenuExtended.value
            )
        }
    }
}


@Composable
fun ModernFabGroup(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    isExtended: Boolean
) {
    val fabScale = if (isExtended) 1f else 0f
    val fabAlpha = if (isExtended) 1f else 0f

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 40.dp)
    ) {
        if (isExtended) {
            ModernRectFab(
                icon = painterResource(R.drawable.gallery),
                contentDescription = "Gallery",
                onClick = onGalleryClick,
                scale = fabScale,
                alpha = fabAlpha,
                offsetX = -40f,
                offsetY = -40f
            )

            ModernRectFab(
                icon = painterResource(R.drawable.camera),
                contentDescription = "Camera",
                onClick = onCameraClick,
                scale = fabScale,
                alpha = fabAlpha,
                offsetX = 40f,
                offsetY = -40f
            )
        }
    }
}

@Composable
fun ModernRectFab(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    scale: Float,
    alpha: Float,
    offsetX: Float,
    offsetY: Float
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .offset(x = offsetX.dp, y = offsetY.dp)
            .scale(scale)
            .alpha(alpha)
            .size(60.dp),
        shape = RoundedCornerShape(10.dp),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
fun BottomNavigationItem(
    icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    iconSize: Dp = 26.dp,
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(id = icon),
        contentDescription = contentDescription,
        tint = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
            .size(iconSize)
            .clickable { onClick() }
    )
}
