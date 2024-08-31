package com.akansu.sosyashare.presentation.message.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewMessageViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentUserProfilePictureUrl = MutableStateFlow<String?>(null)
    val currentUserProfilePictureUrl: StateFlow<String?> = _currentUserProfilePictureUrl

    private var searchJob: Job? = null

    init {
        loadCurrentUserProfilePicture()
    }

    private fun loadCurrentUserProfilePicture() {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId()
            if (userId != null) {
                val user = userRepository.getUserById(userId).firstOrNull()
                _currentUserProfilePictureUrl.value = user?.profilePictureUrl
            }
        }
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            delay(200)  // Kısa bir gecikme ekliyoruz (200ms)
            _searchResults.value = userRepository.searchUsers(query)
        }
    }
}
