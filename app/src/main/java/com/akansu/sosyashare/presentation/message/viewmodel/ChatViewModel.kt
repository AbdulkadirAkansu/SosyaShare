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

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _otherUser = MutableStateFlow<User?>(null)
    val otherUser: StateFlow<User?> = _otherUser

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var chatId: String? = null

    init {
        viewModelScope.launch {
            _currentUserId.value = userRepository.getCurrentUserId()
            _currentUser.value = _currentUserId.value?.let { userRepository.getUserById(it).firstOrNull() }
            Log.d("ChatViewModel", "Current User ID: ${_currentUserId.value}")
            Log.d("ChatViewModel", "Current User: ${_currentUser.value}")
        }
    }


    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            try {
                val chatId = chatId ?: return@launch
                messageRepository.deleteMessage(chatId, messageId)
                Log.d("ChatViewModel", "deleteMessage - Message deleted with ID: $messageId")
            } catch (e: Exception) {
                _error.value = "Failed to delete message: ${e.message}"
                Log.e("ChatViewModel", "deleteMessage - Error deleting message: ${e.message}")
            }
        }
    }

    fun forwardMessage(receiverId: String, originalMessage: Message) {
        viewModelScope.launch {
            try {
                val senderId = _currentUserId.value ?: return@launch
                messageRepository.forwardMessage(senderId, receiverId, originalMessage)
                Log.d("ChatViewModel", "forwardMessage - Message forwarded: $originalMessage")
            } catch (e: Exception) {
                _error.value = "Failed to forward message: ${e.message}"
                Log.e("ChatViewModel", "forwardMessage - Error forwarding message: ${e.message}")
            }
        }
    }

    fun replyToMessage(receiverId: String, originalMessage: Message, replyContent: String) {
        viewModelScope.launch {
            try {
                val senderId = _currentUserId.value ?: return@launch
                messageRepository.replyToMessage(senderId, receiverId, originalMessage, replyContent)
                Log.d("ChatViewModel", "replyToMessage - Replied to message: $originalMessage")
            } catch (e: Exception) {
                _error.value = "Failed to reply to message: ${e.message}"
                Log.e("ChatViewModel", "replyToMessage - Error replying to message: ${e.message}")
            }
        }
    }


    fun listenForMessages(otherUserId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = _currentUserId.value ?: throw Exception("User ID not found")
                Log.d("ChatViewModel", "listenForMessages - Listening for messages in chat with otherUserId: $otherUserId")

                chatId = getChatId(currentUserId, otherUserId)
                Log.d("ChatViewModel", "listenForMessages - Generated chatId: $chatId")

                _otherUser.value = userRepository.getUserById(otherUserId).firstOrNull()
                Log.d("ChatViewModel", "listenForMessages - Other User: ${_otherUser.value}")


                messageRepository.listenForMessages(chatId!!) { newMessages ->
                    _messages.value = newMessages

                    viewModelScope.launch {
                        val unreadMessages = newMessages.filter { !it.isRead && it.receiverId == currentUserId }
                        unreadMessages.forEach { message ->
                            messageRepository.updateMessageReadStatus(chatId!!, message.id, true)
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to listen for messages: ${e.message}"
                Log.e("ChatViewModel", "listenForMessages - Error listening for messages: ${e.message}")
            }
        }
    }


    fun sendMessage(receiverId: String, content: String) {
        viewModelScope.launch {
            try {
                val currentUserId = _currentUserId.value ?: return@launch
                Log.d("ChatViewModel", "sendMessage - Sending message to receiverId: $receiverId with content: $content")

                if (receiverId.isBlank()) {
                    Log.e("ChatViewModel", "sendMessage - Attempted to send a message with an empty receiverId!")
                    return@launch
                }

                val message = Message(
                    senderId = currentUserId,
                    receiverId = receiverId,
                    content = content,
                    timestamp = java.util.Date()
                )

                Log.d("ChatViewModel", "sendMessage - Created Message object: $message")

                messageRepository.sendMessage(currentUserId, receiverId, message)
                Log.d("ChatViewModel", "sendMessage - Message sent: $message")

                loadMessages(receiverId)
            } catch (e: Exception) {
                _error.value = "Failed to send message: ${e.message}"
                Log.e("ChatViewModel", "sendMessage - Error sending message: ${e.message}")
            }
        }
    }

    fun loadMessages(otherUserId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = _currentUserId.value ?: throw Exception("User ID not found")
                Log.d("ChatViewModel", "loadMessages - Loading messages for chat with otherUserId: $otherUserId")

                chatId = getChatId(currentUserId, otherUserId)
                Log.d("ChatViewModel", "loadMessages - Generated chatId: $chatId")

                val messages = messageRepository.getMessagesByChatId(chatId!!)
                Log.d("ChatViewModel", "loadMessages - Fetched messages: $messages")

                _otherUser.value = userRepository.getUserById(otherUserId).firstOrNull()
                Log.d("ChatViewModel", "loadMessages - Other User: ${_otherUser.value}")

                // Mesajları okundu olarak işaretle
                val unreadMessages = messages.filter { !it.isRead && it.receiverId == currentUserId }
                Log.d("ChatViewModel", "loadMessages - Unread messages: $unreadMessages")

                unreadMessages.forEach { message ->
                    messageRepository.updateMessageReadStatus(chatId!!, message.id, true)
                }

                // Okundu olarak işaretlenmiş mesajları yeniden yükle
                val updatedMessages = messageRepository.getMessagesByChatId(chatId!!)
                Log.d("ChatViewModel", "loadMessages - Updated messages after marking as read: $updatedMessages")

                _messages.value = updatedMessages
            } catch (e: Exception) {
                _error.value = "Failed to load messages: ${e.message}"
                Log.e("ChatViewModel", "loadMessages - Error loading messages: ${e.message}")
            }
        }
    }

    private fun getChatId(userId1: String, userId2: String): String {
        val chatId = if (userId1 < userId2) {
            "$userId1-$userId2"
        } else {
            "$userId2-$userId1"
        }
        Log.d("ChatViewModel", "Generated chatId: $chatId")
        return chatId
    }
}
