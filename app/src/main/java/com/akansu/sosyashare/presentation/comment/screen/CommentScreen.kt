package com.akansu.sosyashare.presentation.comment.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.*
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
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.akansu.sosyashare.domain.model.BaseComment
import com.akansu.sosyashare.domain.model.Comment
import com.akansu.sosyashare.domain.model.Reply
import com.akansu.sosyashare.presentation.comment.viewmodel.CommentViewModel
import com.akansu.sosyashare.presentation.comment.viewmodel.formatTimestampToRelativeTime
import java.util.Date

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CommentScreen(
    postId: String,
    currentUserId: String,
    currentUserName: String,
    currentUserProfileUrl: String,
    viewModel: CommentViewModel = hiltViewModel(),
    backgroundContent: @Composable () -> Unit
) {
    val comments by viewModel.comments.observeAsState(emptyList())
    val repliesMap by viewModel.replies.observeAsState(emptyMap())

    BoxWithConstraints {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            backgroundContent()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)
                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = "Comments",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(items = comments, key = { it.id }) { comment ->
                        var showReplies by remember { mutableStateOf(false) }

                        CommentItem(
                            comment = comment,
                            currentUserId = currentUserId,
                            replies = repliesMap[comment.id] ?: emptyList(),
                            showReplies = showReplies,
                            onToggleReplies = { showReplies = !showReplies },
                            onLikeClick = { viewModel.likeComment(comment.id, currentUserId) },
                            onUnlikeClick = { viewModel.unlikeComment(comment.id, currentUserId) },
                            onDeleteClick = { viewModel.deleteComment(comment.id, postId) },
                            onReplyClick = { commentId, username ->
                                viewModel.setReplyingTo(
                                    Reply(
                                        id = "",
                                        commentId = commentId,
                                        userId = currentUserId,
                                        username = currentUserName,
                                        userProfileUrl = currentUserProfileUrl,
                                        content = "",
                                        timestamp = Date()
                                    )
                                )
                            },
                            onReplyDeleteClick = { replyId ->
                                viewModel.deleteReply(replyId, postId)
                            }
                        )
                    }
                }

                CommentInputField(
                    onCommentAdded = { content ->
                        val replyInfo = viewModel.replyingTo.value
                        if (replyInfo != null) {
                            viewModel.replyToComment(replyInfo.commentId, content, currentUserId, currentUserName, currentUserProfileUrl)
                        } else {
                            viewModel.addComment(postId, content, currentUserId, currentUserName, currentUserProfileUrl)
                        }
                    },
                    replyingTo = viewModel.replyingTo.observeAsState(null).value,
                    currentUserProfileUrl = currentUserProfileUrl
                )


            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadComments(postId)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CommentItem(
    comment: BaseComment,
    currentUserId: String,
    replies: List<Reply>,
    showReplies: Boolean,
    onToggleReplies: () -> Unit,
    onLikeClick: () -> Unit,
    onUnlikeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onReplyClick: (String, String) -> Unit,
    onReplyDeleteClick: (String) -> Unit
) {
    val isReply = comment is Reply
    val paddingStart = if (isReply) 24.dp else 0.dp
    var showDeleteIcon by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .padding(start = paddingStart)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { /* Boş bırak */ },
                    onLongClick = { showDeleteIcon = !showDeleteIcon } // Silme ikonunu gösterme
                ),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = comment.userProfileUrl,
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = comment.username,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = comment.content,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTimestampToRelativeTime(comment.timestamp),
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    if (!isReply) {
                        Text(
                            text = "Reply",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.clickable { onReplyClick(comment.id, comment.username) }
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }

            // Silme ikonu
            if (showDeleteIcon) {
                IconButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteIcon = false // İkonu sakla
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        if (comment.likes.contains(currentUserId)) {
                            onUnlikeClick()
                        } else {
                            onLikeClick()
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (comment.likes.contains(currentUserId)) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (comment.likes.contains(currentUserId)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Yanıtlar için dinamik görünüm
        if (replies.isNotEmpty()) {
            Text(
                text = if (showReplies) "Hide replies" else "View ${replies.size} more replies",
                modifier = Modifier.clickable { onToggleReplies() },
                color = MaterialTheme.colorScheme.primary
            )

            if (showReplies) {
                replies.forEach { reply ->
                    CommentItem(
                        comment = reply,
                        currentUserId = currentUserId,
                        replies = emptyList(), // Yanıtın yanıtları olmadığı için boş liste
                        showReplies = false,
                        onToggleReplies = {},
                        onLikeClick = { onLikeClick() },
                        onUnlikeClick = { onUnlikeClick() },
                        onDeleteClick = { onReplyDeleteClick(reply.id) },
                        onReplyClick = onReplyClick,
                        onReplyDeleteClick = onReplyDeleteClick
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
    replyingTo: Reply?,
    currentUserProfileUrl: String
) {
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(replyingTo) {
        commentText = if (replyingTo != null) "@${replyingTo.username} " else ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
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
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
        )

        TextField(
            value = commentText,
            onValueChange = { commentText = it },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            placeholder = { Text("Add a comment...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.onBackground,
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
                Text("Comment", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

fun Reply.toComment(postId: String): Comment {
    return Comment(
        id = this.id,
        postId = postId,
        userId = this.userId,
        username = this.username,
        userProfileUrl = this.userProfileUrl,
        content = this.content,
        timestamp = this.timestamp,
        likes = this.likes
    )
}
