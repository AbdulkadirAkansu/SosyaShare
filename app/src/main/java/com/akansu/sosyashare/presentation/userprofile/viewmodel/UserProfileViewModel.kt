package com.akansu.sosyashare.presentation.userprofile.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.data.mapper.toDomainModel
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.usecase.UpdateUserProfilePictureUseCase
import com.akansu.sosyashare.domain.usecase.UploadProfilePictureUseCase
import com.akansu.sosyashare.domain.usecase.profile.CheckIfFollowingUseCase
import com.akansu.sosyashare.domain.usecase.profile.FollowUserUseCase
import com.akansu.sosyashare.domain.usecase.profile.GetCurrentUserIdUseCase
import com.akansu.sosyashare.domain.usecase.profile.GetUserDetailsUseCase
import com.akansu.sosyashare.domain.usecase.profile.UnfollowUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val followUserUseCase: FollowUserUseCase,
    private val unfollowUserUseCase: UnfollowUserUseCase,
    private val checkIfFollowingUseCase: CheckIfFollowingUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val uploadProfilePictureUseCase: UploadProfilePictureUseCase,
    private val updateUserProfilePictureUseCase: UpdateUserProfilePictureUseCase
) : ViewModel() {

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> get() = _isFollowing

    private val _profilePictureUrl = MutableStateFlow<String?>(null)
    val profilePictureUrl: StateFlow<String?> get() = _profilePictureUrl

    fun uploadProfilePicture(uri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val url = uploadProfilePictureUseCase(uri) // Uri parametresini alır ve String döner
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
            val userId = getCurrentUserIdUseCase()
            userId?.let {
                updateUserProfilePictureUseCase(it, url) // userId ve url parametrelerini alır
            }
        }
    }

    fun getUserDetails(userId: String, onResult: (User?) -> Unit) {
        viewModelScope.launch {
            val user = getUserDetailsUseCase(userId).firstOrNull()
            onResult(user)
        }
    }

    fun followUser(currentUserId: String, followUserId: String) {
        viewModelScope.launch {
            followUserUseCase(currentUserId, followUserId)
            checkIfFollowing(currentUserId, followUserId)
        }
    }

    fun unfollowUser(currentUserId: String, unfollowUserId: String) {
        viewModelScope.launch {
            unfollowUserUseCase(currentUserId, unfollowUserId)
            checkIfFollowing(currentUserId, unfollowUserId)
        }
    }

    private fun checkIfFollowing(currentUserId: String, userId: String) {
        viewModelScope.launch {
            val isFollowing = checkIfFollowingUseCase(currentUserId, userId)
            _isFollowing.value = isFollowing
        }
    }

    fun getCurrentUserId(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val userId = getCurrentUserIdUseCase()
            onResult(userId)
        }
    }

    fun updateUserProfilePictureUrl(newUrl: String) {
        _profilePictureUrl.value = newUrl
    }
}
