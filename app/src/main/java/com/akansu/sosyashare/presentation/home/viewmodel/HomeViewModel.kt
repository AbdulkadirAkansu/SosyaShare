package com.akansu.sosyashare.presentation.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.model.User
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
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
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
                            _posts.value = followedUsersPosts

                            // Load user details for each post
                            followedUsersPosts.forEach { post ->
                                userRepository.getUserById(post.userId).collect { userDetails ->
                                    userDetails?.let { user ->
                                        _users.value = _users.value + (user.id to user)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun likePost(postId: String) {
        viewModelScope.launch {
            val currentUserId = userRepository.getCurrentUserId() ?: return@launch
            userRepository.likePost(postId, currentUserId)
            loadFollowedUsersPosts()
        }
    }

    fun unlikePost(postId: String) {
        viewModelScope.launch {
            val currentUserId = userRepository.getCurrentUserId() ?: return@launch
            userRepository.unlikePost(postId, currentUserId)
            loadFollowedUsersPosts()
        }
    }

    fun getUserById(userId: String): User? {
        return _users.value[userId]
    }
}
