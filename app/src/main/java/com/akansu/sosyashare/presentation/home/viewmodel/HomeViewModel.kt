package com.akansu.sosyashare.presentation.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.repository.PostRepository
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
    private val postRepository: PostRepository
) : ViewModel() {

    private val _users = MutableStateFlow<Map<String, User>>(emptyMap())
    val users: StateFlow<Map<String, User>> = _users

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    fun loadFollowedUsersPosts() {
        viewModelScope.launch {
            val currentUserId = userRepository.getCurrentUserId()
            currentUserId?.let {
                val user = userRepository.getUserById(it).firstOrNull()
                user?.let { currentUser ->
                    userRepository.getFollowedUsersPosts(currentUser.following).collect { followedUsersPosts ->
                        // Kullanıcı bilgilerini yükle
                        val usersMap = followedUsersPosts.mapNotNull { post ->
                            userRepository.getUserById(post.userId).firstOrNull()?.let { user ->
                                post.userId to user
                            }
                        }.toMap()

                        _users.value = usersMap

                        // Postları güncelle
                        _posts.value = followedUsersPosts.map { post ->
                            val likedByUser = post.likedBy.contains(currentUserId)
                            post.copy(isLiked = likedByUser)
                        }
                    }
                }
            }
        }
    }


    fun likePost(postId: String) {
        viewModelScope.launch {
            val currentUserId = userRepository.getCurrentUserId() ?: return@launch
            Log.d("HomeViewModel", "Liking Post: postId=$postId by User: $currentUserId")
            try {
                postRepository.likePost(postId, currentUserId)
                _posts.value = _posts.value.map { p ->
                    if (p.id == postId) {
                        val updatedPost = p.copy(isLiked = true, likeCount = p.likeCount + 1)
                        Log.d("HomeViewModel", "Post Liked and Updated: $updatedPost")
                        updatedPost
                    } else p
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error Liking Post: postId=$postId, error=${e.message}")
            }
        }
    }

    fun unlikePost(postId: String) {
        viewModelScope.launch {
            val currentUserId = userRepository.getCurrentUserId() ?: return@launch
            Log.d("HomeViewModel", "Unliking Post: postId=$postId by User: $currentUserId")
            try {
                postRepository.unlikePost(postId, currentUserId)
                _posts.value = _posts.value.map { p ->
                    if (p.id == postId) {
                        val updatedPost = p.copy(isLiked = false, likeCount = p.likeCount - 1)
                        Log.d("HomeViewModel", "Post Unliked and Updated: $updatedPost")
                        updatedPost
                    } else p
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error Unliking Post: postId=$postId, error=${e.message}")
            }
        }
    }
}
