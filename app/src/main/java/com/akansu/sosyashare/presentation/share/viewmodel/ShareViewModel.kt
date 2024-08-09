package com.akansu.sosyashare.presentation.share.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.Post
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

    private val _userPosts = MutableStateFlow<List<Post>>(emptyList())
    val userPosts: StateFlow<List<Post>> = _userPosts

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
            val newPost = uploadPostPictureUseCase(uri, comment)
            _userPosts.value += newPost
            onSuccess()
        }
    }

    fun refreshUserPosts(userId: String? = null) {
        viewModelScope.launch {
            refreshUserPostsUseCase(userId).collect { posts ->
                _userPosts.value = posts
            }
        }
    }
}
