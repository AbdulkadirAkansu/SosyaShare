package com.akansu.sosyashare.data.repository

import android.util.Log
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
        Log.d("MessageRepositoryImpl", "sendMessage - Sending message: $message")
        messageService.sendMessage(senderId, receiverId, message.toEntityModel())
    }

    override suspend fun getMessagesByChatId(chatId: String): List<Message> {
        val messages = messageService.getMessagesByChatId(chatId).map { it.toDomainModel() }
        Log.d("MessageRepositoryImpl", "getMessagesByChatId - Messages: $messages")
        return messages
    }

    override suspend fun getRecentChats(userId: String): List<Message> {
        val chatMessages = messageService.getRecentChats(userId)
        Log.d("MessageRepositoryImpl", "getRecentChats - Messages: $chatMessages")
        return chatMessages
    }

    override suspend fun updateMessageReadStatus(chatId: String, messageId: String, isRead: Boolean) {
        Log.d("MessageRepositoryImpl", "updateMessageReadStatus - Updating read status for message: $messageId in chat: $chatId")
        messageService.updateMessageReadStatus(chatId, messageId, isRead)
    }
}
