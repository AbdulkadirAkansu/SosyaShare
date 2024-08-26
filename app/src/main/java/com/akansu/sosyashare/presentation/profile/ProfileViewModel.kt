package com.akansu.sosyashare.presentation.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.model.PrivateAccount
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.repository.PostRepository
import com.akansu.sosyashare.domain.repository.PrivateAccountRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val privateAccountRepository: PrivateAccountRepository
) : ViewModel() {

    private val _userDetails = MutableStateFlow<User?>(null)
    val userDetails: StateFlow<User?> get() = _userDetails

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> get() = _isFollowing

    private val _isPrivateAccount = MutableStateFlow(false)
    val isPrivateAccount: StateFlow<Boolean> get() = _isPrivateAccount

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> get() = _currentUserId

    private val _userPosts = MutableStateFlow<List<Post>>(emptyList())
    val userPosts: StateFlow<List<Post>> get() = _userPosts

    init {
        loadCurrentUserId()
    }

    private fun loadCurrentUserId() {
        viewModelScope.launch {
            _currentUserId.value = userRepository.getCurrentUserId()
            Log.d("ProfileViewModel", "Current User ID: ${_currentUserId.value}")
        }
    }

    fun loadProfileData(currentUserId: String, userId: String) {
        viewModelScope.launch {
            val user = userRepository.getUserById(userId).firstOrNull()
            val privateAccount = privateAccountRepository.getPrivateAccount(userId)

            if (user != null) {
                _userDetails.value = user
                _isFollowing.value = userRepository.checkIfFollowing(currentUserId, userId)
                _isPrivateAccount.value = privateAccount?.isPrivate ?: false

                Log.d("ProfileViewModel", "PrivateAccount isPrivate: ${_isPrivateAccount.value}, isFollowing: ${_isFollowing.value}")

                if (shouldFetchPosts(privateAccount, currentUserId)) {
                    postRepository.getPostsByUser(userId).collect { posts ->
                        _userPosts.value = posts
                        Log.d("ProfileViewModel", "Posts loaded: ${posts.size} posts")
                    }
                } else {
                    _userPosts.value = emptyList()
                    Log.d("ProfileViewModel", "User is private and not followed, hiding posts.")
                }
            } else {
                Log.d("ProfileViewModel", "User not found or unable to load user data.")
            }
        }
    }

    private fun shouldFetchPosts(privateAccount: PrivateAccount?, currentUserId: String): Boolean {
        return privateAccount == null || !privateAccount.isPrivate || privateAccount.allowedFollowers.contains(currentUserId)
    }

    fun followUser(currentUserId: String, followUserId: String) {
        viewModelScope.launch {
            Log.d("ProfileViewModel", "Attempting to follow user: $followUserId")
            userRepository.followUser(currentUserId, followUserId)
            privateAccountRepository.addAllowedFollower(followUserId, currentUserId)
            Log.d("ProfileViewModel", "Successfully followed user: $followUserId")
            checkIfFollowing(currentUserId, followUserId)
        }
    }

    fun unfollowUser(currentUserId: String, unfollowUserId: String) {
        viewModelScope.launch {
            Log.d("ProfileViewModel", "Attempting to unfollow user: $unfollowUserId")
            userRepository.unfollowUser(currentUserId, unfollowUserId)
            privateAccountRepository.removeAllowedFollower(unfollowUserId, currentUserId)
            Log.d("ProfileViewModel", "Successfully unfollowed user: $unfollowUserId")
            checkIfFollowing(currentUserId, unfollowUserId)
        }
    }

    private fun checkIfFollowing(currentUserId: String, userId: String) {
        viewModelScope.launch {
            Log.d("ProfileViewModel", "Checking if $currentUserId is following $userId")
            val isFollowing = userRepository.checkIfFollowing(currentUserId, userId)
            _isFollowing.value = isFollowing
            Log.d("ProfileViewModel", "Following status updated: isFollowing = $isFollowing")
        }
    }
}
