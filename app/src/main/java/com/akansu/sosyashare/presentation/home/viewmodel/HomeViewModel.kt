package com.akansu.sosyashare.presentation.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.data.remote.FirebasePostService
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.repository.PostRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import com.akansu.sosyashare.domain.usecase.profile.GetCurrentUserIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val postRepository: PostRepository,
    private val firebasePostService: FirebasePostService // FirebasePostService burada enjekte ediliyor
) : ViewModel() {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _users = MutableStateFlow<Map<String, User>>(emptyMap())
    val users: StateFlow<Map<String, User>> = _users

    fun loadFollowedUsersPosts() {
        viewModelScope.launch {
            val currentUserId = userRepository.getCurrentUserId()
            currentUserId?.let {
                userRepository.getUserById(it).collect { user ->
                    user?.let {
                        userRepository.getFollowedUsersPosts(user.following).collect { followedUsersPosts ->
                            _posts.value = followedUsersPosts.map { post ->
                                val likedByUser = post.likedBy.contains(currentUserId)
                                post.copy(isLiked = likedByUser)
                            }

                            followedUsersPosts.forEach { post ->
                                userRepository.getUserById(post.userId).collect { userDetails ->
                                    userDetails?.let { user ->
                                        _users.value += (user.id to user)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun likePost(postId: String, postUserId: String) {
        viewModelScope.launch {
            val currentUserId = userRepository.getCurrentUserId() ?: return@launch
            firebasePostService.likePost(postId, postUserId, currentUserId)
            _posts.value = _posts.value.map { post ->
                if (post.id == postId) {
                    post.copy(isLiked = true, likeCount = post.likeCount + 1)
                } else post
            }
        }
    }

    fun unlikePost(postId: String, postUserId: String) {
        viewModelScope.launch {
            val currentUserId = userRepository.getCurrentUserId() ?: return@launch
            firebasePostService.unlikePost(postId, postUserId, currentUserId)
            _posts.value = _posts.value.map { post ->
                if (post.id == postId) {
                    post.copy(isLiked = false, likeCount = post.likeCount - 1)
                } else post
            }
        }
    }

    fun getUserById(userId: String): User? {
        return _users.value[userId]
    }
}
