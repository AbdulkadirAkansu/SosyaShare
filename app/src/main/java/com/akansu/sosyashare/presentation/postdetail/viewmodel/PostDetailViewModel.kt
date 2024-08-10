package com.akansu.sosyashare.presentation.postdetail.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.repository.PostRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _profilePictureUrl = MutableStateFlow<String?>(null)
    val profilePictureUrl: StateFlow<String?> = _profilePictureUrl

    fun loadUserDetails(userId: String) {
        viewModelScope.launch {
            try {
                val userDetails = userRepository.getUserById(userId).firstOrNull()
                _user.value = userDetails
                _profilePictureUrl.value = userDetails?.profilePictureUrl

                val userPosts = postRepository.getPostsByUser(userId).firstOrNull() ?: emptyList()
                _posts.value = userPosts.map { post ->
                    val isLiked = post.likedBy.contains(userRepository.getCurrentUserId())
                    post.copy(isLiked = isLiked)
                }

                Log.d("PostDetailViewModel", "User details loaded: $userDetails")
                Log.d("PostDetailViewModel", "Posts loaded: $userPosts")
            } catch (e: Exception) {
                Log.e("PostDetailViewModel", "Error loading user details", e)
            }
        }
    }

    fun likePost(postId: String) {
        viewModelScope.launch {
            val currentUserId = userRepository.getCurrentUserId() ?: return@launch
            postRepository.likePost(postId, currentUserId)
            updatePostLikeStatus(postId, true)
        }
    }

    fun unlikePost(postId: String) {
        viewModelScope.launch {
            val currentUserId = userRepository.getCurrentUserId() ?: return@launch
            postRepository.unlikePost(postId, currentUserId)
            updatePostLikeStatus(postId, false)
        }
    }

    private fun updatePostLikeStatus(postId: String, isLiked: Boolean) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(isLiked = isLiked, likeCount = if (isLiked) post.likeCount + 1 else post.likeCount - 1)
            } else post
        }
    }
}
