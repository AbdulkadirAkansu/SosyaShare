package com.akansu.sosyashare.presentation.editprofile.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.repository.AuthRepository
import com.akansu.sosyashare.domain.repository.PostRepository
import com.akansu.sosyashare.domain.repository.StorageRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> get() = _user

    private val _profilePictureUrl = MutableStateFlow<String?>(null)
    val profilePictureUrl: StateFlow<String?> get() = _profilePictureUrl

    private val _username = MutableStateFlow<String>("")
    val username: StateFlow<String> get() = _username

    private val _bio = MutableStateFlow<String>("")
    val bio: StateFlow<String> get() = _bio

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> get() = _posts

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> get() = _successMessage

    private val _canChangeUsername = MutableStateFlow(true)
    val canChangeUsername: StateFlow<Boolean> get() = _canChangeUsername

    private val _backgroundImageUrl = MutableStateFlow<String?>(null)
    val backgroundImageUrl: StateFlow<String?> get() = _backgroundImageUrl

    private var initialUsername = ""
    private var initialBio = ""

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.uid
            if (userId != null) {
                val user = userRepository.getUserById(userId).firstOrNull()
                _user.value = user
                _profilePictureUrl.value = user?.profilePictureUrl
                _username.value = user?.username ?: ""
                _bio.value = user?.bio ?: ""

                // Postları PostRepository'den alıyoruz
                val userPosts = postRepository.getPostsByUser(userId).firstOrNull() ?: emptyList()
                _posts.value = userPosts

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

    fun uploadProfilePicture(
        file: File,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
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

    fun uploadBackgroundImage(
        file: File,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val url = storageRepository.uploadBackgroundImage(file)
                _backgroundImageUrl.value = url
                saveBackgroundImageUrlToDatabase(url)
                onSuccess(url)
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun updateProfilePictureUrl(newUrl: String) {
        _profilePictureUrl.value = newUrl
        saveProfilePictureUrlToDatabase(newUrl)
    }

    fun updateBackgroundImageUrl(newUrl: String) {
        _backgroundImageUrl.value = newUrl
        saveBackgroundImageUrlToDatabase(newUrl)
    }

    private fun saveProfilePictureUrlToDatabase(url: String) {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId()
            userId?.let {
                userRepository.updateUserProfilePicture(it, url)
            }
        }
    }

    private fun saveBackgroundImageUrlToDatabase(url: String) {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId()
            userId?.let {
                userRepository.updateBackgroundImageUrl(it, url)
            }
        }
    }

    fun updateUsername(newUsername: String) {
        if (newUsername.isBlank()) {
            _errorMessage.value = "Kullanıcı adı boş olamaz."
            return
        }
        if (newUsername.length > 20) {
            _errorMessage.value = "Kullanıcı adı 20 karakterden uzun olamaz."
            return
        }
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.uid
            if (userId != null) {
                try {
                    if (_canChangeUsername.value && newUsername != initialUsername) {
                        userRepository.updateUsernameInFirebase(userId, newUsername)
                        _username.value = newUsername
                        _user.value = _user.value?.copy(username = newUsername)
                        _canChangeUsername.value = false
                        _successMessage.value = "Kullanıcı adı başarıyla güncellendi."
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Kullanıcı adı güncellenemedi: ${e.message}"
                }
            }
        }
    }

    fun updateBio(newBio: String) {
        if (newBio.length > 50) {
            _errorMessage.value = "Biyografi 50 karakterden uzun olamaz."
            return
        }
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.uid
            if (userId != null) {
                try {
                    if (newBio != initialBio) {
                        userRepository.updateBioInFirebase(userId, newBio)
                        _bio.value = newBio
                        _user.value = _user.value?.copy(bio = newBio)
                        _successMessage.value = "Biyografi başarıyla güncellendi."
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Biyografi güncellenemedi: ${e.message}"
                }
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                val post = postRepository.getPostById(postId)
                val imageUrl = post?.imageUrl

                if (imageUrl != null && imageUrl.isNotBlank()) {
                    val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                    storageRef.delete().await()

                    postRepository.deletePost(postId, post.userId)
                    _posts.value = _posts.value.filterNot { it.id == postId }
                    _successMessage.value = "Gönderi başarıyla silindi."
                } else {
                    _errorMessage.value = "Silinecek dosya bulunamadı."
                }
            } catch (e: IllegalArgumentException) {
                _errorMessage.value = "Geçersiz URI: ${e.message}"
            } catch (e: Exception) {
                _errorMessage.value = "Gönderi silinemedi: ${e.message}"
            }
        }
    }
}

