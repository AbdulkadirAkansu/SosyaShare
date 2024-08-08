package com.akansu.sosyashare.presentation.editprofile.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.usecase.profile.*
import com.akansu.sosyashare.domain.usecase.UploadProfilePictureUseCase
import com.akansu.sosyashare.domain.usecase.editprofile.UpdateBioUseCase
import com.akansu.sosyashare.domain.usecase.editprofile.UpdateUsernameUseCase
import com.akansu.sosyashare.domain.usecase.share.DeletePostUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val uploadProfilePictureUseCase: UploadProfilePictureUseCase,
    private val updateUsernameUseCase: UpdateUsernameUseCase,
    private val updateBioUseCase: UpdateBioUseCase,
    private val deletePostUseCase: DeletePostUseCase
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> get() = _user

    private val _profilePictureUrl = MutableStateFlow<String?>(null)
    val profilePictureUrl: StateFlow<String?> get() = _profilePictureUrl

    private val _username = MutableStateFlow<String>("")
    val username: StateFlow<String> get() = _username

    private val _bio = MutableStateFlow<String>("")
    val bio: StateFlow<String> get() = _bio

    private val _posts = MutableStateFlow<List<String>>(emptyList())
    val posts: StateFlow<List<String>> get() = _posts

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> get() = _successMessage

    private val _canChangeUsername = MutableStateFlow(true)
    val canChangeUsername: StateFlow<Boolean> get() = _canChangeUsername

    private var initialUsername = ""
    private var initialBio = ""

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            val userId = getCurrentUserIdUseCase()
            if (userId != null) {
                val user = getUserDetailsUseCase(userId).firstOrNull()
                _user.value = user
                _profilePictureUrl.value = user?.profilePictureUrl
                _username.value = user?.username ?: ""
                _bio.value = user?.bio ?: ""
                _posts.value = user?.posts ?: emptyList()
                _canChangeUsername.value = canChangeUsername(user)
                initialUsername = user?.username ?: ""
                initialBio = user?.bio ?: ""
            }
        }
    }

    private fun canChangeUsername(user: User?): Boolean {
        val lastChange = user?.lastUsernameChange
        return lastChange == null || Date().time - lastChange.time > 7 * 24 * 60 * 60 * 1000L
    }

    fun updateProfilePicture(uri: Uri) {
        viewModelScope.launch {
            try {
                val url = uploadProfilePictureUseCase(uri)
                _profilePictureUrl.value = url
                _user.value = _user.value?.copy(profilePictureUrl = url)
                _successMessage.value = "Profile picture updated successfully."
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update profile picture: ${e.message}"
            }
        }
    }

    fun saveChanges(newUsername: String, newBio: String) {
        if (newUsername.isBlank()) {
            _errorMessage.value = "Username cannot be empty."
            return
        }
        if (newUsername.length > 20) {
            _errorMessage.value = "Username cannot exceed 20 characters."
            return
        }
        if (newBio.length > 50) {
            _errorMessage.value = "Bio cannot exceed 50 characters."
            return
        }
        viewModelScope.launch {
            val userId = getCurrentUserIdUseCase()
            if (userId != null) {
                try {
                    if (_canChangeUsername.value && newUsername != initialUsername) {
                        updateUsernameUseCase(userId, newUsername)
                        _username.value = newUsername
                        _user.value = _user.value?.copy(username = newUsername)
                        _canChangeUsername.value = false
                    }
                    if (newBio != initialBio) {
                        updateBioUseCase(userId, newBio)
                        _bio.value = newBio
                        _user.value = _user.value?.copy(bio = newBio)
                    }
                    _successMessage.value = "Profile updated successfully."
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to update profile: ${e.message}"
                }
            }
        }
    }

    fun deletePost(postUrl: String) {
        viewModelScope.launch {
            try {
                deletePostUseCase(postUrl)
                _posts.value = _posts.value.filterNot { it == postUrl }
                _user.value = _user.value?.copy(posts = _posts.value)
                _successMessage.value = "Post deleted successfully."
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete post: ${e.message}"
            }
        }
    }
}
