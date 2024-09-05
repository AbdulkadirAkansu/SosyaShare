package com.akansu.sosyashare.presentation.message.screen

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.akansu.sosyashare.R
import com.akansu.sosyashare.domain.model.Message
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.presentation.message.viewmodel.ChatColors
import com.akansu.sosyashare.presentation.message.viewmodel.ChatViewModel
import com.akansu.sosyashare.presentation.message.viewmodel.getChatColors
import com.akansu.sosyashare.presentation.postdetail.screen.FullScreenImage
import com.akansu.sosyashare.util.poppinsFontFamily
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun ChatScreen(
    navController: NavHostController,
    userId: String,
    viewModel: ChatViewModel = hiltViewModel(),
    forwardedMessage: String? = null
) {
    val (messages, otherUser, currentUser, newMessageState, isTypingState, selectedMessageState, isMenuVisibleState, replyToMessageState, showFullScreenImageState) = rememberChatScreenStates(viewModel)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var menuPosition by remember { mutableStateOf(IntOffset(0, 0)) }  // Menü pozisyonu için state


    Log.d("ChatScreen", "Chat loaded for userId: $userId")

    LaunchedEffect(userId) { viewModel.listenForMessages(userId) }

    LaunchedEffect(forwardedMessage) {
        forwardedMessage?.let {
            viewModel.sendMessage(userId, it)
        }
    }
    ChatScaffold(
        navController = navController,
        messages = messages,
        otherUser = otherUser,
        currentUser = currentUser,
        newMessage = newMessageState,
        isTyping = isTypingState,
        replyToMessage = replyToMessageState,
        selectedMessage = selectedMessageState,
        isMenuVisible = isMenuVisibleState,
        showFullScreenImage = showFullScreenImageState,
        onBackClick = { navController.popBackStack() },
        onMessageLongPress = { message, offset ->
            selectedMessageState.value = message  // Mesajı kaydet
            isMenuVisibleState.value = true  // Menü aç
            menuPosition = IntOffset(offset.x.toInt(), offset.y.toInt())
        },
        onSendClick = { message -> handleSendMessage(message, viewModel, userId, replyToMessageState, newMessageState) },
        onImageSelected = { uri -> viewModel.sendImageMessage(userId, uri) },
        onDismissFullScreenImage = { showFullScreenImageState.value = null },
        coroutineScope = coroutineScope,
        context = context
    )

}

@Composable
fun rememberChatScreenStates(viewModel: ChatViewModel): ChatScreenStates {
    val messages by viewModel.messages.collectAsState()
    val otherUser by viewModel.otherUser.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val newMessageState = remember { mutableStateOf("") }
    val isTypingState = remember { mutableStateOf(false) }
    val selectedMessageState = remember { mutableStateOf<Message?>(null) }
    val isMenuVisibleState = remember { mutableStateOf(false) }
    val replyToMessageState = remember { mutableStateOf<Message?>(null) }
    val showFullScreenImageState = remember { mutableStateOf<String?>(null) }

    return ChatScreenStates(
        messages, otherUser, currentUser, newMessageState,
        isTypingState, selectedMessageState, isMenuVisibleState,
        replyToMessageState, showFullScreenImageState
    )
}

data class ChatScreenStates(
    val messages: List<Message>,
    val otherUser: User?,
    val currentUser: User?,
    val newMessage: MutableState<String>,
    val isTyping: MutableState<Boolean>,
    val selectedMessage: MutableState<Message?>,
    val isMenuVisible: MutableState<Boolean>,
    val replyToMessage: MutableState<Message?>,
    val showFullScreenImage: MutableState<String?>
)

fun handleSendMessage(
    message: String,
    viewModel: ChatViewModel,
    userId: String,
    replyToMessageState: MutableState<Message?>,
    newMessageState: MutableState<String> // Buraya `newMessageState` ekledik
) {
    if (message.isNotBlank()) {
        if (replyToMessageState.value != null) {
            viewModel.replyToMessage(userId, replyToMessageState.value!!, message)
            replyToMessageState.value = null
        } else {
            viewModel.sendMessage(userId, message)
        }
        newMessageState.value = ""
    }
}


@Composable
fun ChatScaffold(
    navController: NavHostController,
    messages: List<Message>,
    otherUser: User?,
    currentUser: User?,
    newMessage: MutableState<String>,
    isTyping: MutableState<Boolean>,
    replyToMessage: MutableState<Message?>,
    selectedMessage: MutableState<Message?>,
    isMenuVisible: MutableState<Boolean>,
    showFullScreenImage: MutableState<String?>,
    onBackClick: () -> Unit,
    onMessageLongPress: (Message, Offset) -> Unit,
    onSendClick: (String) -> Unit,
    onImageSelected: (Uri) -> Unit,
    onDismissFullScreenImage: () -> Unit,
    coroutineScope: CoroutineScope,
    context: Context,
    viewModel: ChatViewModel = hiltViewModel()  // viewModel burada tanımlanıyor
) {
    val colors = getChatColors(isDarkTheme = isSystemInDarkTheme())
    var menuPosition by remember { mutableStateOf(IntOffset(0, 0)) }

    Scaffold(
        topBar = {
            ChatTopBar(
                onBackClick = onBackClick,
                userProfileUrl = otherUser?.profilePictureUrl,
                username = otherUser?.username,
                textColor = colors.textColor
            )
        },
        containerColor = colors.backgroundColor
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(colors.surfaceColor, RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                ChatMessageList(
                    messages = messages,
                    currentUser = currentUser,
                    otherUser = otherUser,
                    isTyping = isTyping.value,
                    bubbleColorOwn = colors.bubbleColorOwn,
                    bubbleColorOther = colors.bubbleColorOther,
                    textColor = colors.textColor,
                    onMessageLongPress = { message, offset ->
                        Log.d("ChatScreen", "Message selected: ${message.content}")
                        selectedMessage.value = message
                        isMenuVisible.value = true
                    },
                    showFullScreenImageState = showFullScreenImage
                )

                if (isMenuVisible.value && selectedMessage.value != null) {
                    ModernMessageOptionsDialog(
                        selectedMessage = selectedMessage.value!!,
                        onDismissRequest = { isMenuVisible.value = false },
                        onReplyClick = {
                            replyToMessage.value = selectedMessage.value
                            isMenuVisible.value = false
                        },
                        onDeleteClick = {
                            viewModel.deleteMessage(selectedMessage.value!!.id)
                            isMenuVisible.value = false
                        },
                        onForwardClick = {
                            val encodedContent = Uri.encode(selectedMessage.value!!.content)
                            navController.navigate("new_message_screen/$encodedContent") {
                                popUpTo("new_message_screen") { inclusive = true }
                            }
                            isMenuVisible.value = false
                        },
                        onSaveImageToGallery = {
                            coroutineScope.launch {
                                viewModel.saveImageToGallery(selectedMessage.value!!.content, context)
                            }
                        }, // Galeriye kaydetme fonksiyonu eklendi
                        colors = colors
                    )
                }
            }

            replyToMessage.value?.let {
                ReplyBox(it, currentUser, otherUser, onCancelReply = { replyToMessage.value = null }, colors.textColor)
            }

            ChatInputField(
                value = newMessage.value,
                onValueChange = { newMessage.value = it; isTyping.value = it.isNotEmpty() },
                onSendClick = { onSendClick(newMessage.value) },
                onImageSelected = onImageSelected,
                textColor = colors.textColor,
                surfaceColor = colors.surfaceColor
            )
        }
    }

    showFullScreenImage.value?.let {
        FullScreenImage(imageUrl = it, onDismiss = onDismissFullScreenImage)
    }
}

@Composable
fun ModernMessageOptionsDialog(
    selectedMessage: Message,
    onDismissRequest: () -> Unit,
    onReplyClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onForwardClick: () -> Unit,
    onSaveImageToGallery: () -> Unit, // Galeriye kaydetme fonksiyonu
    colors: ChatColors,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSystemInDarkTheme()) Color.DarkGray.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f)

    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(backgroundColor, shape = RoundedCornerShape(16.dp))  // Saydam arka plan
                .padding(16.dp)
        ) {
            Column {
                DropdownMenuItem(
                    onClick = onReplyClick,
                    text = { Text(text = "Yanıtla", color = colors.textColor) }
                )
                DropdownMenuItem(
                    onClick = onForwardClick,
                    text = { Text(text = "İlet", color = colors.textColor) }
                )
                // Eğer mesaj bir resimse Galeriye Kaydet seçeneğini ekliyoruz
                if (selectedMessage.content.startsWith("http") && selectedMessage.content.contains("firebase")) {
                    DropdownMenuItem(
                        onClick = {
                            onSaveImageToGallery()  // Galeriye kaydetme fonksiyonu çağrılıyor
                            onDismissRequest() // Menü kapatılıyor
                        },
                        text = { Text(text = "Galeriye Kaydet", color = colors.textColor) }
                    )
                }
                DropdownMenuItem(
                    onClick = onDeleteClick,
                    text = { Text(text = "Gönderimi Sil", color = colors.textColor) }
                )
            }
        }
    }
}

@Composable
fun MessageOptionItem(text: String, onClick: () -> Unit, textColor: Color) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        color = textColor,
        fontSize = 16.sp
    )
}


@Composable
fun ChatMessageList(
    messages: List<Message>,
    currentUser: User?,
    otherUser: User?,
    isTyping: Boolean,
    bubbleColorOwn: Color,
    bubbleColorOther: Color,
    textColor: Color,
    onMessageLongPress: (Message, Offset) -> Unit,
    showFullScreenImageState: MutableState<String?>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        var lastSenderId: String? = null
        var lastMessageTimestamp: Long? = null

        items(messages) { message ->
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
                        fontSize = 12.sp
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
                onMessageLongPress = onMessageLongPress,
                onImageClick = { imageUrl ->
                    showFullScreenImageState.value = imageUrl
                },
                onImageLongPress = { /* Uzun basma işlemi için gerekli implementasyon */ },
                currentUserId = currentUser?.id ?: "",
                otherUsername = otherUser?.username,
                messages = messages
            )

            lastSenderId = message.senderId
        }

        if (isTyping) {
            item {
                TypingIndicator(bubbleColorOther)
            }
        }
    }
}

@Composable
fun ReplyBox(
    replyToMessage: Message,
    currentUser: User?,
    otherUser: User?,
    onCancelReply: () -> Unit,
    textColor: Color
) {
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
                    text = if (replyToMessage.senderId == currentUser?.id) "Siz" else otherUser?.username ?: "Kullanıcı",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = textColor
                )
                Text(
                    text = replyToMessage.content,
                    fontFamily = poppinsFontFamily,
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.8f)
                )
            }
            IconButton(onClick = onCancelReply) {
                Icon(Icons.Default.Close, contentDescription = "İptal Et")
            }
        }
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
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
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = textColor)
        }
    }
}

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
    onMessageLongPress: (Message, Offset) -> Unit,  // Mesaj ve pozisyon
    onImageClick: (String) -> Unit,
    onImageLongPress: (String) -> Unit,
    currentUserId: String,
    otherUsername: String?,
    messages: List<Message>,
) {
    val bubbleShape = BubbleShape(isOwnMessage, isFirstMessage)

    Column(
        horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { offset ->
                        onMessageLongPress(message, offset)  // Pozisyonu da geçiyoruz
                    }
                )
            }
            .heightIn(min = 48.dp)
    ) {
        ReplySection(
            message = message,
            messages = messages,
            isOwnMessage = isOwnMessage,
            currentUserId = currentUserId,
            otherUsername = otherUsername,
            bubbleColorOwn = bubbleColorOwn,
            bubbleColorOther = bubbleColorOther,
            textColor = textColor,
            bubbleShape = bubbleShape,
            ownUserProfileUrl = ownUserProfileUrl,
            otherUserProfileUrl = otherUserProfileUrl
        )

        // Eğer yanıtlanan bir mesaj varsa, sadece yanıt gösterilsin, mesaj içeriği gösterilmesin
        if (message.replyToMessageId == null) {
            MessageContent(
                message = message,
                isOwnMessage = isOwnMessage,
                bubbleColorOwn = bubbleColorOwn,
                bubbleColorOther = bubbleColorOther,
                bubbleShape = bubbleShape,
                textColor = textColor,
                onImageClick = onImageClick,
                onImageLongPress = onImageLongPress,
                showAvatar = showAvatar,
                ownUserProfileUrl = ownUserProfileUrl,
                otherUserProfileUrl = otherUserProfileUrl
            )
        }
    }
}

@Composable
fun BubbleShape(isOwnMessage: Boolean, isFirstMessage: Boolean): RoundedCornerShape {
    return if (isOwnMessage) {
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
}

@Composable
fun ReplySection(
    message: Message,
    messages: List<Message>,
    isOwnMessage: Boolean,
    currentUserId: String,
    otherUsername: String?,
    bubbleColorOwn: Color,
    bubbleColorOther: Color,
    textColor: Color,
    bubbleShape: RoundedCornerShape,
    ownUserProfileUrl: String?,
    otherUserProfileUrl: String?
) {
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
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageContent(
    message: Message,
    isOwnMessage: Boolean,
    bubbleColorOwn: Color,
    bubbleColorOther: Color,
    bubbleShape: RoundedCornerShape,
    textColor: Color,
    onImageClick: (String) -> Unit,
    onImageLongPress: (String) -> Unit,
    showAvatar: Boolean,
    ownUserProfileUrl: String?,
    otherUserProfileUrl: String?
) {
    Row(
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        if (!isOwnMessage) {
            Spacer(modifier = Modifier.width(if (showAvatar) 0.dp else 40.dp))
            if (showAvatar) {
                CircleAvatar(size = 32.dp, imageUrl = otherUserProfileUrl)
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        Box(
            modifier = Modifier
                .background(
                    if (isOwnMessage) bubbleColorOwn else bubbleColorOther,
                    bubbleShape
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            if (message.content.startsWith("http") && message.content.contains("firebase")) {
                // Resime uzun basıldığında menü açılmasını, kısa tıklamada tam ekran açılmasını sağlıyoruz
                AsyncImage(
                    model = message.content,
                    contentDescription = "Image Message",
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .combinedClickable(
                            onClick = { onImageClick(message.content) },  // Resmi tam ekran göster
                            onLongClick = { onImageLongPress(message.content) }  // Uzun basıldığında menüyü aç
                        ),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = message.content,
                    color = if (isOwnMessage && isSystemInDarkTheme()) Color.Black else textColor,
                    fontSize = 14.sp,
                    fontFamily = poppinsFontFamily
                )
            }
        }

        if (isOwnMessage) {
            Spacer(modifier = Modifier.width(if (showAvatar) 8.dp else 40.dp))
            if (showAvatar) {
                CircleAvatar(size = 32.dp, imageUrl = ownUserProfileUrl)
            }
        }
    }
}


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
    // Sistemin dark mode olup olmadığını kontrol ediyoruz
    val isDarkTheme = isSystemInDarkTheme()

    // Diğer kullanıcının metni dark temada beyaz olacak
    val adjustedTextColor = if (!isOwnMessage && isDarkTheme) Color.White else textColor

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
                    // Bu Box, reply edilen mesajın içeriğini gösteriyor
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.05f), shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = if (repliedMessage.senderId == currentUserId) "Siz" else otherUsername ?: "Kullanıcı",
                                fontFamily = poppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                // Diğer kullanıcının dark modda text rengi beyaz olacak
                                color = if (!isOwnMessage && isDarkTheme) Color.White else Color.Black
                            )
                            Text(
                                text = repliedMessage.content,
                                fontFamily = poppinsFontFamily,
                                fontSize = 14.sp,
                                // Diğer kullanıcının dark modda text rengi beyaz olacak
                                color = if (!isOwnMessage && isDarkTheme) Color.White else Color.Black,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    Text(
                        text = newMessage,
                        fontFamily = poppinsFontFamily,
                        fontSize = 14.sp,
                        color = if (isOwnMessage && isDarkTheme) Color.Black else adjustedTextColor,
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
