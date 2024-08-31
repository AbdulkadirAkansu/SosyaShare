package com.akansu.sosyashare.presentation.message.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.data.remote.FirebaseMessageService
import com.akansu.sosyashare.domain.model.Message
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.repository.MessageRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val firebaseMessageService: FirebaseMessageService
) : ViewModel() {

    private val _recentMessages = MutableStateFlow<List<Message>>(emptyList())
    val recentMessages: StateFlow<List<Message>> = _recentMessages

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _currentUsername = MutableStateFlow<String?>(null)
    val currentUsername: StateFlow<String?> = _currentUsername

    private val _currentUserProfilePictureUrl = MutableStateFlow<String?>(null)
    val currentUserProfilePictureUrl: StateFlow<String?> = _currentUserProfilePictureUrl

    private val _searchResults = MutableStateFlow<List<Message>>(emptyList())
    val searchResults: StateFlow<List<Message>> = _searchResults

    private val _recentMessagesUpdated = MutableStateFlow(false)
    val recentMessagesUpdated: StateFlow<Boolean> = _recentMessagesUpdated

    init {
        loadCurrentUserIdAndUsername()
    }

    private fun loadCurrentUserIdAndUsername() {
        viewModelScope.launch {
            try {
                _currentUserId.value = userRepository.getCurrentUserId()
                Log.d("MessageViewModel", "Current User ID: ${_currentUserId.value}")
                _currentUsername.value = userRepository.getCurrentUserName()
                loadCurrentUserProfilePicture()
            } catch (e: Exception) {
                _error.value = "Failed to get User ID or Username: ${e.message}"
                Log.e("MessageViewModel", "Error loading User ID and Username: ${e.message}")
            }
        }
    }

    fun searchChatsByUsername(username: String) {
        viewModelScope.launch {
            try {
                val userId = _currentUserId.value ?: return@launch
                Log.d("MessageViewModel", "searchChatsByUsername - Searching chats for username: $username")
                val allChats = messageRepository.getRecentChats(userId)
                val matchingChats = allChats.filter { chat ->
                    val otherUserId = if (chat.senderId == userId) chat.receiverId else chat.senderId
                    val otherUser = userRepository.getUserById(otherUserId).firstOrNull()
                    otherUser?.username?.contains(username, ignoreCase = true) == true
                }
                _searchResults.value = matchingChats
                Log.d("MessageViewModel", "searchChatsByUsername - Matching Chats: $matchingChats")
            } catch (e: Exception) {
                _error.value = "Failed to search chats: ${e.message}"
                Log.e("MessageViewModel", "Error searching chats: ${e.message}")
            }
        }
    }

    private suspend fun loadCurrentUserProfilePicture() {
        _currentUserId.value?.let { userId ->
            val user = userRepository.getUserById(userId).firstOrNull()
            _currentUserProfilePictureUrl.value = user?.profilePictureUrl
            Log.d("MessageViewModel", "Current User Profile Picture URL: ${_currentUserProfilePictureUrl.value}")
        }
    }

    fun loadRecentChats() {
        viewModelScope.launch {
            try {
                val userId = _currentUserId.value ?: return@launch
                Log.d("MessageViewModel", "Loading recent chats for user ID: $userId")
                val chats = messageRepository.getRecentChats(userId)
                _recentMessages.value = chats

                _recentMessagesUpdated.value = false

                Log.d("MessageViewModel", "Loaded recent chats: $chats")
            } catch (e: Exception) {
                _error.value = "Failed to load recent messages: ${e.message}"
                Log.e("MessageViewModel", "Error loading recent messages: ${e.message}")
            }
        }
    }


    fun refreshRecentChats() {
        _recentMessagesUpdated.value = true
    }

    fun updateMessageReadStatus(chatId: String, messageId: String) {
        viewModelScope.launch {
            try {
                Log.d("ViewModel", "Updating message isRead status for message: $messageId in chat: $chatId")
                firebaseMessageService.updateMessageReadStatus(chatId, messageId, true)
                Log.d("ViewModel", "Successfully updated message isRead status for message: $messageId")
            } catch (e: Exception) {
                _error.value = "Failed to update message read status: ${e.message}"
                Log.e("MessageViewModel", "Error updating isRead status for message $messageId", e)
            }
        }
    }


    suspend fun getUserById(userId: String): User? {
        return try {
            val user = userRepository.getUserById(userId).firstOrNull()
            Log.d("MessageViewModel", "Fetched user by ID: $userId - $user")
            user
        } catch (e: Exception) {
            _error.value = "Failed to fetch user by id: ${e.message}"
            Log.e("MessageViewModel", "Error fetching user by ID: ${e.message}")
            null
        }
    }
}
