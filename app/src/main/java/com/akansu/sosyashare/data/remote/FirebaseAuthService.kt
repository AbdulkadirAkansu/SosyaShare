package com.akansu.sosyashare.data.remote

import com.akansu.sosyashare.data.local.UserDao
import com.akansu.sosyashare.data.model.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthService @Inject constructor(
    private val userDao: UserDao
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
    }
}


