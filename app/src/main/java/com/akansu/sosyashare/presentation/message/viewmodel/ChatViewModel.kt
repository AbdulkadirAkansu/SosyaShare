package com.akansu.sosyashare.presentation.message.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.Message
import com.akansu.sosyashare.domain.repository.MessageRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var chatId: String? = null

    init {
        viewModelScope.launch {
            _currentUserId.value = userRepository.getCurrentUserId()
        }
    }

    fun loadMessages(otherUserId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = _currentUserId.value ?: throw Exception("User ID not found")
                chatId = getChatId(currentUserId, otherUserId)
                _messages.value = messageRepository.getMessagesByChatId(chatId!!)
            } catch (e: Exception) {
                _error.value = "Failed to load messages: ${e.message}"
            }
        }
    }

    fun sendMessage(receiverId: String, content: String) {
        viewModelScope.launch {
            try {
                val currentUserId = _currentUserId.value ?: return@launch
                val message = Message(
                    senderId = currentUserId,
                    receiverId = receiverId,
                    content = content,
                    timestamp = Date()
                )
                messageRepository.sendMessage(currentUserId, receiverId, message)
                loadMessages(receiverId)
            } catch (e: Exception) {
                _error.value = "Failed to send message: ${e.message}"
            }
        }
    }

    private fun getChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) {
            "$userId1-$userId2"
        } else {
            "$userId2-$userId1"
        }
    }
}
