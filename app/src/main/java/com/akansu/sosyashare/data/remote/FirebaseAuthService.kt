package com.akansu.sosyashare.data.remote

import android.system.Os.remove
import android.util.Log
import com.akansu.sosyashare.data.local.UserDao
import com.akansu.sosyashare.data.model.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthService @Inject constructor(
    private val userDao: UserDao,
    private val firebaseStorage: FirebaseStorage
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun registerUser(email: String, password: String, username: String) {
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
    }

    suspend fun getUserDetails(userId: String): UserEntity? {
        val document = firestore.collection("users").document(userId).get().await()
        return document.toObject(UserEntity::class.java)?.copy(id = userId)
    }

    suspend fun updateUsername(userId: String, username: String) {
        firestore.collection("users").document(userId).update("username", username).await()
    }

    suspend fun updateBio(userId: String, bio: String) {
        firestore.collection("users").document(userId).update("bio", bio).await()
    }

    suspend fun updateUserProfilePicture(userId: String, url: String) {
        firestore.collection("users").document(userId).update("profilePictureUrl", url).await()
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

    suspend fun sendEmailVerification() {
        val user = auth.currentUser ?: throw Exception("No authenticated user")
        user.sendEmailVerification().await()
    }

    suspend fun isUsernameUnique(username: String): Boolean {
        val user = getUserByUsername(username)
        return user == null
    }

    private suspend fun getUserByUsername(username: String): Map<String, Any>? {
        val querySnapshot = firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .await()
        return if (!querySnapshot.isEmpty) {
            querySnapshot.documents.first().data
        } else {
            null
        }
    }

    suspend fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
        val currentUser = auth.currentUser ?: throw Exception("User not found")
        val userId = currentUser.uid
        val document = firestore.collection("users").document(userId).get().await()
        val userEntity = document.toObject(UserEntity::class.java)?.copy(id = userId)
        userEntity?.let {
            userDao.insertUser(it)
        }
    }

    suspend fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    suspend fun reloadUser() {
        val user = auth.currentUser ?: throw Exception("No authenticated user")
        user.reload().await()
    }

    fun getCurrentUser() = auth.currentUser

    fun logoutUser() {
        auth.signOut()
    }

    suspend fun followUser(currentUserId: String, followUserId: String) {
        val currentUserRef = firestore.collection("users").document(currentUserId)
        val followUserRef = firestore.collection("users").document(followUserId)

        firestore.runTransaction { transaction ->
            val currentUserSnapshot = transaction.get(currentUserRef)
            val followUserSnapshot = transaction.get(followUserRef)

            val currentUserFollowing = currentUserSnapshot.get("following") as? MutableList<String> ?: mutableListOf()
            val followUserFollowers = followUserSnapshot.get("followers") as? MutableList<String> ?: mutableListOf()

            if (!currentUserFollowing.contains(followUserId)) {
                currentUserFollowing.add(followUserId)
            }
            if (!followUserFollowers.contains(currentUserId)) {
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

    suspend fun deletePost(userId: String, postId: String, postImageUrl: String) {
        // Gönderiyi Firestore'dan silme
        val postRef = firestore.collection("users").document(userId).collection("posts").document(postId)
        postRef.delete().await()

        // Gönderiye ait resmi Firebase Storage'dan silme
        if (postImageUrl.isNotEmpty()) {
            val storageRef = firebaseStorage.getReferenceFromUrl(postImageUrl)
            storageRef.delete().await()
        }
        Log.d("FirebaseUserService", "Deleted post with postId: $postId for userId: $userId")
    }

}


