package com.akansu.sosyashare.presentation.share.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.repository.PostRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import com.akansu.sosyashare.domain.repository.StorageRepository
import com.akansu.sosyashare.util.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _userPosts = MutableStateFlow<List<Post>>(emptyList())
    val userPosts: StateFlow<List<Post>> = _userPosts

    init {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId()
            userId?.let {
                refreshUserPosts(it)
            }
        }
    }

    fun uploadPostPicture(file: File, content: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId() ?: return@launch
            val imageUrl = storageRepository.uploadPostPicture(file)
            val newPost = Post(
                id = generateUniqueId(),
                userId = userId,
                imageUrl = imageUrl,
                content = content,
                createdAt = Date()
            )
            postRepository.createPost(newPost)
            _userPosts.value += newPost
            onSuccess()
        }
    }

    fun refreshUserPosts(userId: String? = null) {
        viewModelScope.launch {
            val id = userId ?: userRepository.getCurrentUserId()
            id?.let {
                postRepository.getPostsByUser(it).collect { posts ->
                    _userPosts.value = posts
                }
            }
        }
    }

    private fun generateUniqueId(): String {
        return java.util.UUID.randomUUID().toString()
    }
}
