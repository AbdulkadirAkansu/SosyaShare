package com.akansu.sosyashare.domain.repository

import com.akansu.sosyashare.domain.model.Message

interface MessageRepository {
    suspend fun sendMessage(senderId: String, receiverId: String, message: Message) // senderId ve receiverId ile mesaj gönderme
    suspend fun getMessagesByChatId(chatId: String): List<Message> // chatId ile mesajları çekme
    suspend fun getRecentChats(userId: String): List<Message> // userId ile son chatleri çekme
    suspend fun updateMessageReadStatus(chatId: String, messageId: String, isRead: Boolean)
}
