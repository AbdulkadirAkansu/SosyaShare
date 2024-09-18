package com.akansu.sosyashare.data.repository

import android.net.Uri
import android.util.Log
import com.akansu.sosyashare.data.mapper.toDomainModel
import com.akansu.sosyashare.data.mapper.toEntityModel
import com.akansu.sosyashare.data.remote.FirebaseMessageService
import com.akansu.sosyashare.domain.model.Message
import com.akansu.sosyashare.domain.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageService: FirebaseMessageService
) : MessageRepository {


    override suspend fun deleteMessage(chatId: String, messageId: String, currentUserId: String) {
        messageService.deleteMessage(chatId, messageId, currentUserId)
    }

    override suspend fun deleteAllMessages(chatId: String) {
        Log.d("MessageRepository", "Deleting all messages for chatId: $chatId")
        messageService.deleteAllMessages(chatId)
        Log.d("MessageRepository", "Successfully deleted all messages for chatId: $chatId")
    }

    override suspend fun sendImageMessage(senderId: String, receiverId: String, imageUri: Uri): String {
        return messageService.sendImageMessage(senderId, receiverId, imageUri)
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
        Log.d("MessageRepositoryImpl", "Listening for messages in chatId: $chatId")
        messageService.listenForMessages(chatId) { messageEntities ->
            val messages = messageEntities.map { it.toDomainModel() }
            Log.d("MessageRepositoryImpl", "Messages received: $messages")
            onMessagesChanged(messages)
        }
    }

    override suspend fun getRecentChats(userId: String): List<Message> {
        Log.d("MessageRepositoryImpl", "Fetching recent chats for userId: $userId")
        val chatMessages = messageService.getRecentChats(userId)
        Log.d("MessageRepositoryImpl", "Recent chats fetched: $chatMessages")
        return chatMessages
    }

    override suspend fun getMessagesByChatId(chatId: String): List<Message> {
        val messages = messageService.getMessagesByChatId(chatId).map { it.toDomainModel() }
        Log.d("MessageRepositoryImpl", "getMessagesByChatId - Messages: $messages")
        return messages
    }

    override suspend fun updateMessageReadStatus(chatId: String, messageId: String, isRead: Boolean) {
        Log.d("MessageRepositoryImpl", "updateMessageReadStatus - Updating read status for message: $messageId in chat: $chatId")
        messageService.updateMessageReadStatus(chatId, messageId, isRead)
    }
}
