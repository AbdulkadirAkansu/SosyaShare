package com.akansu.sosyashare.presentation.postdetail.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.repository.NotificationRepository
import com.akansu.sosyashare.domain.repository.PostRepository
import com.akansu.sosyashare.domain.repository.SaveRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val saveRepository: SaveRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _profilePictureUrl = MutableStateFlow<String?>(null)
    val profilePictureUrl: StateFlow<String?> = _profilePictureUrl

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> get() = _currentUserId

    private val _savedPosts = MutableStateFlow<List<Post>>(emptyList())
    val savedPosts: StateFlow<List<Post>> = _savedPosts

    private val _likedUsers = MutableStateFlow<List<User>>(emptyList())
    val likedUsers: StateFlow<List<User>> = _likedUsers

    init {
        loadCurrentUserId()
        loadSavedPosts()
    }

    private fun loadCurrentUserId() {
        viewModelScope.launch {
            _currentUserId.value = userRepository.getCurrentUserId()
        }
    }

    fun loadLikedUsers(postId: String) {
        viewModelScope.launch {
            val likedUserIds = postRepository.getLikedUserIds(postId)
            val users = likedUserIds.mapNotNull { userId ->
                userRepository.getUserById(userId).firstOrNull()
            }
            _likedUsers.value = users
        }
    }

    fun loadUserDetails(userId: String) {
        viewModelScope.launch {
            try {
                val userDetails = userRepository.getUserById(userId).firstOrNull()
                _user.value = userDetails
                _profilePictureUrl.value = userDetails?.profilePictureUrl

                val userPosts = postRepository.getPostsByUser(userId).firstOrNull() ?: emptyList()
                _posts.value = userPosts.map { post ->
                    val isLiked = post.likedBy.contains(_currentUserId.value)
                    post.copy(isLiked = isLiked)
                }

                Log.d("PostDetailViewModel", "User details loaded: $userDetails")
                Log.d("PostDetailViewModel", "Posts loaded: $userPosts")
            } catch (e: Exception) {
                Log.e("PostDetailViewModel", "Error loading user details", e)
            }
        }
    }

    fun savePost(postId: String) {
        viewModelScope.launch {
            if (!isPostAlreadySaved(postId)) {
                saveRepository.savePost(postId)
                loadSavedPosts()
            }
        }
    }

    fun removeSavedPost(postId: String) {
        viewModelScope.launch {
            saveRepository.removeSavedPost(postId)
            loadSavedPosts()
        }
    }

    fun loadSavedPosts() {
        viewModelScope.launch {
            val saves = saveRepository.getSavedPosts()
            val posts = saves.mapNotNull { save ->
                postRepository.getPostById(save.postId) // Her bir Save için ilgili Post'u getir
            }
            _savedPosts.value = posts
            Log.d("PostDetailViewModel", "Updated _savedPosts with ${posts.size} posts")
        }
    }

    fun likePost(postId: String, postOwnerId: String) {
        viewModelScope.launch {
            val currentUserId = _currentUserId.value ?: return@launch
            postRepository.likePost(postId, currentUserId)

            // Beğeni sonrası bildirim gönder
            val currentUser = userRepository.getUserById(currentUserId).firstOrNull()
            currentUser?.let { user ->
                notificationRepository.sendNotification(
                    userId = postOwnerId, // Gönderinin sahibi
                    postId = postId,      // Beğenilen post ID'si
                    senderId = currentUserId,
                    senderUsername = user.username,
                    senderProfileUrl = user.profilePictureUrl,
                    notificationType = "like" // Bildirim türü
                )
            }

            updatePostLikeStatus(postId, true)
        }
    }


    fun unlikePost(postId: String) {
        viewModelScope.launch {
            val currentUserId = _currentUserId.value ?: return@launch
            postRepository.unlikePost(postId, currentUserId)
            updatePostLikeStatus(postId, false)
        }
    }

    fun isPostAlreadySaved(postId: String): Boolean {
        return _savedPosts.value.any { it.id == postId }
    }

    private fun updatePostLikeStatus(postId: String, isLiked: Boolean) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(isLiked = isLiked, likeCount = if (isLiked) post.likeCount + 1 else post.likeCount - 1)
            } else post
        }
    }
}
