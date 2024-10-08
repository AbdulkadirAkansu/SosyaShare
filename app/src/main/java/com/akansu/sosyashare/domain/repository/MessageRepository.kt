package com.akansu.sosyashare.domain.repository

import android.net.Uri
import com.akansu.sosyashare.domain.model.Message

interface MessageRepository {
    suspend fun sendMessage(senderId: String, receiverId: String, message: Message)
    suspend fun getMessagesByChatId(chatId: String): List<Message>
    suspend fun getRecentChats(userId: String): List<Message>
    suspend fun updateMessageReadStatus(chatId: String, messageId: String, isRead: Boolean)
    fun listenForMessages(chatId: String, onMessagesChanged: (List<Message>) -> Unit)
    suspend fun deleteMessage(chatId: String, messageId: String, currentUserId: String)
    suspend fun forwardMessage(senderId: String, receiverId: String, originalMessage: Message)
    suspend fun replyToMessage(senderId: String, receiverId: String, originalMessage: Message, replyContent: String)
    suspend fun sendImageMessage(senderId: String, receiverId: String, imageUri: Uri): String
    suspend fun deleteAllMessages(chatId: String)
}
