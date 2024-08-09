package com.akansu.sosyashare.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.usecase.profile.CheckIfFollowingUseCase
import com.akansu.sosyashare.domain.usecase.profile.FollowUserUseCase
import com.akansu.sosyashare.domain.usecase.profile.GetCurrentUserIdUseCase
import com.akansu.sosyashare.domain.usecase.profile.GetUserDetailsUseCase
import com.akansu.sosyashare.domain.usecase.profile.UnfollowUserUseCase
import com.akansu.sosyashare.domain.usecase.post.GetUserPostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val followUserUseCase: FollowUserUseCase,
    private val unfollowUserUseCase: UnfollowUserUseCase,
    private val checkIfFollowingUseCase: CheckIfFollowingUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getUserPostsUseCase: GetUserPostsUseCase
) : ViewModel() {

    private val _userDetails = MutableStateFlow<User?>(null)
    val userDetails: StateFlow<User?> get() = _userDetails

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> get() = _isFollowing

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> get() = _currentUserId

    private val _userPosts = MutableStateFlow<List<Post>>(emptyList())
    val userPosts: StateFlow<List<Post>> get() = _userPosts

    init {
        loadCurrentUserId()
    }

    private fun loadCurrentUserId() {
        viewModelScope.launch {
            _currentUserId.value = getCurrentUserIdUseCase()
        }
    }

    fun loadUserDetails(userId: String) {
        viewModelScope.launch {
            val user = getUserDetailsUseCase(userId).firstOrNull()
            _userDetails.value = user

            user?.let {
                getUserPostsUseCase(userId).collect { posts ->
                    _userPosts.value = posts
                }
            }
        }
    }

    fun followUser(currentUserId: String, followUserId: String) {
        viewModelScope.launch {
            followUserUseCase(currentUserId, followUserId)
            checkIfFollowing(currentUserId, followUserId)
        }
    }

    fun unfollowUser(currentUserId: String, unfollowUserId: String) {
        viewModelScope.launch {
            unfollowUserUseCase(currentUserId, unfollowUserId)
            checkIfFollowing(currentUserId, unfollowUserId)
        }
    }

    fun checkIfFollowing(currentUserId: String, userId: String) {
        viewModelScope.launch {
            val isFollowing = checkIfFollowingUseCase(currentUserId, userId)
            _isFollowing.value = isFollowing
        }
    }
}
