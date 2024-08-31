package com.akansu.sosyashare.presentation.message.viewmodel

import android.util.Log
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
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _recentMessages = MutableStateFlow<List<Message>>(emptyList())
    val recentMessages: StateFlow<List<Message>> = _recentMessages

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _currentUsername = MutableStateFlow<String?>(null)
    val currentUsername: StateFlow<String?> = _currentUsername

    init {
        loadCurrentUserIdAndUsername()
    }

    private fun loadCurrentUserIdAndUsername() {
        viewModelScope.launch {
            try {
                _currentUserId.value = userRepository.getCurrentUserId()
                _currentUsername.value = userRepository.getCurrentUserName()
            } catch (e: Exception) {
                _error.value = "Failed to get User ID or Username: ${e.message}"
            }
        }
    }

    fun loadRecentChats() {
        viewModelScope.launch {
            try {
                val userId = _currentUserId.value ?: return@launch
                val chats = messageRepository.getRecentChats(userId)
                _recentMessages.value = chats
            } catch (e: Exception) {
                _error.value = "Failed to load recent messages: ${e.message}"

                // Firebase index hatası durumunda loga linki ekleyin
                if (e.message?.contains("FAILED_PRECONDITION") == true) {
                    val projectId = "YOUR_PROJECT_ID"  // Kendi proje ID'nizle değiştirin
                    Log.e(
                        "MessageViewModel",
                        "Firebase index hatası: ${e.message}. Index'i oluşturmak için bu linki takip edin: https://console.firebase.google.com/v1/r/project/$projectId/firestore/indexes?create_composite=Cktwcm9qZWN0cy9zb3N5YXNoYXJlL2RhdGFiYXNlcy8oZGVmYXVsdCkvY29sbGVjdGlvbkdyb3Vwcy9tZXNzYWdlcy9pbmRleGVzL18QARoMCghzZW5kZXJJZBABGg0KCXRpbWVzdGFtcBACGgwKCF9fbmFtZV9fEAI"
                    )
                }
            }
        }
    }

    fun sendMessage(receiverId: String, content: String) {
        viewModelScope.launch {
            try {
                val senderId = _currentUserId.value ?: return@launch
                val message = Message(
                    senderId = senderId,
                    receiverId = receiverId,
                    content = content,
                    timestamp = Date()
                )
                messageRepository.sendMessage(senderId, receiverId, message)
                loadRecentChats() // Mesaj gönderildikten sonra güncel sohbetleri yükle
            } catch (e: Exception) {
                _error.value = "Failed to send message: ${e.message}"
            }
        }
    }

    suspend fun getUserById(userId: String): User? {
        return try {
            userRepository.getUserById(userId).firstOrNull()
        } catch (e: Exception) {
            _error.value = "Failed to fetch user by id: ${e.message}"
            null
        }
    }
}
