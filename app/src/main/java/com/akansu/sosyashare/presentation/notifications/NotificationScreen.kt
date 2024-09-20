package com.akansu.sosyashare.presentation.notifications

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.akansu.sosyashare.domain.model.Notification
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.presentation.postdetail.viewmodel.PostDetailViewModel
import com.akansu.sosyashare.util.poppinsFontFamily
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    userId: String,
    onNotificationClick: (String) -> Unit,
    viewModel: NotificationViewModel = hiltViewModel(),
    postDetailViewModel: PostDetailViewModel = hiltViewModel() // Post verilerini almak için
) {
    val notifications by viewModel.notifications.collectAsState()
    val error by viewModel.error.collectAsState()
    val posts by postDetailViewModel.posts.collectAsState() // Postları burada alıyoruz

    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.loadNotificationsAndMarkAsRead(userId)
        postDetailViewModel.loadUserDetails(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notifications",
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = poppinsFontFamily)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tüm bildirimleri sil", fontFamily = poppinsFontFamily) },
                            onClick = {
                                showMenu = false
                                viewModel.clearAllNotifications()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (error != null) {
                Text(
                    text = error ?: "Bir hata oluştu",
                    color = MaterialTheme.colorScheme.error,
                    fontFamily = poppinsFontFamily
                )
            } else {
                LazyColumn {
                    items(notifications.reversed(), key = { it.documentId }) { notification ->
                        NotificationItem(
                            notification = notification,
                            onNotificationClick = {
                                handleNotificationClick(
                                    navController = navController,
                                    notification = notification,
                                    posts = posts
                                )
                            },
                            onDelete = {
                                viewModel.deleteNotification(notification.documentId)
                            }
                        )
                    }
                }
            }
        }
    }
}



// Bildirime tıklandığında ilgili ekrana yönlendirme yapılır
fun handleNotificationClick(
    navController: NavController,
    notification: Notification,
    posts: List<Post>
) {
    when (notification.type) {
        "comment" -> {
            navController.navigate("comments/${notification.postId}/${notification.userId}")
        }
        "like" -> {
            val postIndex = posts.indexOfFirst { it.id == notification.postId }
            Log.d("NotificationClick", "PostId from notification: ${notification.postId}")  // Log eklendi
            Log.d("NotificationClick", "Posts available: ${posts.map { it.id }}")  // Tüm postların id'lerini loglayalım
            Log.d("NotificationClick", "PostIndex for liked post: $postIndex")  // Log eklendi
            if (postIndex != -1) {
                navController.navigate("post_detail/${notification.userId}/$postIndex/true")
                Log.d("NotificationClick", "Navigating to post detail for index: $postIndex")
            } else {
                Log.d("NotificationClick", "Post bulunamadı") // Hata durumunda log
            }
        }
        "follow" -> {
            navController.navigate("profile/${notification.senderId}")
        }
        "unfollow" -> {
            navController.navigate("profile/${notification.senderId}")
        }
        else -> {
            // Başka bir bildirim türü varsa burada işlemler yapılabilir
        }
    }
}



@Composable
fun NotificationItem(
    notification: Notification,
    onNotificationClick: () -> Unit,
    onDelete: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffsetX by animateDpAsState(targetValue = offsetX.dp)
    val threshold = 100f
    val backgroundColor = if (notification.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = animatedOffsetX)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX < -threshold) {
                            onDelete() // Silme işlemi tetiklenir
                        }
                        offsetX = 0f // Kart geri sıfırlanır
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX = (offsetX + dragAmount).coerceIn(-300f, 0f)
                    }
                )
            }
            .padding(vertical = 8.dp)
            .background(
                color = backgroundColor,
                shape = if (!notification.isRead) RoundedCornerShape(16.dp) else RoundedCornerShape(0.dp)
            ) // Radius ekledik
            .clickable(onClick = onNotificationClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profil fotoğrafı (CircleAvatar)
            Image(
                painter = rememberAsyncImagePainter(notification.senderProfileUrl ?: ""),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Bildirim metni
                Text(
                    text = notification.content,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (notification.isRead) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Bildirim tarihi
                val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(notification.timestamp)
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontFamily = poppinsFontFamily
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
