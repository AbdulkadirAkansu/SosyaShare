package com.akansu.sosyashare.presentation.message.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.data.remote.FirebaseMessageService
import com.akansu.sosyashare.domain.model.Message
import com.akansu.sosyashare.domain.repository.MessageRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val firebaseMessageService: FirebaseMessageService
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var chatId: String? = null

    private val _recentMessagesUpdated = MutableStateFlow(false)
    val recentMessagesUpdated: StateFlow<Boolean> = _recentMessagesUpdated

    init {
        viewModelScope.launch {
            _currentUserId.value = userRepository.getCurrentUserId()
            Log.d("ChatViewModel", "Current User ID: ${_currentUserId.value}")
        }
    }

    fun loadMessages(otherUserId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = _currentUserId.value ?: throw Exception("User ID not found")
                chatId = getChatId(currentUserId, otherUserId)
                val messages = messageRepository.getMessagesByChatId(chatId!!)

                // Mesajları okundu olarak işaretle
                val unreadMessages = messages.filter { !it.isRead && it.receiverId == currentUserId }
                unreadMessages.forEach { message ->
                    messageRepository.updateMessageReadStatus(chatId!!, message.id, true)
                }

                // Okundu olarak işaretlenmiş mesajları yeniden yükle
                val updatedMessages = messageRepository.getMessagesByChatId(chatId!!)
                _messages.value = updatedMessages

                // Mesajlar güncellenmişse, MessageScreen'in verilerini yenilemek için recent chats'i yeniden yükle
                _recentMessagesUpdated.value = true

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
                    timestamp = java.util.Date()
                )
                messageRepository.sendMessage(currentUserId, receiverId, message)
                Log.d("ChatViewModel", "Message sent: $message")
                loadMessages(receiverId)
            } catch (e: Exception) {
                _error.value = "Failed to send message: ${e.message}"
                Log.e("ChatViewModel", "Error sending message: ${e.message}")
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
