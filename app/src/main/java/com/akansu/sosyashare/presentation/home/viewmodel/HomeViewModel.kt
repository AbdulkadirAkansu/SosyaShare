package com.akansu.sosyashare.presentation.home.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.repository.MessagingRepository
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
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val saveRepository: SaveRepository,
    private val notificationRepository: NotificationRepository,
    private val messagingRepository: MessagingRepository
) : ViewModel() {

    private val _users = MutableStateFlow<Map<String, User>>(emptyMap())
    val users: StateFlow<Map<String, User>> = _users

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _savedPosts = MutableStateFlow<List<Post>>(emptyList())
    val savedPosts: StateFlow<List<Post>> = _savedPosts

    private val _likedUsers = MutableStateFlow<List<User>>(emptyList())
    val likedUsers: StateFlow<List<User>> = _likedUsers

    init {
        loadFollowedUsersPosts()
        loadSavedPosts()
    }

    fun loadFollowedUsersPosts() {
        viewModelScope.launch {
            val currentUserId = userRepository.getCurrentUserId()
            currentUserId?.let {
                val user = userRepository.getUserById(it).firstOrNull()
                user?.let { currentUser ->
                    userRepository.getFollowedUsersPosts(currentUser.following).collect { followedUsersPosts ->
                        val usersMap = followedUsersPosts.mapNotNull { post ->
                            userRepository.getUserById(post.userId).firstOrNull()?.let { user ->
                                post.userId to user
                            }
                        }.toMap()

                        _users.value = usersMap

                        _posts.value = followedUsersPosts.map { post ->
                            val likedByUser = post.likedBy.contains(currentUserId)
                            post.copy(isLiked = likedByUser)
                        }
                    }
                }
            }
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

    fun savePost(postId: String) {
        viewModelScope.launch {
            saveRepository.savePost(postId)
            loadSavedPosts() // Save işlemi sonrası hemen güncelleniyor
        }
    }

    fun removeSavedPost(postId: String) {
        viewModelScope.launch {
            saveRepository.removeSavedPost(postId)
            loadSavedPosts() // Remove işlemi sonrası hemen güncelleniyor
        }
    }

    fun loadSavedPosts() {
        viewModelScope.launch {
            val saves = saveRepository.getSavedPosts()
            val posts = saves.mapNotNull { save ->
                postRepository.getPostById(save.postId)
            }
            _savedPosts.value = posts
        }
    }

    fun likePost(postId: String, postOwnerId: String, context: Context) {
        viewModelScope.launch {
            val currentUserId = userRepository.getCurrentUserId() ?: return@launch
            val currentUser = userRepository.getUserById(currentUserId).firstOrNull()
            // getPostById doğrudan Post? döndürdüğü için firstOrNull() kullanmanıza gerek yok
            val post = postRepository.getPostById(postId) // suspend fonksiyon zaten Post? döndürüyor

            if (currentUser != null && post != null) {
                // Kullanıcının tekrar tekrar beğeni yapmasını engelle
                val canLike = notificationRepository.canUserLikeOrComment(currentUserId, postId, "like")

                if (canLike) {
                    try {
                        // Gönderiyi beğen
                        postRepository.likePost(postId, currentUserId)

                        // Uygulama içi bildirim gönder
                        notificationRepository.sendNotification(
                            userId = postOwnerId,
                            postId = postId,
                            senderId = currentUserId,
                            senderUsername = currentUser.username,
                            senderProfileUrl = currentUser.profilePictureUrl,
                            notificationType = "like"
                        )

                        val fcmToken = messagingRepository.getFCMTokenByUserId(postOwnerId)
                        if (fcmToken != null) {
                            val postContent = post.content ?: "Your post was liked!"
                            messagingRepository.sendFCMNotification(
                                context, fcmToken, "${currentUser.username} liked your post", postContent
                            )
                        }

                        // Beğeni durumu güncellemesi
                        _posts.value = _posts.value.map { p ->
                            if (p.id == postId) {
                                p.copy(isLiked = true, likeCount = p.likeCount + 1)
                            } else p
                        }
                    } catch (e: Exception) {
                        Log.e("HomeViewModel", "Error Liking Post: postId=$postId, error=${e.message}")
                    }
                }
            }
        }
    }


    fun unlikePost(postId: String) {
        viewModelScope.launch {
            val currentUserId = userRepository.getCurrentUserId() ?: return@launch
            try {
                postRepository.unlikePost(postId, currentUserId)
                _posts.value = _posts.value.map { p ->
                    if (p.id == postId) {
                        p.copy(isLiked = false, likeCount = p.likeCount - 1)
                    } else p
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error Unliking Post: postId=$postId, error=${e.message}")
            }
        }
    }
}
