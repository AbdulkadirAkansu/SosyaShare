package com.akansu.sosyashare.presentation.comment.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.akansu.sosyashare.domain.model.CommentReplyInfo
import com.akansu.sosyashare.domain.model.CommentWithUserInfo
import com.akansu.sosyashare.presentation.comment.viewmodel.CommentViewModel
import com.akansu.sosyashare.presentation.comment.viewmodel.formatTimestampToRelativeTime

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CommentScreen(
    postId: String,
    currentUserId: String,
    currentUserProfileUrl: String,
    viewModel: CommentViewModel = hiltViewModel(),
    backgroundContent: @Composable () -> Unit
) {
    val comments by viewModel.comments.observeAsState(emptyList())
    var sheetOffset by remember { mutableStateOf(0f) }
    val screenHeight = with(LocalDensity.current) { 400.dp.toPx() }

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    BoxWithConstraints {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .clickable {
                    sheetOffset = screenHeight
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            sheetOffset = (sheetOffset + dragAmount.y).coerceIn(0f, screenHeight)
                        }
                    }
            ) {
                backgroundContent()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = sheetOffset.dp)
                        .padding(top = 40.dp)
                        .zIndex(1f)
                        .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                        .background(backgroundColor)
                        .pointerInput(Unit) {
                            detectDragGestures { _, dragAmount ->
                                sheetOffset = (sheetOffset + dragAmount.y).coerceIn(0f, screenHeight)
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .width(60.dp)
                            .height(4.dp)
                            .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 16.dp)
                    )

                    Text(
                        text = "Comments",
                        style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 16.dp)
                    )

                    Divider(color = Color.Gray.copy(alpha = 0.2f), thickness = 1.dp)

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(items = comments, key = { it.comment.id }) { commentWithUserInfo ->
                            CommentItem(
                                commentWithUserInfo = commentWithUserInfo,
                                postId = postId,
                                currentUserId = currentUserId,
                                onLikeClick = {
                                    viewModel.likeComment(commentWithUserInfo.comment.id, currentUserId, postId)
                                },
                                onUnlikeClick = {
                                    viewModel.unlikeComment(commentWithUserInfo.comment.id, currentUserId,postId)
                                },
                                onDeleteClick = {
                                    viewModel.deleteComment(commentWithUserInfo.comment.id, postId)
                                },
                                onReplyClick = { commentId, username ->
                                    viewModel.replyToComment(postId, commentId, username)
                                }
                            )
                        }
                    }

                    Divider(color = Color.Gray.copy(alpha = 0.2f))

                    CommentInputField(
                        onCommentAdded = { content ->
                            val replyInfo = viewModel.replyingTo.value
                            if (replyInfo != null) {
                                viewModel.sendReply(replyInfo.postId, replyInfo.commentId, content, currentUserId)
                            } else {
                                viewModel.addComment(postId, content, currentUserId)
                            }
                        },
                        replyingTo = viewModel.replyingTo.observeAsState(null).value,
                        currentUserProfileUrl = currentUserProfileUrl
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadComments(postId)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommentItem(
    commentWithUserInfo: CommentWithUserInfo,
    postId: String,
    currentUserId: String,
    onLikeClick: () -> Unit,
    onUnlikeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onReplyClick: (String, String) -> Unit
) {
    val comment = commentWithUserInfo.comment
    val username = commentWithUserInfo.username
    val userProfileUrl = commentWithUserInfo.userProfileUrl
    var showDeleteIcon by remember { mutableStateOf(false) }
    var liked by remember { mutableStateOf(comment.likes.contains(currentUserId)) }
    var likeCount by remember { mutableIntStateOf(comment.likes.size) }
    var showReplies by remember { mutableStateOf(false) }
    val repliesToShow = if (showReplies) commentWithUserInfo.replies else commentWithUserInfo.replies.take(1)

    val textColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .padding(start = if (comment.parentCommentId != null) 24.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { /* Normal click action */ },
                    onLongClick = {
                        showDeleteIcon = !showDeleteIcon
                    }
                ),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = userProfileUrl,
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = username,
                        style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = comment.content,
                        style = MaterialTheme.typography.bodyMedium.copy(color = textColor)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTimestampToRelativeTime(comment.timestamp),
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Reply",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color.Gray),
                        modifier = Modifier.clickable { onReplyClick(comment.id, username) }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }

            IconButton(
                onClick = {
                    if (liked) {
                        onUnlikeClick()
                        liked = false
                        likeCount--
                    } else {
                        onLikeClick()
                        liked = true
                        likeCount++
                    }
                }
            ) {
                Icon(
                    imageVector = if (liked) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (liked) Color.Red else Color.White
                )
            }

            if (likeCount > 0) {
                Text(
                    text = likeCount.toString(),
                    style = MaterialTheme.typography.bodySmall.copy(color = textColor),
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            if (showDeleteIcon) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }
        }

        if (commentWithUserInfo.replies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (showReplies) "Hide replies" else "View ${commentWithUserInfo.replies.size} more ${if (commentWithUserInfo.replies.size == 1) "reply" else "replies"}",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                modifier = Modifier
                    .clickable { showReplies = !showReplies }
                    .padding(start = 44.dp)
            )

            if (showReplies) {
                repliesToShow.forEach { reply ->
                    Spacer(modifier = Modifier.height(8.dp))
                    CommentItem(
                        commentWithUserInfo = reply,
                        postId = postId,
                        currentUserId = currentUserId,
                        onLikeClick = {
                            onLikeClick()
                        },
                        onUnlikeClick = {
                            onUnlikeClick()
                        },
                        onDeleteClick = {
                            onDeleteClick()
                        },
                        onReplyClick = { replyId, username ->
                            onReplyClick(replyId, username)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentInputField(
    onCommentAdded: (String) -> Unit,
    replyingTo: CommentReplyInfo?,
    currentUserProfileUrl: String
) {
    var commentText by remember { mutableStateOf("") }

    val textColor = MaterialTheme.colorScheme.onBackground

    LaunchedEffect(replyingTo) {
        commentText = if (replyingTo != null) "@${replyingTo.username} " else ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = currentUserProfileUrl,
            contentDescription = "Your Profile Picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )

        TextField(
            value = commentText,
            onValueChange = { commentText = it },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            placeholder = { Text("Add a comment...", color = Color.Gray) },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                cursorColor = textColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        if (commentText.isNotBlank()) {
            TextButton(
                onClick = {
                    onCommentAdded(commentText.trim())
                    commentText = ""
                }
            ) {
                Text("Comment", color = Color(0xFF3897F0))
            }
        }
    }
}
