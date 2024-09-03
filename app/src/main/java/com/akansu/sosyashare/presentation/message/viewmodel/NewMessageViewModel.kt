package com.akansu.sosyashare.presentation.message.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.Message
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.repository.MessageRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class NewMessageViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentUserProfilePictureUrl = MutableStateFlow<String?>(null)
    val currentUserProfilePictureUrl: StateFlow<String?> = _currentUserProfilePictureUrl

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

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

    fun forwardImageMessage(receiverId: String, imageUrl: String) {
        viewModelScope.launch {
            try {
                val senderId = _currentUserId.value ?: return@launch
                messageRepository.sendImageMessage(senderId, receiverId, Uri.parse(imageUrl))
            } catch (e: Exception) {
                _error.value = "Failed to forward image message: ${e.message}"
            }
        }
    }


    fun forwardMessage(receiverId: String, content: String) {
        viewModelScope.launch {
            try {
                val senderId = _currentUserId.value ?: return@launch
                val message = Message(
                    senderId = senderId,
                    receiverId = receiverId,
                    content = content,
                    timestamp = java.util.Date()
                )
                messageRepository.sendMessage(senderId, receiverId, message)
            } catch (e: Exception) {
                _error.value = "Failed to forward message: ${e.message}"
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
            delay(200)
            _searchResults.value = userRepository.searchUsers(query)
        }
    }
}
