package com.akansu.sosyashare.presentation.message.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.Message
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.repository.MessageRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // _recentMessages state değişkeni
    private val _recentMessages = MutableStateFlow<List<Message>>(emptyList())
    val recentMessages: StateFlow<List<Message>> = _recentMessages

    // Eğer hata yakalamak istiyorsanız, _error değişkeni
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    suspend fun getCurrentUserId(): String? {
        return userRepository.getCurrentUserId()
    }

    fun loadRecentMessages() {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            userId?.let {
                try {
                    val messages = messageRepository.getRecentMessages(it)
                    if (messages.isEmpty()) {
                        _error.value = "No messages found."
                    } else {
                        _recentMessages.value = messages
                            .groupBy { message ->
                                if (message.senderId == userId) message.receiverId else message.senderId
                            }
                            .mapValues { entry -> entry.value.maxByOrNull { it.timestamp } }
                            .values
                            .filterNotNull()
                            .sortedByDescending { it.timestamp }
                            .toList()
                    }
                } catch (e: Exception) {
                    _error.value = "Failed to load messages: ${e.message}"
                }
            }
        }
    }

    fun updateMessageListAfterNewChat(userId: String) {
        loadRecentMessages()
    }

    suspend fun getUserById(userId: String): User? {
        return userRepository.getUserById(userId).firstOrNull()
    }
}
