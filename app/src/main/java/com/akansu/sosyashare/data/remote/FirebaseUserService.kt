package com.akansu.sosyashare.data.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import com.akansu.sosyashare.data.mapper.UserMapper
import com.akansu.sosyashare.data.mapper.toDomainModel
import com.akansu.sosyashare.data.model.PostEntity
import com.akansu.sosyashare.data.model.UserEntity
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.util.NetworkUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseUserService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val context: Context
) {
    private val offlineErrorMessage = "İnternet bağlantısı yok. Lütfen internet bağlantınızı kontrol edin."

    private fun checkNetworkConnection() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.e("FirebaseUserService", "No internet connection")
            throw FirebaseFirestoreException("İnternet bağlantısı yok", FirebaseFirestoreException.Code.UNAVAILABLE)
        }
    }

    suspend fun getCurrentUserName(): String? {
        try {
            val userId = auth.currentUser?.uid ?: return null
            val document = firestore.collection("users").document(userId).get().await()
            return document.getString("username")
        } catch (e: Exception) {
            Log.e("FirebaseUserService", "Error getting current user name: ${e.message}")
            return null
        }
    }

    suspend fun updateBackgroundImageUrl(userId: String, backgroundImageUrl: String) {
        try {
            checkNetworkConnection()
            firestore.collection("users").document(userId)
                .update("backgroundImageUrl", backgroundImageUrl)
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseUserService", "Error updating background image: ${e.message}")
            throw e
        }
    }

    suspend fun getUserDetails(userId: String): UserEntity? {
        return try {
            // Önce yerel önbellekten deneyelim
            val document = firestore.collection("users").document(userId).get().await()
            if (document.exists()) {
                val user = document.toObject(UserEntity::class.java)?.copy(id = userId)
                user
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FirebaseUserService", "Error getting user details: ${e.message}")
            null
        }
    }

    suspend fun updateUsername(userId: String, username: String) {
        try {
            checkNetworkConnection()
            firestore.collection("users").document(userId).update("username", username).await()
        } catch (e: Exception) {
            Log.e("FirebaseUserService", "Error updating username: ${e.message}")
            throw e
        }
    }

    suspend fun updateBio(userId: String, bio: String) {
        try {
            checkNetworkConnection()
            firestore.collection("users").document(userId).update("bio", bio).await()
        } catch (e: Exception) {
            Log.e("FirebaseUserService", "Error updating bio: ${e.message}")
            throw e
        }
    }

    suspend fun updateUserProfilePicture(userId: String, profilePictureUrl: String) {
        try {
            checkNetworkConnection()
            firestore.collection("users").document(userId)
                .update("profilePictureUrl", profilePictureUrl)
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseUserService", "Error updating profile picture: ${e.message}")
            throw e
        }
    }

    suspend fun searchUsers(query: String): List<User> {
        try {
            checkNetworkConnection()
            val users = mutableListOf<User>()
            val result = firestore.collection("users")
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .get()
                .await()

            for (document in result.documents) {
                val userEntity = document.toObject(UserEntity::class.java)?.copy(id = document.id)
                if (userEntity != null) {
                    users.add(userEntity.toDomainModel())
                }
            }
            return users
        } catch (e: Exception) {
            Log.e("FirebaseUserService", "Error searching users: ${e.message}")
            return emptyList()
        }
    }

    suspend fun followUser(currentUserId: String, followUserId: String) {
        try {
            checkNetworkConnection()
            firestore.runTransaction { transaction ->
                val currentUserRef = firestore.collection("users").document(currentUserId)
                val followUserRef = firestore.collection("users").document(followUserId)

                val currentUser = transaction.get(currentUserRef).toObject(UserEntity::class.java)
                val followUser = transaction.get(followUserRef).toObject(UserEntity::class.java)

                val currentUserFollowing = currentUser?.following?.toMutableList() ?: mutableListOf()
                val followUserFollowers = followUser?.followers?.toMutableList() ?: mutableListOf()

                if (!currentUserFollowing.contains(followUserId)) {
                    currentUserFollowing.add(followUserId)
                    followUserFollowers.add(currentUserId)
                }

                transaction.update(currentUserRef, "following", currentUserFollowing)
                transaction.update(followUserRef, "followers", followUserFollowers)
            }.await()
        } catch (e: Exception) {
            Log.e("FirebaseUserService", "Error following user: ${e.message}")
            throw e
        }
    }

    suspend fun unfollowUser(currentUserId: String, unfollowUserId: String) {
        try {
            checkNetworkConnection()
            firestore.runTransaction { transaction ->
                val currentUserRef = firestore.collection("users").document(currentUserId)
                val unfollowUserRef = firestore.collection("users").document(unfollowUserId)

                val currentUser = transaction.get(currentUserRef).toObject(UserEntity::class.java)
                val unfollowUser = transaction.get(unfollowUserRef).toObject(UserEntity::class.java)

                val currentUserFollowing = currentUser?.following?.toMutableList() ?: mutableListOf()
                val unfollowUserFollowers = unfollowUser?.followers?.toMutableList() ?: mutableListOf()

                if (currentUserFollowing.contains(unfollowUserId)) {
                    currentUserFollowing.remove(unfollowUserId)
                    unfollowUserFollowers.remove(currentUserId)
                }

                transaction.update(currentUserRef, "following", currentUserFollowing)
                transaction.update(unfollowUserRef, "followers", unfollowUserFollowers)
            }.await()
        } catch (e: Exception) {
            Log.e("FirebaseUserService", "Error unfollowing user: ${e.message}")
            throw e
        }
    }

    suspend fun syncAllUsers(): List<UserEntity> {
        try {
            val result = firestore.collection("users").get().await()
            val users = mutableListOf<UserEntity>()
            for (document in result.documents) {
                val user = document.toObject(UserEntity::class.java)?.copy(id = document.id)
                if (user != null) {
                    users.add(user)
                }
            }
            return users
        } catch (e: Exception) {
            Log.e("FirebaseUserService", "Error syncing users: ${e.message}")
            return emptyList()
        }
    }

    suspend fun deletePost(userId: String, postId: String, postImageUrl: String) {
        val postRef =
            firestore.collection("users").document(userId).collection("posts").document(postId)
        postRef.delete().await()
        if (postImageUrl.isNotEmpty()) {
            val storageRef = firebaseStorage.getReferenceFromUrl(postImageUrl)
            storageRef.delete().await()
        }
    }

    suspend fun uploadProfilePicture(uri: Uri): String {
        val ref = firebaseStorage.reference.child("profile_pictures/${uri.lastPathSegment}")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadPostPicture(uri: Uri): String {
        val ref = firebaseStorage.reference.child("post_pictures/${uri.lastPathSegment}")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun isUsernameUnique(username: String): Boolean {
        val result = firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .await()
        return result.isEmpty
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    suspend fun getCurrentUserProfilePictureUrl(): String? {
        val userId = auth.currentUser?.uid ?: return null
        val userEntity = getUserDetails(userId)
        return userEntity?.profilePictureUrl
    }

    suspend fun likePost(postId: String, userId: String) {
        try {
            val postRef =
                firestore.collection("users").document(userId).collection("posts").document(postId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentLikes = snapshot.getLong("likeCount") ?: 0
                transaction.update(postRef, "likeCount", currentLikes + 1)
                val likesList = snapshot.get("likes") as? List<String> ?: emptyList()
                transaction.update(postRef, "likes", likesList + auth.currentUser?.uid)
            }.await()
        } catch (e: Exception) {
            // Hata yönetimi
        }
    }

    // Unlike a post
    suspend fun unlikePost(postId: String, userId: String) {
        try {
            val postRef =
                firestore.collection("users").document(userId).collection("posts").document(postId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentLikes = snapshot.getLong("likeCount") ?: 0
                transaction.update(postRef, "likeCount", (currentLikes - 1).coerceAtLeast(0))
                val likesList = snapshot.get("likes") as? List<String> ?: emptyList()
                transaction.update(postRef, "likes", likesList - auth.currentUser?.uid)
            }.await()
        } catch (e: Exception) {
        }
    }

    suspend fun getFollowedUserIds(currentUserId: String): List<String> {
        val document = firestore.collection("users").document(currentUserId).get().await()
        val user = document.toObject(UserEntity::class.java)
        return user?.following ?: emptyList()
    }

    suspend fun getFollowedUsersPosts(userIds: List<String>): Flow<List<PostEntity>> = flow {
        if (userIds.isEmpty()) {
            emit(emptyList<PostEntity>())
            return@flow
        }

        val postsSnapshot = firestore.collection("posts")
            .whereIn("userId", userIds)
            .get()
            .await()

        val userPosts = postsSnapshot.toObjects(PostEntity::class.java)
        emit(userPosts)
    }

    suspend fun getFollowers(userId: String): List<UserEntity> {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            val followersIds = document.get("followers") as? List<String> ?: emptyList()

            val followers = mutableListOf<UserEntity>()
            for (followerId in followersIds) {
                val followerDocument =
                    firestore.collection("users").document(followerId).get().await()
                followerDocument.toObject(UserEntity::class.java)?.let {
                    followers.add(it)
                }
            }
            followers
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getFollowing(userId: String): List<UserEntity> {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            val followingIds = document.get("following") as? List<String> ?: emptyList()

            val following = mutableListOf<UserEntity>()
            for (followingId in followingIds) {
                val followingDocument =
                    firestore.collection("users").document(followingId).get().await()
                followingDocument.toObject(UserEntity::class.java)?.let {
                    following.add(it)
                }
            }
            following
        } catch (e: Exception) {
            emptyList()
        }
    }
}
