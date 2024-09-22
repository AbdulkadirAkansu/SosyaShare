package com.akansu.sosyashare.presentation.profile

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.data.remote.FirebaseUserPrivacyService
import com.akansu.sosyashare.domain.model.BlockedUser
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.repository.BlockedUserRepository
import com.akansu.sosyashare.domain.repository.MessagingRepository
import com.akansu.sosyashare.domain.repository.NotificationRepository
import com.akansu.sosyashare.domain.repository.PostRepository
import com.akansu.sosyashare.domain.repository.UserPrivacyRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userPrivacyRepository: UserPrivacyRepository,
    private val userPrivacyService: FirebaseUserPrivacyService,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val blockedUserRepository: BlockedUserRepository,
    private val notificationRepository: NotificationRepository,
    private val messagingRepository: MessagingRepository
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

    private val _backgroundImageUrl = MutableStateFlow<String?>(null)
    val backgroundImageUrl: StateFlow<String?> = _backgroundImageUrl

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
                val user = userRepository.getUserById(userId).firstOrNull()
                _userDetails.value = user

                _backgroundImageUrl.value = user?.backgroundImageUrl

                _isFollowing.value = userRepository.checkIfFollowing(currentUserId, userId)
                Log.d(
                    "ProfileViewModel",
                    "isFollowing for $userId by $currentUserId is ${_isFollowing.value}"
                )

                _userDetails.value = userRepository.getUserDetails(userId)
                Log.d("ProfileViewModel", "User details for $userId loaded: ${_userDetails.value}")

                val allowedFollowers =
                    userPrivacyService.getUserPrivacy(userId)?.allowedFollowers ?: emptyList()
                if (shouldFetchPosts(isPrivate, allowedFollowers)) {
                    _userPosts.value = postRepository.getUserPosts(userId)
                    Log.d(
                        "ProfileViewModel",
                        "User posts loaded for $userId: ${_userPosts.value.size} posts"
                    )
                } else {
                    _userPosts.value = emptyList()
                    Log.d(
                        "ProfileViewModel",
                        "User posts hidden due to privacy settings for $userId"
                    )
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
            Log.e(
                "ProfileViewModel",
                "Error loading followers and following for $userId: ${e.message}"
            )
        }
    }

    private fun shouldFetchPosts(isPrivate: Boolean, allowedFollowers: List<String>): Boolean {
        val isAllowed = allowedFollowers.contains(currentUserId.value)
        val shouldFetch = !(isPrivate && !isAllowed)
        Log.d("ProfileViewModel", "Should fetch posts: $shouldFetch")
        return shouldFetch
    }

    fun followUser(currentUserId: String, followUserId: String, context: Context) {
        viewModelScope.launch {
            try {
                Log.d(
                    "ProfileViewModel",
                    "Attempting to follow user: $followUserId by $currentUserId"
                )

                // Kullanıcıyı takip et
                userRepository.followUser(currentUserId, followUserId)
                userPrivacyRepository.addAllowedFollower(followUserId, currentUserId)

                // Bildirim gönder
                val currentUser = userRepository.getUserById(currentUserId).firstOrNull()
                currentUser?.let { user ->
                    notificationRepository.sendNotification(
                        userId = followUserId,
                        postId = "",
                        senderId = currentUserId,
                        senderUsername = user.username,
                        senderProfileUrl = user.profilePictureUrl,
                        notificationType = "follow"
                    )

                    // FCM Token alma ve bildirim gönderme
                    val fcmToken = messagingRepository.getFCMTokenByUserId(followUserId)
                    fcmToken?.let { token ->
                        val messageTitle = "New Follower"
                        val messageBody = "${user.username} started following you."
                        messagingRepository.sendFCMNotification(
                            context,
                            token,
                            messageTitle,
                            messageBody
                        )
                    } ?: run {
                        Log.e("ProfileViewModel", "FCM Token bulunamadı.")
                    }
                }

                Log.d(
                    "ProfileViewModel",
                    "Successfully followed user: $followUserId by $currentUserId"
                )
                loadProfileData(currentUserId, followUserId)
            } catch (e: Exception) {
                Log.e(
                    "ProfileViewModel",
                    "Error following user: $followUserId by $currentUserId: ${e.message}"
                )
            }
        }
    }

    fun unfollowUser(currentUserId: String, unfollowUserId: String, context: Context) {
        viewModelScope.launch {
            try {
                Log.d(
                    "ProfileViewModel",
                    "Attempting to unfollow user: $unfollowUserId by $currentUserId"
                )

                // Kullanıcıyı takip etmeyi bırak
                userRepository.unfollowUser(currentUserId, unfollowUserId)
                userPrivacyRepository.removeAllowedFollower(unfollowUserId, currentUserId)

                // Bildirim gönderme opsiyonel
                val currentUser = userRepository.getUserById(currentUserId).firstOrNull()
                currentUser?.let { user ->
                    notificationRepository.sendNotification(
                        userId = unfollowUserId,
                        postId = "",
                        senderId = currentUserId,
                        senderUsername = user.username,
                        senderProfileUrl = user.profilePictureUrl,
                        notificationType = "unfollow"
                    )

                    // FCM Token alma ve bildirim gönderme
                    val fcmToken = messagingRepository.getFCMTokenByUserId(unfollowUserId)
                    fcmToken?.let { token ->
                        val messageTitle = "Unfollow Notification"
                        val messageBody = "${user.username} unfollowed you."
                        messagingRepository.sendFCMNotification(
                            context,
                            token,
                            messageTitle,
                            messageBody
                        )
                    } ?: run {
                        Log.e("ProfileViewModel", "FCM Token bulunamadı.")
                    }
                }

                _isPrivateAccount.value = userPrivacyService.fetchIsPrivateDirectly(unfollowUserId)

                Log.d(
                    "ProfileViewModel",
                    "Successfully unfollowed user: $unfollowUserId by $currentUserId"
                )
                loadProfileData(currentUserId, unfollowUserId)
            } catch (e: Exception) {
                Log.e(
                    "ProfileViewModel",
                    "Error unfollowing user: $unfollowUserId by $currentUserId: ${e.message}"
                )
            }
        }
    }


    fun blockUser(currentUserId: String, blockUserId: String) {
        viewModelScope.launch {
            try {
                val blockedUser = BlockedUser(
                    blockerUserId = currentUserId,
                    blockedUserId = blockUserId
                )
                blockedUserRepository.blockUser(blockedUser)
                Log.d(
                    "ProfileViewModel",
                    "User $blockUserId successfully blocked by $currentUserId"
                )
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error blocking user: ${e.message}")
            }
        }
    }

    suspend fun fetchUserDetailsByIds(userIds: List<String>): List<User> {
        return userIds.mapNotNull { userId ->
            userRepository.getUserById(userId).firstOrNull()
        }
    }

    suspend fun getUserById(userId: String): User? {
        return userRepository.getUserById(userId).firstOrNull()
    }


    fun unblockUser(currentUserId: String, blockedUserId: String) {
        viewModelScope.launch {
            try {
                blockedUserRepository.unblockUser(currentUserId, blockedUserId)
                Log.d(
                    "ProfileViewModel",
                    "User $blockedUserId successfully unblocked by $currentUserId"
                )
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error unblocking user: ${e.message}")
            }
        }
    }
}
