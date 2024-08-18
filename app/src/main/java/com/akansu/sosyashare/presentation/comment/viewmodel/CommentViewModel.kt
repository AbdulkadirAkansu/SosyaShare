package com.akansu.sosyashare.presentation.comment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.Comment
import com.akansu.sosyashare.domain.model.Reply
import com.akansu.sosyashare.domain.repository.CommentRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> get() = _comments

    private val _replies = MutableLiveData<Map<String, List<Reply>>>()
    val replies: LiveData<Map<String, List<Reply>>> get() = _replies

    private val _replyingTo = MutableLiveData<Reply?>()
    val replyingTo: LiveData<Reply?> get() = _replyingTo

    private var currentUserName: String? = null

    init {
        viewModelScope.launch {
            currentUserName = userRepository.getCurrentUserName()
        }
    }

    fun loadComments(postId: String) {
        viewModelScope.launch {
            val comments = commentRepository.getCommentsForPost(postId)
            _comments.value = comments

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
        userProfileUrl: String
    ) {
        viewModelScope.launch {
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
            loadComments(postId)
        }
    }

    fun deleteComment(commentId: String, postId: String) {
        viewModelScope.launch {
            try {
                // Ana yorum ve ona bağlı tüm yanıtları tek seferde sil
                commentRepository.deleteCommentWithReplies(commentId)
                loadComments(postId)  // Yorumları yeniden yükle
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
            loadComments(commentRepository.getCommentById(commentId)?.postId ?: return@launch)
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
