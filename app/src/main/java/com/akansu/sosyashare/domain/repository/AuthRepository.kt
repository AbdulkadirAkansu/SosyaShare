package com.akansu.sosyashare.domain.repository

import com.akansu.sosyashare.data.model.UserEntity
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    suspend fun registerUser(email: String, password: String, username: String)
    suspend fun loginUser(email: String, password: String)
    suspend fun resetPassword(email: String)
    suspend fun sendEmailVerification()
    suspend fun reloadUser()
    suspend fun getUserDetails(): UserEntity?
    suspend fun isUsernameUnique(username: String): Boolean
    suspend fun getCurrentUserProfilePictureUrl(): String?
    fun getCurrentUser(): FirebaseUser?
    fun logoutUser()
    suspend fun syncAllUsers()
    suspend fun updateEmailVerifiedStatus(userId: String)
}
