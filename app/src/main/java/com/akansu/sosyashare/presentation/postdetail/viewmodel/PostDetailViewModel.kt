package com.akansu.sosyashare.presentation.postdetail.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.model.User
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
    private val getUserDetailsUseCase: GetUserDetailsUseCase
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

                // User modelindeki posts alanını kullanarak postları yükle
                val postsList = userDetails?.posts?.map { postUrl ->
                    Post(
                        id = "", // ID kullanmıyoruz
                        userId = userDetails.id,
                        content = "", // İçerik yoksa boş bırakıyoruz
                        imageUrl = postUrl,
                        likeCount = 0, // Like sayısı kullanmıyoruz
                        createdAt = Date() // Tarih kullanmıyoruz
                    )
                } ?: emptyList()

                _posts.value = postsList
                Log.d("PostDetailViewModel", "User details loaded: $userDetails")
                Log.d("PostDetailViewModel", "Posts loaded: $postsList")
            } catch (e: Exception) {
                Log.e("PostDetailViewModel", "Error loading user details", e)
            }
        }
    }
}