package com.akansu.sosyashare.presentation.trend


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.repository.PostRepository
import com.akansu.sosyashare.domain.repository.UserPrivacyRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrendViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository, // Kullanıcıları da getirebilmek için UserRepository ekleniyor
    private val userPrivacyRepository: UserPrivacyRepository
) : ViewModel() {

    private val _trendingPosts = MutableStateFlow<List<Post>>(emptyList())
    val trendingPosts: StateFlow<List<Post>> = _trendingPosts

    private val _trendingUsers = MutableStateFlow<List<User>>(emptyList())
    val trendingUsers: StateFlow<List<User>> = _trendingUsers

    init {
        loadTrendingPostsAndUsers() // Hem postları hem kullanıcıları yükleyen fonksiyonu çağırıyoruz
    }

    private fun loadTrendingPostsAndUsers() {
        viewModelScope.launch {
            val allPosts = postRepository.getAllPosts().firstOrNull().orEmpty()
            val filteredPosts = allPosts.filter { post ->
                val userPrivacy = userPrivacyRepository.getUserPrivacy(post.userId)
                userPrivacy?.isPrivate == false
            }.sortedByDescending { it.likeCount }

            _trendingPosts.value = filteredPosts

            // Postlara göre kullanıcıları getirip trendingUsers'a ekliyoruz
            val userIds = filteredPosts.map { it.userId }.distinct() // Tekrarlayan kullanıcıları ayıklıyoruz
            val users = userIds.mapNotNull { userId ->
                userRepository.getUserById(userId).firstOrNull()
            }
            _trendingUsers.value = users
        }
    }
}