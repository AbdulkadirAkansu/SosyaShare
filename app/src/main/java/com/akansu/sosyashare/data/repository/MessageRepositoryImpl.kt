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

    override suspend fun deleteMessage(chatId: String, messageId: String) {
        messageService.deleteMessage(chatId, messageId)
    }

    override suspend fun forwardMessage(senderId: String, receiverId: String, originalMessage: Message) {
        messageService.forwardMessage(senderId, receiverId, originalMessage.toEntityModel())
    }

    override suspend fun replyToMessage(senderId: String, receiverId: String, originalMessage: Message, replyContent: String) {
        messageService.replyToMessage(senderId, receiverId, originalMessage.toEntityModel(), replyContent)
    }

    override suspend fun sendMessage(senderId: String, receiverId: String, message: Message) {
        Log.d("MessageRepositoryImpl", "sendMessage - Sending message: $message")
        messageService.sendMessage(senderId, receiverId, message.toEntityModel())
    }

    override fun listenForMessages(chatId: String, onMessagesChanged: (List<Message>) -> Unit) {
        messageService.listenForMessages(chatId) { messageEntities ->
            val messages = messageEntities.map { it.toDomainModel() }
            onMessagesChanged(messages)
        }
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
