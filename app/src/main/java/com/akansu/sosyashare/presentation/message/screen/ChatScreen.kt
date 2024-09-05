package com.akansu.sosyashare.presentation.message.screen

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.akansu.sosyashare.R
import com.akansu.sosyashare.domain.model.Message
import com.akansu.sosyashare.presentation.message.viewmodel.ChatViewModel
import com.akansu.sosyashare.presentation.postdetail.screen.FullScreenImage
import com.akansu.sosyashare.util.poppinsFontFamily
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChatScreen(
    navController: NavHostController,
    userId: String,
    viewModel: ChatViewModel = hiltViewModel(),
    forwardedMessage: String? = null
) {
    val messages by viewModel.messages.collectAsState()
    val otherUser by viewModel.otherUser.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var newMessage by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    var selectedMessage by remember { mutableStateOf<Message?>(null) }
    var isMenuVisible by remember { mutableStateOf(false) }
    var replyToMessage by remember { mutableStateOf<Message?>(null) }
    var showFullScreenImage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Log.d("ChatScreen", "Chat loaded for userId: $userId")

    LaunchedEffect(userId) {
        viewModel.listenForMessages(userId)
    }


    LaunchedEffect(forwardedMessage) {
        forwardedMessage?.let {
            Log.d("ChatScreen", "Forwarded message received: $it, sending it now.")
            viewModel.sendMessage(userId, it)
        }
    }



    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color(0xFF121212) else Color(0xFFF5F5F5)
    val surfaceColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val bubbleColorOwn = if (isDarkTheme) Color(0xFFFFF6E2) else Color(0xFFDCF8C6)
    val bubbleColorOther = if (isDarkTheme) Color(0xFF252525) else Color(0xFFE4E6EB)


    Scaffold(
        topBar = {
            ChatTopBar(
                onBackClick = { navController.popBackStack() },
                userProfileUrl = otherUser?.profilePictureUrl,
                username = otherUser?.username,
                textColor = textColor
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(surfaceColor, RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    var lastSenderId: String? = null
                    var lastMessageTimestamp: Long? = null

                    items(messages) { message ->
                        Log.d("ChatScreen", "Rendering message with ID: ${message.id}, content: ${message.content}")
                        val messageTimestamp = message.timestamp.time
                        val messageDayTime = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(messageTimestamp)

                        val shouldShowTime = lastMessageTimestamp == null || (messageTimestamp - lastMessageTimestamp!!) > 300000

                        if (shouldShowTime) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = messageDayTime,
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                )
                            }
                            lastMessageTimestamp = messageTimestamp
                        }
                        ChatBubble(
                            message = message,
                            isOwnMessage = message.senderId == currentUser?.id,
                            ownUserProfileUrl = currentUser?.profilePictureUrl,
                            otherUserProfileUrl = otherUser?.profilePictureUrl,
                            showAvatar = lastSenderId != message.senderId,
                            isFirstMessage = lastSenderId != message.senderId,
                            bubbleColorOwn = bubbleColorOwn,
                            bubbleColorOther = bubbleColorOther,
                            textColor = textColor,
                            onMessageLongPress = {
                                selectedMessage = it
                                isMenuVisible = true
                            },
                            onImageClick = { imageUrl ->
                                showFullScreenImage = imageUrl
                            },
                            onImageLongPress = { imageUrl ->
                                selectedMessage = message
                                isMenuVisible = true
                            },
                            currentUserId = currentUser?.id ?: "",
                            otherUsername = otherUser?.username,
                            messages = messages,
                        )

                        lastSenderId = message.senderId
                    }

                    if (isTyping) {
                        item {
                            TypingIndicator(bubbleColorOther)
                        }
                    }
                }

                if (isMenuVisible && selectedMessage != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        DropdownMenu(
                            expanded = isMenuVisible,
                            onDismissRequest = { isMenuVisible = false },
                            modifier = Modifier.background(Color.White).padding(8.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Yanıtla") },
                                onClick = {
                                    replyToMessage = selectedMessage
                                    isMenuVisible = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("İlet") },
                                onClick = {
                                    selectedMessage?.let { message ->
                                        val encodedContent = Uri.encode(message.content) // Resim ya da metin içeriği encode ediliyor
                                        navController.navigate("new_message_screen/$encodedContent") {
                                            popUpTo("new_message_screen") { inclusive = true }
                                        }
                                    }
                                    isMenuVisible = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Galeriye Kaydet") },
                                onClick = {
                                    val imageUrl = selectedMessage?.content ?: return@DropdownMenuItem
                                    coroutineScope.launch {
                                        viewModel.saveImageToGallery(imageUrl, context)
                                    }
                                    isMenuVisible = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Gönderimi Sil") },
                                onClick = {
                                    viewModel.deleteMessage(selectedMessage!!.id)
                                    isMenuVisible = false
                                }
                            )
                        }
                    }
                }
            }

            replyToMessage?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Gray.copy(alpha = 0.2f))
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (it.senderId == currentUser?.id) "Siz" else otherUser?.username ?: "Kullanıcı",
                                fontFamily = poppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = textColor
                            )
                            Text(
                                text = it.content,
                                fontFamily = poppinsFontFamily,
                                fontSize = 12.sp,
                                color = textColor.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(onClick = { replyToMessage = null }) {
                            Icon(Icons.Default.Close, contentDescription = "İptal Et")
                        }
                    }
                }
            }

            ChatInputField(
                value = newMessage,
                onValueChange = {
                    newMessage = it
                    isTyping = it.isNotEmpty()
                },
                onSendClick = {
                    if (newMessage.isNotBlank()) {
                        if (replyToMessage != null) {
                            viewModel.replyToMessage(userId, replyToMessage!!, newMessage)
                            replyToMessage = null
                        } else {
                            viewModel.sendMessage(userId, newMessage)
                        }
                        newMessage = ""
                        isTyping = false
                    }
                },
                onImageSelected = { imageUri ->
                    viewModel.sendImageMessage(userId, imageUri)
                },
                textColor = textColor,
                surfaceColor = surfaceColor
            )

        }
    }

    showFullScreenImage?.let { imageUrl ->
        FullScreenImage(imageUrl = imageUrl, onDismiss = { showFullScreenImage = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    onBackClick: () -> Unit,
    userProfileUrl: String?,
    username: String?,
    textColor: Color
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircleAvatar(size = 40.dp, imageUrl = userProfileUrl)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = username ?: "Unknown",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    fontFamily = poppinsFontFamily,
                    color = textColor
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
            }
        },
        actions = {
            IconButton(onClick = { /* TODO: Handle more options */ }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "Gallery", tint = textColor)
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onImageSelected: (Uri) -> Unit,
    textColor: Color,
    surfaceColor: Color
) {
    // Galeri seçim işlemi için bir launcher oluşturuyoruz
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { onImageSelected(it) }
        }
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(surfaceColor, RoundedCornerShape(24.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { galleryLauncher.launch("image/*") }) {
            Icon(Icons.Rounded.Add, contentDescription = "Gallery", tint = textColor)
        }

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text("Message...", fontFamily = poppinsFontFamily, color = textColor.copy(alpha = 0.5f))
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontFamily = poppinsFontFamily)
        )

        IconButton(onClick = onSendClick) {
            Icon(Icons.Filled.Send, contentDescription = "Send", tint = textColor)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    message: Message,
    isOwnMessage: Boolean,
    ownUserProfileUrl: String?,
    otherUserProfileUrl: String?,
    showAvatar: Boolean,
    isFirstMessage: Boolean,
    bubbleColorOwn: Color,
    bubbleColorOther: Color,
    textColor: Color,
    onMessageLongPress: (Message) -> Unit,
    onImageClick: (String) -> Unit,
    onImageLongPress: (String) -> Unit,
    currentUserId: String,
    otherUsername: String?,
    messages: List<Message>,
    modifier: Modifier = Modifier
) {
    // Balonun şekli istek üzerine belirlenecek
    val bubbleShape = if (isOwnMessage) {
        if (isFirstMessage) {
            RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 5.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            )
        } else {
            RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            )
        }
    } else {
        if (isFirstMessage) {
            RoundedCornerShape(
                topStart = 5.dp,
                topEnd = 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            )
        } else {
            RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            )
        }
    }

    Column(
        horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = { onMessageLongPress(message) }
            )
            .heightIn(min = 48.dp)
    ) {
        // Eğer mesaj bir yanıtlama içeriyorsa
        message.replyToMessageId?.let { replyToMessageId ->
            val repliedMessage = messages.find { it.id == replyToMessageId }
            repliedMessage?.let {
                ReplyBubble(
                    repliedMessage = it,
                    currentUserId = currentUserId,
                    otherUsername = otherUsername,
                    textColor = textColor.copy(alpha = 0.8f),
                    bubbleColor = if (isOwnMessage) bubbleColorOwn else bubbleColorOther,
                    bubbleShape = bubbleShape,
                    avatarUrl = if (isOwnMessage) ownUserProfileUrl else otherUserProfileUrl,
                    isOwnMessage = isOwnMessage,
                    newMessage = message.content
                )
            }
        } ?: run {
            Row(
                horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
            ) {
                // Profil resmi gösterimi
                if (!isOwnMessage) {
                    Spacer(modifier = Modifier.width(if (showAvatar) 0.dp else 40.dp))
                    if (showAvatar) {
                        CircleAvatar(size = 32.dp, imageUrl = otherUserProfileUrl)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                // Mesaj içeriği
                Box(
                    modifier = Modifier
                        .background(
                            if (isOwnMessage) bubbleColorOwn else bubbleColorOther,
                            bubbleShape
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    // Eğer mesaj içeriği bir resim URL'si ise resim göster
                    if (message.content.startsWith("http") && message.content.contains("firebase")) {
                        AsyncImage(
                            model = message.content,
                            contentDescription = "Image Message",
                            modifier = Modifier
                                .size(150.dp) // Resim boyutu
                                .clip(RoundedCornerShape(8.dp))
                                .combinedClickable(
                                    onClick = { onImageClick(message.content) },
                                    onLongClick = { onImageLongPress(message.content) }
                                ),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Normal metin mesajı olarak göster
                        Text(
                            text = message.content,
                            color = if (isOwnMessage && isSystemInDarkTheme()) Color.Black else textColor,
                            fontSize = 14.sp,
                            fontFamily = poppinsFontFamily
                        )
                    }
                }

                // Kendi mesajınızın profil resmi
                if (isOwnMessage) {
                    Spacer(modifier = Modifier.width(if (showAvatar) 8.dp else 40.dp))
                    if (showAvatar) {
                        CircleAvatar(size = 32.dp, imageUrl = ownUserProfileUrl)
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplyBubble(
    repliedMessage: Message,
    currentUserId: String,
    otherUsername: String?,
    textColor: Color,
    bubbleColor: Color,
    bubbleShape: RoundedCornerShape,
    avatarUrl: String?,
    isOwnMessage: Boolean,
    newMessage: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        if (!isOwnMessage) {
            Spacer(modifier = Modifier.width(0.dp))
            CircleAvatar(size = 32.dp, imageUrl = avatarUrl)
            Spacer(modifier = Modifier.width(3.dp))
        }

        Column(
            horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(bubbleColor, bubbleShape)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .heightIn(min = 64.dp)
                    .fillMaxWidth(0.75f)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.05f), shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = if (repliedMessage.senderId == currentUserId) "Siz" else otherUsername
                                    ?: "Kullanıcı",
                                fontFamily = poppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                            Text(
                                text = repliedMessage.content,
                                fontFamily = poppinsFontFamily,
                                fontSize = 14.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    Text(
                        text = newMessage,
                        fontFamily = poppinsFontFamily,
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        if (isOwnMessage) {
            Spacer(modifier = Modifier.width(8.dp))
            CircleAvatar(size = 32.dp, imageUrl = avatarUrl)
        }
    }
}

@Composable
fun TypingIndicator(bubbleColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(bubbleColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                for (i in 1..3) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.Gray, CircleShape)
                            .padding(2.dp)
                    )
                    if (i < 3) Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}

@Composable
fun CircleAvatar(size: Dp, imageUrl: String?) {
    if (imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Avatar",
            modifier = Modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "Default Avatar",
            modifier = Modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}
