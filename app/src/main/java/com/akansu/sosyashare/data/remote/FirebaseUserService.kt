package com.akansu.sosyashare.data.remote

import android.net.Uri
import android.util.Log
import com.akansu.sosyashare.data.local.UserDao
import com.akansu.sosyashare.data.model.PostEntity
import com.akansu.sosyashare.data.model.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseUserService @Inject constructor(
    private val userDao: UserDao,
    private val firebaseStorage: FirebaseStorage
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun registerUser(email: String, password: String, username: String): UserEntity {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val userId = authResult.user?.uid ?: throw Exception("User ID is null")
        val userMap = mapOf(
            "username" to username,
            "email" to email,
            "isEmailVerified" to false,
            "following" to emptyList<String>(),
            "followers" to emptyList<String>(),
            "posts" to emptyList<String>()
        )
        firestore.collection("users").document(userId).set(userMap).await()
        val userEntity = UserEntity(id = userId, username = username, email = email, profilePictureUrl = null)
        userDao.insertUser(userEntity)
        return userEntity
    }

    suspend fun getUserDetails(userId: String): UserEntity? {
        val document = firestore.collection("users").document(userId).get().await()
        Log.d("FirebaseUserService", "Fetched user details for userId: $userId")
        return document.toObject(UserEntity::class.java)?.copy(id = userId)
    }

    suspend fun updateUsername(userId: String, username: String) {
        firestore.collection("users").document(userId).update("username", username).await()
    }

    suspend fun updateBio(userId: String, bio: String) {
        firestore.collection("users").document(userId).update("bio", bio).await()
    }

    suspend fun updateUserProfilePicture(userId: String, profilePictureUrl: String) {
        firestore.collection("users").document(userId)
            .update("profilePictureUrl", profilePictureUrl)
            .await()
    }

    suspend fun updateUserPosts(userId: String, posts: List<String>) {
        firestore.collection("users").document(userId).update("posts", posts).await()
    }

    suspend fun searchUsers(query: String): List<UserEntity> {
        val users = mutableListOf<UserEntity>()
        val result = firestore.collection("users")
            .whereGreaterThanOrEqualTo("username", query)
            .whereLessThanOrEqualTo("username", query + "\uf8ff")
            .get()
            .await()

        for (document in result.documents) {
            val user = document.toObject(UserEntity::class.java)?.copy(id = document.id)
            if (user != null) {
                users.add(user)
            }
        }
        return users
    }

    suspend fun followUser(currentUserId: String, followUserId: String) {
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
    }

    suspend fun unfollowUser(currentUserId: String, unfollowUserId: String) {
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
    }

    suspend fun syncAllUsers() {
        val result = firestore.collection("users").get().await()
        for (document in result.documents) {
            val user = document.toObject(UserEntity::class.java)?.copy(id = document.id)
            if (user != null) {
                userDao.insertUser(user)
            }
        }
    }

    suspend fun deletePost(userId: String, postUrl: String) {
        val userDoc = firestore.collection("users").document(userId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userDoc)
            val user = snapshot.toObject(UserEntity::class.java)
            user?.let {
                val updatedPosts = it.posts.toMutableList().apply { remove(postUrl) }
                transaction.update(userDoc, "posts", updatedPosts)
            }
        }.await()
        if (postUrl.isNotEmpty()) {
            val storageRef = firebaseStorage.getReferenceFromUrl(postUrl)
            storageRef.delete().await()
        }
        Log.d("FirebaseUserService", "Deleted post with postUrl: $postUrl for userId: $userId")
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
            val postRef = firestore.collection("users").document(userId).collection("posts").document(postId)
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
            val postRef = firestore.collection("users").document(userId).collection("posts").document(postId)
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

    fun getFollowedUsersPosts(userIds: List<String>): Flow<List<PostEntity>> = flow {
        val posts = mutableListOf<PostEntity>()
        for (userId in userIds) {
            Log.d("FirebaseUserService", "Fetching posts for userId: $userId")
            val document = firestore.collection("users").document(userId).get().await()
            val user = document.toObject(UserEntity::class.java)?.copy(id = userId)
            user?.posts?.forEach { postUrl ->
                posts.add(PostEntity(userId = userId, imageUrl = postUrl, content = ""))
            }
        }
        Log.d("FirebaseUserService", "All posts fetched: $posts")
        emit(posts)
    }


}
