package com.akansu.sosyashare.presentation.home.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.akansu.sosyashare.R
import com.akansu.sosyashare.util.PermissionHandler
import java.io.File


@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    profilePictureUrl: String?
) {
    val isMenuExtended = remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    // Galeri launcher
    val openDocumentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                // Seçilen resmi PostCreation'a yönlendir
                navController.navigate("post_creation?imageUri=$it")
            }
        }

    // Kamera launcher
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                // Çekilen fotoğrafı PostCreation'a yönlendir
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp) // Yanlardan padding verdik
            .padding(bottom = 16.dp) // Alt tarafa da padding verdik
            .offset(y = 15.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .align(Alignment.BottomCenter),  // Sabit alt pozisyon
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
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
                    iconSize = 33.dp,  // Search ikonu biraz daha büyük
                    modifier = Modifier.padding(start = 15.dp) // Search ikonunu sağa kaydırıyoruz
                )

                IconButton(
                    onClick = { isMenuExtended.value = !isMenuExtended.value },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.more),
                        contentDescription = "More",
                        modifier = Modifier.size(28.dp),
                    )
                }

                BottomNavigationItem(
                    icon = R.drawable.trend,
                    contentDescription = "Trend",
                    onClick = { navController.navigate("trend") },
                    modifier = Modifier.padding(end = 15.dp) // Trend ikonunu sola kaydırıyoruz
                )

                Image(
                    painter = rememberAsyncImagePainter(profilePictureUrl ?: R.drawable.profile),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable {
                            navController.navigate("userprofile")
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }

        // FAB'ler NavigationBar'ın üzerinde açılır
        if (isMenuExtended.value) {
            ModernFabGroup(
                onCameraClick = {
                    // Kamera açılır, gerekli izinler kontrol edilir
                    if (PermissionHandler.hasCameraPermission(context as Activity)) {
                        selectedImageUri = createImageFileUri()
                        cameraLauncher.launch(selectedImageUri!!)
                    } else {
                        PermissionHandler.requestCameraPermission(context)
                    }
                },
                onGalleryClick = {
                    // Galeri açılır
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
    // Yan FAB butonların ölçek ve alfa animasyonları
    val fabScale = if (isExtended) 1f else 0f
    val fabAlpha = if (isExtended) 1f else 0f

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 40.dp) // FAB'leri daha da aşağı kaydırdım
    ) {
        if (isExtended) {
            ModernRectFab(
                icon = painterResource(R.drawable.gallery),  // Star ikonu yerine gallery.xml kullanıldı
                contentDescription = "Gallery",
                onClick = onGalleryClick,
                scale = fabScale,
                alpha = fabAlpha,
                offsetX = -40f,
                offsetY = -40f // Aşağı kaydırıldı
            )

            ModernRectFab(
                icon = painterResource(R.drawable.camera),  // Share ikonu yerine camera.xml kullanıldı
                contentDescription = "Camera",
                onClick = onCameraClick,
                scale = fabScale,
                alpha = fabAlpha,
                offsetX = 40f,
                offsetY = -40f // Aşağı kaydırıldı
            )
        }
    }
}

@Composable
fun ModernRectFab(
    icon: Painter,  // ImageVector yerine Painter kullanıldı
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
            painter = icon,  // ImageVector yerine Painter kullanıldı
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
    modifier: Modifier = Modifier // Modifier parametresini ekliyoruz
) {
    Icon(
        painter = painterResource(id = icon),
        contentDescription = contentDescription,
        tint = MaterialTheme.colorScheme.onBackground,
        modifier = modifier  // Burada modifier'ı kullanıyoruz
            .size(iconSize)
            .clickable { onClick() }
    )
}
