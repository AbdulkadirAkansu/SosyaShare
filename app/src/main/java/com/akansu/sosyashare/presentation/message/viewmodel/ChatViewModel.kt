package com.akansu.sosyashare.presentation.message.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.akansu.sosyashare.domain.model.Message
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.repository.MessageRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
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
            _currentUser.value =
                _currentUserId.value?.let { userRepository.getUserById(it).firstOrNull() }
        }
    }

    fun getChatId(): String? {
        return chatId
    }

    fun deleteAllMessages() {
        viewModelScope.launch {
            val currentChatId = chatId
            Log.d("ChatViewModel", "Attempting to delete all messages for chatId: $currentChatId")

            if (currentChatId == null) {
                Log.e("ChatViewModel", "chatId is null. Cannot delete messages.")
                _error.value = "Failed to delete messages: chatId is null"
                return@launch
            }

            try {
                withContext(NonCancellable) {
                    messageRepository.deleteAllMessages(currentChatId)
                    Log.d(
                        "ChatViewModel",
                        "Successfully deleted all messages for chatId: $currentChatId"
                    )
                    _messages.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error deleting messages for chatId: $currentChatId", e)
                _error.value = "Failed to delete messages: ${e.message}"
            }
        }
    }


    fun forwardImageMessage(receiverId: String, originalMessage: Message) {
        viewModelScope.launch {
            try {
                val senderId = _currentUserId.value ?: return@launch
                if (originalMessage.content.startsWith("http") && originalMessage.content.contains("firebase")) {
                    messageRepository.sendImageMessage(
                        senderId,
                        receiverId,
                        Uri.parse(originalMessage.content)
                    )
                } else {
                    messageRepository.forwardMessage(senderId, receiverId, originalMessage)
                }
            } catch (e: Exception) {
                _error.value = "Failed to forward image message: ${e.message}"
            }
        }
    }


    suspend fun saveImageToGallery(imageUrl: String, context: Context) {
        val directory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val fileName = "${System.currentTimeMillis()}.jpg"
        val file = File(directory, fileName)

        try {
            val outputStream = withContext(Dispatchers.IO) {
                FileOutputStream(file)
            }
            val bitmap = ImageLoader(context).execute(
                ImageRequest.Builder(context)
                    .data(imageUrl)
                    .build()
            ).drawable?.toBitmap()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            // Notify gallery about new image
            val mediaScanIntent =
                android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = android.net.Uri.fromFile(file)
            context.sendBroadcast(mediaScanIntent)

            Toast.makeText(context, "Resim galeriye kaydedildi", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Resim kaydedilemedi", Toast.LENGTH_SHORT).show()
        }
    }


    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            try {
                val chatId = chatId ?: return@launch
                val currentUserId = _currentUserId.value ?: return@launch
                messageRepository.deleteMessage(chatId, messageId, currentUserId)
            } catch (e: Exception) {
                _error.value = "Failed to delete message: ${e.message}"
            }
        }
    }

    fun forwardMessage(receiverId: String, originalMessage: Message) {
        viewModelScope.launch {
            try {
                val senderId = _currentUserId.value ?: return@launch
                messageRepository.forwardMessage(senderId, receiverId, originalMessage)
            } catch (e: Exception) {
                _error.value = "Failed to forward message: ${e.message}"
            }
        }
    }

    fun replyToMessage(receiverId: String, originalMessage: Message, replyContent: String) {
        viewModelScope.launch {
            try {
                val senderId = _currentUserId.value ?: return@launch
                messageRepository.replyToMessage(
                    senderId,
                    receiverId,
                    originalMessage,
                    replyContent
                )
            } catch (e: Exception) {
                _error.value = "Failed to reply to message: ${e.message}"
            }
        }
    }

    fun listenForMessages(otherUserId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = _currentUserId.value ?: throw Exception("User ID not found")
                chatId = getChatId(currentUserId, otherUserId)
                _otherUser.value = userRepository.getUserById(otherUserId).firstOrNull()

                messageRepository.listenForMessages(chatId!!) { newMessages ->
                    _messages.value = newMessages
                    viewModelScope.launch {
                        markMessagesAsRead(newMessages, currentUserId)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to listen for messages: ${e.message}"
            }
        }
    }


    fun sendImageMessage(receiverId: String, imageUri: Uri) {
        viewModelScope.launch {
            try {
                val senderId = _currentUserId.value ?: return@launch
                messageRepository.sendImageMessage(senderId, receiverId, imageUri)
            } catch (e: Exception) {
                _error.value = "Failed to send image message: ${e.message}"
            }
        }
    }


    private suspend fun markMessagesAsRead(messages: List<Message>, currentUserId: String) {
        val unreadMessages = messages.filter { !it.isRead && it.receiverId == currentUserId }
        unreadMessages.forEach { message ->
            messageRepository.updateMessageReadStatus(chatId!!, message.id, true)
        }
    }

    fun sendMessage(receiverId: String, content: String) {
        viewModelScope.launch {
            try {
                val currentUserId = _currentUserId.value ?: return@launch
                Log.d(
                    "SendMessage",
                    "Sending message from $currentUserId to $receiverId with content: $content"
                )

                val message = Message(
                    senderId = currentUserId,
                    receiverId = receiverId,
                    content = content,
                    timestamp = java.util.Date()
                )

                messageRepository.sendMessage(currentUserId, receiverId, message)
                Log.d("SendMessage", "Message successfully sent to $receiverId")
                loadMessages(receiverId)
            } catch (e: Exception) {
                Log.e("SendMessage", "Failed to send message: ${e.message}")
                _error.value = "Failed to send message: ${e.message}"
            }
        }
    }


    fun loadMessages(otherUserId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = _currentUserId.value ?: throw Exception("User ID not found")
                chatId = getChatId(currentUserId, otherUserId)
                val messages = messageRepository.getMessagesByChatId(chatId!!)
                _otherUser.value = userRepository.getUserById(otherUserId).firstOrNull()
                markMessagesAsRead(messages, currentUserId)
                _messages.value = messages
            } catch (e: Exception) {
                _error.value = "Failed to load messages: ${e.message}"
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
