package com.akansu.sosyashare.domain.repository

import android.net.Uri
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.model.User
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun searchUsers(query: String): List<User>
    fun getUserById(userId: String): Flow<User?>
    suspend fun updateUsernameInFirebase(userId: String, username: String)
    suspend fun updateBioInFirebase(userId: String, bio: String)
    suspend fun updateUser(user: User)
    suspend fun followUser(currentUserId: String, followUserId: String)
    suspend fun unfollowUser(currentUserId: String, unfollowUserId: String)
    suspend fun getFollowedUsersPosts(userIds: List<String>): Flow<List<Post>>
    suspend fun uploadProfilePicture(uri: Uri): String
    suspend fun uploadPostPicture(uri: Uri): String
    suspend fun deletePost(userId: String, postId: String, postImageUrl: String)
    suspend fun getUserDetails(userId: String): User?
    suspend fun syncAllUsers()
    suspend fun isUsernameUnique(username: String): Boolean
    suspend fun getCurrentUserId(): String?
    suspend fun getCurrentUserProfilePictureUrl(): String?
    suspend fun checkIfFollowing(currentUserId: String, userId: String): Boolean
    suspend fun getFollowedUserIds(currentUserId: String): List<String>
    suspend fun likePost(postId: String, userId: String)
    suspend fun unlikePost(postId: String, userId: String)
    suspend fun updateUserProfilePicture(userId: String, profilePictureUrl: String)
    suspend fun getCurrentUserName(): String?
    suspend fun getFollowers(userId: String): List<User>
    suspend fun getFollowing(userId: String): List<User>
    suspend fun updateBackgroundImageUrl(userId: String, backgroundImageUrl: String)
}
