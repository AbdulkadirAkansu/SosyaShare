package com.akansu.sosyashare.data.repository

import com.akansu.sosyashare.data.mapper.toDomainModel
import com.akansu.sosyashare.data.mapper.toEntityModel
import com.akansu.sosyashare.data.remote.FirebaseMessageService
import com.akansu.sosyashare.domain.model.Message
import com.akansu.sosyashare.domain.repository.MessageRepository
import java.util.Date
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageService: FirebaseMessageService
) : MessageRepository {

    override suspend fun sendMessage(senderId: String, receiverId: String, message: Message) {
        messageService.sendMessage(senderId, receiverId, message.toEntityModel())
    }

    override suspend fun getMessagesByChatId(chatId: String): List<Message> {
        return messageService.getMessagesByChatId(chatId).map { it.toDomainModel() }
    }

    override suspend fun getRecentChats(userId: String): List<Message> {
        val chatDocuments = messageService.getRecentChats(userId)
        return chatDocuments.map {
            Message(
                content = it["lastMessage"] as String,
                timestamp = it["updatedAt"] as Date,
                // Katılımcılardan currentUserId dışındaki kullanıcıyı belirleyip atamak gerekli
                senderId = it["participants"].let { participants ->
                    (participants as List<String>).find { id -> id != userId } ?: ""
                }
            )
        }
    }

    override suspend fun updateMessageReadStatus(chatId: String, messageId: String, isRead: Boolean) {
        messageService.updateMessageReadStatus(chatId, messageId, isRead)
    }
}
