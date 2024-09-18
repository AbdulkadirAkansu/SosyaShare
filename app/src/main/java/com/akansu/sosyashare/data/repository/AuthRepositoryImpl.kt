package com.akansu.sosyashare.data.repository

import com.akansu.sosyashare.data.model.UserEntity
import com.akansu.sosyashare.data.remote.FirebaseAuthService
import com.akansu.sosyashare.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authService: FirebaseAuthService
) : AuthRepository {

    override suspend fun registerUser(email: String, password: String, username: String) {
        authService.registerUser(email, password, username)
    }

    override suspend fun loginUser(email: String, password: String) {
        authService.loginUser(email, password)
    }

    override suspend fun resetPassword(email: String) {
        authService.resetPassword(email)
    }

    override suspend fun sendEmailVerification() {
        authService.sendEmailVerification()
    }

    override suspend fun reloadUser() {
        authService.reloadUser()
    }

    override suspend fun getUserDetails(): UserEntity? {
        return authService.getUserDetails()
    }

    override suspend fun updateEmailVerifiedStatus(userId: String) {
        authService.updateEmailVerifiedStatus(userId)
    }

    override suspend fun isUsernameUnique(username: String): Boolean {
        return authService.isUsernameUnique(username)
    }

    override suspend fun getCurrentUserProfilePictureUrl(): String? {
        val currentUser = authService.getCurrentUser()
        return currentUser?.let {
            val userDetails = authService.getUserDetails()
            userDetails?.profilePictureUrl
        }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return authService.getCurrentUser()
    }

    override fun logoutUser() {
        authService.logoutUser()
    }

    override suspend fun syncAllUsers() {
        authService.syncAllUsers()
    }
}
