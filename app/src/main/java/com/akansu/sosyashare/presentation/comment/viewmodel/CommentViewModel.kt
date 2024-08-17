package com.akansu.sosyashare.presentation.comment.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.Comment
import com.akansu.sosyashare.domain.model.CommentReplyInfo
import com.akansu.sosyashare.domain.model.CommentWithUserInfo
import com.akansu.sosyashare.domain.repository.CommentRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _comments = MutableLiveData<List<CommentWithUserInfo>>()
    val comments: LiveData<List<CommentWithUserInfo>> get() = _comments

    private val _replyingTo = MutableLiveData<CommentReplyInfo?>()
    val replyingTo: LiveData<CommentReplyInfo?> get() = _replyingTo


    fun loadComments(postId: String) {
        viewModelScope.launch {
            _comments.value = emptyList()

            val comments = commentRepository.getCommentsForPost(postId)
            val commentsWithUserInfo = comments.mapNotNull { comment ->
                val user = userRepository.getUserById(comment.userId).firstOrNull()
                user?.let {
                    CommentWithUserInfo(
                        comment = comment,
                        username = it.username,
                        userProfileUrl = it.profilePictureUrl ?: "",
                        replies = emptyList() // Yanıtlar ayrı olarak yüklenecek
                    )
                }
            }

            // Yanıtları ana yorumlara ekle
            val groupedComments = commentsWithUserInfo.groupBy { it.comment.parentCommentId }
            val topLevelComments = groupedComments[null] ?: emptyList()
            val finalCommentsWithReplies = topLevelComments.map { parentComment ->
                parentComment.copy(
                    replies = groupedComments[parentComment.comment.id] ?: emptyList()
                )
            }

            _comments.value = finalCommentsWithReplies
            Log.d("CommentViewModel", "Distinct Comments Loaded: ${_comments.value}")
        }
    }

    fun addComment(postId: String, content: String, userId: String, parentCommentId: String? = null) {
        viewModelScope.launch {
            val newComment = Comment(
                id = UUID.randomUUID().toString(),
                postId = postId,
                userId = userId,
                content = content,
                timestamp = Date(),
                likes = mutableListOf(), // Boş MutableList başlat
                parentCommentId = parentCommentId,
                replies = mutableListOf() // Boş MutableList başlat
            )

            commentRepository.addComment(newComment)
            loadComments(postId)
        }
    }

    fun deleteComment(commentId: String, postId: String) {
        viewModelScope.launch {
            commentRepository.deleteComment(commentId)
            loadComments(postId)
        }
    }

    fun likeComment(commentId: String, userId: String, postId: String) {
        viewModelScope.launch {
            commentRepository.likeComment(commentId, userId)
            loadComments(postId)
        }
    }

    fun unlikeComment(commentId: String, userId: String, postId: String) {
        viewModelScope.launch {
            commentRepository.unlikeComment(commentId, userId)
            loadComments(postId)
        }
    }

    fun replyToComment(postId: String, commentId: String, username: String) {
        _replyingTo.value = CommentReplyInfo(postId, commentId, username)
    }

    fun sendReply(postId: String, parentCommentId: String, content: String, userId: String) {
        viewModelScope.launch {
            val replyComment = Comment(
                id = UUID.randomUUID().toString(),
                postId = postId,
                userId = userId,
                content = content,
                timestamp = Date(),
                likes = mutableListOf(), // Boş MutableList başlat
                parentCommentId = parentCommentId,
                replies = mutableListOf() // Boş MutableList başlat
            )

            commentRepository.addComment(replyComment)
            _replyingTo.value = null
            loadComments(postId)
        }
    }
}
