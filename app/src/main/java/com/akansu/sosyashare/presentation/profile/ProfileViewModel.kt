package com.akansu.sosyashare.presentation.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.data.remote.FirebaseUserPrivacyService
import com.akansu.sosyashare.domain.model.Post
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
    private val userPrivacyService: FirebaseUserPrivacyService,
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
                Log.d("ProfileViewModel", "Loading profile data for userId: $userId")

                val isPrivate = userPrivacyService.fetchIsPrivateDirectly(userId)
                _isPrivateAccount.value = isPrivate
                Log.d("ProfileViewModel", "isPrivateAccount for $userId is $isPrivate")

                _isFollowing.value = userRepository.checkIfFollowing(currentUserId, userId)
                Log.d("ProfileViewModel", "isFollowing for $userId by $currentUserId is ${_isFollowing.value}")

                _userDetails.value = userRepository.getUserDetails(userId)
                Log.d("ProfileViewModel", "User details for $userId loaded: ${_userDetails.value}")

                val allowedFollowers = userPrivacyService.getUserPrivacy(userId)?.allowedFollowers ?: emptyList()
                if (shouldFetchPosts(isPrivate, allowedFollowers)) {
                    _userPosts.value = postRepository.getUserPosts(userId)
                    Log.d("ProfileViewModel", "User posts loaded for $userId: ${_userPosts.value.size} posts")
                } else {
                    _userPosts.value = emptyList()
                    Log.d("ProfileViewModel", "User posts hidden due to privacy settings for $userId")
                }

                loadFollowersAndFollowing(userId)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading profile data for $userId: ${e.message}")
            }
        }
    }


    private suspend fun loadFollowersAndFollowing(userId: String) {
        try {
            _followers.value = userRepository.getFollowers(userId)
            Log.d("ProfileViewModel", "Followers loaded for $userId: ${_followers.value.size}")

            _following.value = userRepository.getFollowing(userId)
            Log.d("ProfileViewModel", "Following loaded for $userId: ${_following.value.size}")
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error loading followers and following for $userId: ${e.message}")
        }
    }

    private fun shouldFetchPosts(isPrivate: Boolean, allowedFollowers: List<String>): Boolean {
        val isAllowed = allowedFollowers.contains(currentUserId.value)
        val shouldFetch = !(isPrivate && !isAllowed)
        Log.d("ProfileViewModel", "Should fetch posts: $shouldFetch")
        return shouldFetch
    }

    fun followUser(currentUserId: String, followUserId: String) {
        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "Attempting to follow user: $followUserId by $currentUserId")

                userRepository.followUser(currentUserId, followUserId)
                userPrivacyRepository.addAllowedFollower(followUserId, currentUserId)

                Log.d("ProfileViewModel", "Successfully followed user: $followUserId by $currentUserId")
                loadProfileData(currentUserId, followUserId)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error following user: $followUserId by $currentUserId: ${e.message}")
            }
        }
    }

    fun unfollowUser(currentUserId: String, unfollowUserId: String) {
        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "Attempting to unfollow user: $unfollowUserId by $currentUserId")

                userRepository.unfollowUser(currentUserId, unfollowUserId)
                userPrivacyRepository.removeAllowedFollower(unfollowUserId, currentUserId)

                // Kullanıcı unfollow yapıldığında, isPrivateAccount değerini güncelle
                _isPrivateAccount.value = userPrivacyService.fetchIsPrivateDirectly(unfollowUserId)

                Log.d("ProfileViewModel", "Successfully unfollowed user: $unfollowUserId by $currentUserId")
                loadProfileData(currentUserId, unfollowUserId)  // Profil verilerini yeniden yükleyerek doğru state'i sağla
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error unfollowing user: $unfollowUserId by $currentUserId: ${e.message}")
            }
        }
    }

}
