package com.akansu.sosyashare.presentation.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.model.UserPrivacy
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.repository.PostRepository
import com.akansu.sosyashare.domain.repository.UserPrivacyRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userPrivacyRepository: UserPrivacyRepository,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _userPosts = MutableStateFlow<List<Post>>(emptyList())
    val userPosts: StateFlow<List<Post>> = _userPosts

    private val _isPrivateAccount = MutableStateFlow(false)
    val isPrivateAccount: StateFlow<Boolean> = _isPrivateAccount

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _userDetails = MutableStateFlow<User?>(null)
    val userDetails: StateFlow<User?> = _userDetails

    private val _followers = MutableStateFlow<List<User>>(emptyList())
    val followers: StateFlow<List<User>> = _followers

    private val _following = MutableStateFlow<List<User>>(emptyList())
    val following: StateFlow<List<User>> = _following

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                _currentUserId.value = userRepository.getCurrentUserId()
                Log.d("ProfileViewModel", "Current user ID loaded: ${_currentUserId.value}")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading current user ID: ${e.message}")
            }
        }
    }

    fun loadProfileData(currentUserId: String, userId: String) {
        viewModelScope.launch {
            try {
                val privateAccount = userPrivacyRepository.getUserPrivacy(userId)
                Log.d("ProfileViewModel", "Fetched UserPrivacy: $privateAccount")
                _isPrivateAccount.value = privateAccount?.isPrivate ?: false
                Log.d("ProfileViewModel", "isPrivateAccount value set to: ${_isPrivateAccount.value}")

                _isFollowing.value = userRepository.checkIfFollowing(currentUserId, userId)
                Log.d("ProfileViewModel", "isFollowing value set to: ${_isFollowing.value}")

                // Kullanıcı bilgilerini yükle
                _userDetails.value = userRepository.getUserDetails(userId)
                Log.d("ProfileViewModel", "User details loaded: ${_userDetails.value}")

                // Gönderileri yükle
                if (shouldFetchPosts(privateAccount, currentUserId)) {
                    _userPosts.value = postRepository.getUserPosts(userId)
                    Log.d("ProfileViewModel", "User posts loaded: ${_userPosts.value.size} posts")
                }

                // Followers ve Following listelerini yükle
                loadFollowersAndFollowing(userId)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading profile data: ${e.message}")
            }
        }
    }

    private suspend fun loadFollowersAndFollowing(userId: String) {
        try {
            _followers.value = userRepository.getFollowers(userId)
            _following.value = userRepository.getFollowing(userId)
            Log.d("ProfileViewModel", "Followers loaded: ${_followers.value.size}, Following loaded: ${_following.value.size}")
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error loading followers and following: ${e.message}")
        }
    }

    private fun shouldFetchPosts(privateAccount: UserPrivacy?, currentUserId: String): Boolean {
        val shouldFetch = !(_isPrivateAccount.value && !_isFollowing.value && privateAccount?.allowedFollowers?.contains(currentUserId) != true)
        Log.d("ProfileViewModel", "Should fetch posts: $shouldFetch")
        return shouldFetch
    }

    fun followUser(currentUserId: String, followUserId: String) {
        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "Attempting to follow user: $followUserId")
                userRepository.followUser(currentUserId, followUserId)
                userPrivacyRepository.addAllowedFollower(followUserId, currentUserId)
                Log.d("ProfileViewModel", "Successfully followed user: $followUserId")
                checkIfFollowing(currentUserId, followUserId)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error following user: ${e.message}")
            }
        }
    }

    fun unfollowUser(currentUserId: String, unfollowUserId: String) {
        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "Attempting to unfollow user: $unfollowUserId")
                userRepository.unfollowUser(currentUserId, unfollowUserId)
                userPrivacyRepository.removeAllowedFollower(unfollowUserId, currentUserId)
                Log.d("ProfileViewModel", "Successfully unfollowed user: $unfollowUserId")
                checkIfFollowing(currentUserId, unfollowUserId)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error unfollowing user: ${e.message}")
            }
        }
    }

    private fun checkIfFollowing(currentUserId: String, userId: String) {
        viewModelScope.launch {
            try {
                val isFollowing = userRepository.checkIfFollowing(currentUserId, userId)
                _isFollowing.value = isFollowing
                Log.d("ProfileViewModel", "Following status updated: isFollowing = $isFollowing")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error checking follow status: ${e.message}")
            }
        }
    }
}
