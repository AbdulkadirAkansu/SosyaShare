package com.akansu.sosyashare.domain.repository

import com.akansu.sosyashare.domain.model.Message

interface MessageRepository {
    suspend fun sendMessage(senderId: String, receiverId: String, message: Message) // senderId ve receiverId ile mesaj gönderme
    suspend fun getMessagesByChatId(chatId: String): List<Message> // chatId ile mesajları çekme
    suspend fun getRecentChats(userId: String): List<Message> // userId ile son chatleri çekme
    suspend fun updateMessageReadStatus(chatId: String, messageId: String, isRead: Boolean)
    fun listenForMessages(chatId: String, onMessagesChanged: (List<Message>) -> Unit)
    suspend fun deleteMessage(chatId: String, messageId: String)
    suspend fun forwardMessage(senderId: String, receiverId: String, originalMessage: Message)
    suspend fun replyToMessage(senderId: String, receiverId: String, originalMessage: Message, replyContent: String)
}
