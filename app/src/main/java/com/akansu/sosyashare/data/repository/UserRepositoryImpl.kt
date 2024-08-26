package com.akansu.sosyashare.data.repository

import android.net.Uri
import com.akansu.sosyashare.data.mapper.toDomainModel
import com.akansu.sosyashare.data.remote.FirebaseUserService
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.repository.UserRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firebaseUserService: FirebaseUserService
) : UserRepository {

    override suspend fun searchUsers(query: String): List<User> {
        return firebaseUserService.searchUsers(query).map { it.toDomainModel() }
    }

    override suspend fun getCurrentUserName(): String? {
        return firebaseUserService.getCurrentUserName()
    }

    override fun getUserById(userId: String): Flow<User?> {
        return flow {
            val userEntity = firebaseUserService.getUserDetails(userId)
            emit(userEntity?.toDomainModel())
        }
    }

    override suspend fun updateUsernameInFirebase(userId: String, username: String) {
        firebaseUserService.updateUsername(userId, username)
    }

    override suspend fun updateBioInFirebase(userId: String, bio: String) {
        firebaseUserService.updateBio(userId, bio)
    }

    override suspend fun updateUser(user: User) {
        firebaseUserService.updateUsername(user.id, user.username)
        firebaseUserService.updateBio(user.id, user.bio)
        user.profilePictureUrl?.let {
            firebaseUserService.updateUserProfilePicture(user.id, it)
        }
    }

    override suspend fun likePost(postId: String, userId: String) {
        firebaseUserService.likePost(postId, userId)
    }

    override suspend fun unlikePost(postId: String, userId: String) {
        firebaseUserService.unlikePost(postId, userId)
    }


    override suspend fun followUser(currentUserId: String, followUserId: String) {
        firebaseUserService.followUser(currentUserId, followUserId)
    }

    override suspend fun unfollowUser(currentUserId: String, unfollowUserId: String) {
        firebaseUserService.unfollowUser(currentUserId, unfollowUserId)
    }

    override suspend fun getFollowedUsersPosts(userIds: List<String>): Flow<List<Post>> {
        return firebaseUserService.getFollowedUsersPosts(userIds).map { posts ->
            posts.map { it.toDomainModel() }
        }
    }

    override suspend fun uploadProfilePicture(uri: Uri): String {
        return firebaseUserService.uploadProfilePicture(uri)
    }

    override suspend fun uploadPostPicture(uri: Uri): String {
        return firebaseUserService.uploadPostPicture(uri)
    }

    override suspend fun deletePost(userId: String, postId: String, postImageUrl: String) {
        firebaseUserService.deletePost(userId, postId, postImageUrl)
    }


    override suspend fun registerUser(email: String, password: String, username: String) {
        firebaseUserService.registerUser(email, password, username)
    }

    override suspend fun getUserDetails(userId: String): User? {
        return firebaseUserService.getUserDetails(userId)?.toDomainModel()
    }

    override suspend fun syncAllUsers() {
        firebaseUserService.syncAllUsers()
    }

    override suspend fun isUsernameUnique(username: String): Boolean {
        return firebaseUserService.isUsernameUnique(username)
    }

    override suspend fun getCurrentUserId(): String? {
        return firebaseUserService.getCurrentUserId()
    }

    override suspend fun getCurrentUserProfilePictureUrl(): String? {
        return firebaseUserService.getCurrentUserProfilePictureUrl()
    }

    override suspend fun checkIfFollowing(currentUserId: String, userId: String): Boolean {
        val user = firebaseUserService.getUserDetails(currentUserId)
        return user?.following?.contains(userId) == true
    }

    override suspend fun getFollowedUserIds(currentUserId: String): List<String> {
        return firebaseUserService.getFollowedUserIds(currentUserId)
    }

    override suspend fun updateUserProfilePicture(userId: String, profilePictureUrl: String) {
        firebaseUserService.updateUserProfilePicture(userId, profilePictureUrl)
    }
}
