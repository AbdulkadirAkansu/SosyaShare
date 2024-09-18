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

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages


    init {
        loadCurrentUserIdAndUsername()
    }

    fun listenForMessages(chatId: String) {
        Log.d("MessageViewModel", "Listening for messages in chatId: $chatId")
        messageRepository.listenForMessages(chatId) { newMessages ->
            _messages.value = newMessages  // StateFlow'u güncelle
            Log.d("MessageViewModel", "New messages received: $newMessages")
        }
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
                Log.d("MessageViewModel", "Searching chats for username: $username with userId: $userId")
                val allChats = messageRepository.getRecentChats(userId)
                Log.d("MessageViewModel", "All chats: $allChats")

                val matchingChats = allChats.filter { chat ->
                    val otherUserId = if (chat.senderId == userId) chat.receiverId else chat.senderId
                    val otherUser = userRepository.getUserById(otherUserId).firstOrNull()
                    Log.d("MessageViewModel", "Other user fetched for chat: $otherUser")
                    otherUser?.username?.contains(username, ignoreCase = true) == true
                }
                _searchResults.value = matchingChats
                Log.d("MessageViewModel", "Matching Chats: $matchingChats")
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

    fun deleteMessage(chatId: String, messageId: String) {
        viewModelScope.launch {
            try {
                val userId = _currentUserId.value ?: return@launch
                messageRepository.deleteMessage(chatId, messageId, userId)
                _recentMessages.value = _recentMessages.value.filter { it.id != messageId }
            } catch (e: Exception) {
                _error.value = "Failed to delete message: ${e.message}"
            }
        }
    }

    fun getChatId(user1Id: String, user2Id: String): String {
        return if (user1Id < user2Id) {
            "$user1Id-$user2Id"
        } else {
            "$user2Id-$user1Id"
        }
    }


    fun loadRecentChats() {
        viewModelScope.launch {
            try {
                val userId = _currentUserId.value ?: return@launch
                Log.d("MessageViewModel", "loadRecentChats - Loading recent chats for user ID: $userId")

                val chats = messageRepository.getRecentChats(userId)
                Log.d("MessageViewModel", "loadRecentChats - Loaded recent chats: $chats")

                // Chat'leri yükledikten sonra receiverId'nin boş olup olmadığını kontrol edin.
                chats.forEach { chat ->
                    if (chat.receiverId.isBlank()) {
                        Log.e("MessageViewModel", "loadRecentChats - Chat with id: ${chat.id} has an empty receiverId!")
                    } else {
                        Log.d("MessageViewModel", "loadRecentChats - Chat with id: ${chat.id} has receiverId: ${chat.receiverId}")
                    }
                }

                _recentMessages.value = chats
                _recentMessagesUpdated.value = false

            } catch (e: Exception) {
                _error.value = "Failed to load recent messages: ${e.message}"
                Log.e("MessageViewModel", "loadRecentChats - Error loading recent messages: ${e.message}")
            }
        }
    }


    suspend fun getUserById(userId: String): User? {
        return try {
            if (userId.isBlank()) {
                Log.e("MessageViewModel", "Attempted to fetch user with empty userId!")
                return null
            }
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
