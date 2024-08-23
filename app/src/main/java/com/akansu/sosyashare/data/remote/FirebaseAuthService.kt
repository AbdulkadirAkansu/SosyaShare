package com.akansu.sosyashare.data.remote

import android.util.Log
import com.akansu.sosyashare.data.model.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    suspend fun registerUser(email: String, password: String, username: String): UserEntity {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val userId = authResult.user?.uid ?: throw Exception("User ID is null")
        val userMap = mapOf(
            "username" to username,
            "email" to email,
            "isEmailVerified" to false,
            "following" to emptyList<String>(),
            "followers" to emptyList<String>()
        )
        firestore.collection("users").document(userId).set(userMap).await()
        return UserEntity(id = userId, username = username, email = email)
    }

    suspend fun getUserDetails(userId: String): UserEntity? {
        val document = firestore.collection("users").document(userId).get().await()
        return document.toObject(UserEntity::class.java)?.copy(id = userId)
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

    suspend fun loginUser(email: String, password: String): UserEntity? {
        auth.signInWithEmailAndPassword(email, password).await()
        val currentUser = auth.currentUser ?: throw Exception("User not found")
        val userId = currentUser.uid
        val document = firestore.collection("users").document(userId).get().await()
        return document.toObject(UserEntity::class.java)?.copy(id = userId)
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

    suspend fun syncAllUsers(): List<UserEntity> {
        val result = firestore.collection("users").get().await()
        val users = mutableListOf<UserEntity>()
        for (document in result.documents) {
            val user = document.toObject(UserEntity::class.java)?.copy(id = document.id)
            if (user != null) {
                users.add(user)
            }
        }
        return users
    }

}


