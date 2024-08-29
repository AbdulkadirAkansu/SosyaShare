package com.akansu.sosyashare.domain.repository

import com.akansu.sosyashare.domain.model.Message

interface MessageRepository {
    suspend fun sendMessage(chatId: String, message: Message) // chatId ile mesaj gönderme
    suspend fun getMessagesByChatId(chatId: String): List<Message> // chatId ile mesajları çekme
    suspend fun getMessagesBetweenUsers(user1Id: String, user2Id: String): List<Message>
    suspend fun getRecentMessages(userId: String): List<Message>
    suspend fun updateMessageReadStatus(messageId: String, isRead: Boolean)
}
