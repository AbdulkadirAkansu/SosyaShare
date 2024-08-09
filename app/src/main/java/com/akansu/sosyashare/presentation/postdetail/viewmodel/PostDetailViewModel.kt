package com.akansu.sosyashare.presentation.postdetail.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.repository.PostRepository
import com.akansu.sosyashare.domain.usecase.profile.GetUserDetailsUseCase
import com.akansu.sosyashare.domain.usecase.postdetail.GetPostDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val getPostDetailsUseCase: GetPostDetailsUseCase, // Postları almak için kullanacağız
    private val postRepository: PostRepository // PostRepository'yi ekleyin
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
                val userDetails = getUserDetailsUseCase(userId).firstOrNull()
                _user.value = userDetails
                _profilePictureUrl.value = userDetails?.profilePictureUrl

                // Kullanıcının postlarını PostRepository üzerinden alın
                val userPosts = postRepository.getPostsByUser(userId).firstOrNull() ?: emptyList()
                _posts.value = userPosts

                Log.d("PostDetailViewModel", "User details loaded: $userDetails")
                Log.d("PostDetailViewModel", "Posts loaded: $userPosts")
            } catch (e: Exception) {
                Log.e("PostDetailViewModel", "Error loading user details", e)
            }
        }
    }
}
