package com.akansu.sosyashare.presentation.share.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.usecase.share.DeletePostUseCase
import com.akansu.sosyashare.domain.usecase.share.GetCurrentUserIdUseCase
import com.akansu.sosyashare.domain.usecase.share.RefreshUserPostsUseCase
import com.akansu.sosyashare.domain.usecase.share.UploadPostPictureUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val uploadPostPictureUseCase: UploadPostPictureUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val refreshUserPostsUseCase: RefreshUserPostsUseCase
) : ViewModel() {

    private var _cameraImageUri: Uri? = null
    val cameraImageUri: Uri?
        get() = _cameraImageUri

    private val _userPosts = MutableStateFlow<List<String>>(emptyList())
    val userPosts: StateFlow<List<String>> = _userPosts

    init {
        viewModelScope.launch {
            val userId = getCurrentUserIdUseCase()
            userId?.let {
                refreshUserPosts(it)
            }
        }
    }

    fun uploadPostPicture(uri: Uri, comment: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val updatedPosts = uploadPostPictureUseCase(uri, comment)
            _userPosts.value = updatedPosts
            onSuccess()
        }
    }

    fun createImageFileUri(context: Context): Uri {
        val storageDir: File = context.getExternalFilesDir(null) ?: throw IllegalStateException("External storage not available")
        val file = File.createTempFile("IMG_", ".jpg", storageDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    fun setCameraImageUri(uri: Uri) {
        _cameraImageUri = uri
    }

    fun deletePost(postUrl: String) {
        viewModelScope.launch {
            deletePostUseCase(postUrl)
            refreshUserPosts()
        }
    }

    fun refreshUserPosts(userId: String? = null) {
        viewModelScope.launch {
            val posts = refreshUserPostsUseCase(userId)
            _userPosts.value = posts
        }
    }
}
