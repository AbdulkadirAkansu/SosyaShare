package com.akansu.sosyashare.presentation.comment.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.data.remote.FirebaseMessagingService
import com.akansu.sosyashare.domain.model.Comment
import com.akansu.sosyashare.domain.model.Reply
import com.akansu.sosyashare.domain.repository.CommentRepository
import com.akansu.sosyashare.domain.repository.MessagingRepository
import com.akansu.sosyashare.domain.repository.NotificationRepository
import com.akansu.sosyashare.domain.repository.PostRepository
import com.akansu.sosyashare.domain.usecase.UpdateCommentCountUseCase
import com.akansu.sosyashare.domain.repository.UserRepository
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val updateCommentCountUseCase: UpdateCommentCountUseCase,
    private val notificationRepository: NotificationRepository,
    private val postRepository: PostRepository,
    private val messagingRepository: MessagingRepository
) : ViewModel() {

    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> get() = _comments

    private val _replies = MutableLiveData<Map<String, List<Reply>>>()
    val replies: LiveData<Map<String, List<Reply>>> get() = _replies

    private val _replyingTo = MutableLiveData<Reply?>()
    val replyingTo: LiveData<Reply?> get() = _replyingTo

    private var currentUserName: String? = null
    private var postOwnerId: String? = null

    init {
        viewModelScope.launch {
            currentUserName = userRepository.getCurrentUserName()
        }
    }


    fun loadComments(postId: String) {
        viewModelScope.launch {
            val comments = commentRepository.getCommentsForPost(postId)
            _comments.value = comments

            // Post sahibinin userId'sini alıyoruz
            val post = postRepository.getPostById(postId)
            postOwnerId = post?.userId

            val repliesMap = comments.associate { comment ->
                comment.id to commentRepository.getRepliesForComment(comment.id)
            }
            _replies.value = repliesMap
        }
    }

    fun addComment(
        postId: String,
        content: String,
        userId: String,
        username: String,
        userProfileUrl: String,
        context: Context
    ) {
        viewModelScope.launch {
            val canComment = notificationRepository.canUserLikeOrComment(userId, postId, "comment")

            if (canComment) {
                val newComment = Comment(
                    id = UUID.randomUUID().toString(),
                    postId = postId,
                    userId = userId,
                    username = username,
                    userProfileUrl = userProfileUrl,
                    content = content,
                    timestamp = Date(),
                    likes = mutableListOf()
                )
                commentRepository.addComment(newComment)
                updateCommentCountUseCase(postId)
                loadComments(postId)

                postOwnerId?.let { ownerId ->
                    // Uygulama içi bildirim gönderme
                    notificationRepository.sendNotification(
                        ownerId,
                        postId,
                        userId,
                        username,
                        userProfileUrl,
                        notificationType = "comment"
                    )
                    val fcmToken = messagingRepository.getFCMTokenByUserId(ownerId)

                    if (fcmToken != null) {
                        Log.d("CommentViewModel", "Post sahibi FCM Token: $fcmToken")
                        messagingRepository.sendFCMNotification(context, fcmToken, "$username commented", content)
                    } else {
                        Log.e("CommentViewModel", "Kullanıcının FCM token'ı bulunamadı.")
                    }
                }
            }
        }
    }



    fun deleteComment(commentId: String, postId: String) {
        viewModelScope.launch {
            try {
                commentRepository.deleteCommentWithReplies(commentId)
                updateCommentCountUseCase(postId)
                loadComments(postId)
            } catch (e: Exception) {
                // Hata durumunda loglama yapabilirsiniz
            }
        }
    }

    fun deleteReply(replyId: String, postId: String) {
        viewModelScope.launch {
            try {
                // Yalnızca yanıtı sil
                commentRepository.deleteReply(replyId)
                loadComments(postId)
            } catch (e: Exception) {
                // Hata durumunda loglama yapabilirsiniz
            }
        }
    }

    fun likeComment(commentId: String, userId: String) {
        viewModelScope.launch {
            commentRepository.likeComment(commentId, userId)
            val postId = commentRepository.getCommentById(commentId)?.postId ?: return@launch
            val commentOwnerId = commentRepository.getCommentById(commentId)?.userId ?: return@launch
            loadComments(postId)

            notificationRepository.sendNotification(
                commentOwnerId,
                postId,
                userId,
                currentUserName ?: "Anonymous",
                null,
                notificationType = "like"
            )
        }
    }

    fun unlikeComment(commentId: String, userId: String) {
        viewModelScope.launch {
            commentRepository.unlikeComment(commentId, userId)
            loadComments(commentRepository.getCommentById(commentId)?.postId ?: return@launch)
        }
    }

    fun replyToComment(
        commentId: String,
        content: String,
        userId: String,
        username: String,
        userProfileUrl: String
    ) {
        viewModelScope.launch {
            val reply = Reply(
                id = UUID.randomUUID().toString(),
                commentId = commentId,
                userId = userId,
                username = username,
                userProfileUrl = userProfileUrl,
                content = content,
                timestamp = Date()
            )
            commentRepository.addReply(reply)
            loadComments(commentRepository.getCommentById(commentId)?.postId ?: return@launch)
        }
    }

    fun setReplyingTo(reply: Reply?) {
        _replyingTo.value = reply
    }
}
