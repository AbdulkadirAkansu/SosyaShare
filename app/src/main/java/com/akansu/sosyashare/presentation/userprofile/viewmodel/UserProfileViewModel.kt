package com.akansu.sosyashare.presentation.userprofile.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.repository.UserRepository
import com.akansu.sosyashare.domain.repository.StorageRepository
import com.akansu.sosyashare.util.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> get() = _isFollowing

    private val _profilePictureUrl = MutableStateFlow<String?>(null)
    val profilePictureUrl: StateFlow<String?> get() = _profilePictureUrl

    fun uploadProfilePicture(file: File, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val url = storageRepository.uploadProfilePicture(file)
                _profilePictureUrl.value = url
                saveProfilePictureUrlToDatabase(url)
                onSuccess(url)
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }


    private fun saveProfilePictureUrlToDatabase(url: String) {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId()
            userId?.let {
                userRepository.updateUserProfilePicture(it, url)
            }
        }
    }

    fun getUserDetails(userId: String, onResult: (User?) -> Unit) {
        viewModelScope.launch {
            val user = userRepository.getUserById(userId).firstOrNull()
            onResult(user)
        }
    }

    fun followUser(currentUserId: String, followUserId: String) {
        viewModelScope.launch {
            userRepository.followUser(currentUserId, followUserId)
            checkIfFollowing(currentUserId, followUserId)
        }
    }

    fun unfollowUser(currentUserId: String, unfollowUserId: String) {
        viewModelScope.launch {
            userRepository.unfollowUser(currentUserId, unfollowUserId)
            checkIfFollowing(currentUserId, unfollowUserId)
        }
    }

    private fun checkIfFollowing(currentUserId: String, userId: String) {
        viewModelScope.launch {
            val isFollowing = userRepository.checkIfFollowing(currentUserId, userId)
            _isFollowing.value = isFollowing
        }
    }

    fun getCurrentUserId(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId()
            onResult(userId)
        }
    }

    fun updateUserProfilePictureUrl(newUrl: String) {
        _profilePictureUrl.value = newUrl
    }

    suspend fun getUserById(userId: String): User? {
        return userRepository.getUserById(userId).firstOrNull()
    }
}
