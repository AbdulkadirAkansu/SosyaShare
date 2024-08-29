package com.akansu.sosyashare.data.repository

import com.akansu.sosyashare.data.mapper.toDomainModel
import com.akansu.sosyashare.data.mapper.toEntityModel
import com.akansu.sosyashare.data.remote.FirebaseMessageService
import com.akansu.sosyashare.domain.model.Message
import com.akansu.sosyashare.domain.repository.MessageRepository
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageService: FirebaseMessageService
) : MessageRepository {

    override suspend fun sendMessage(chatId: String, message: Message) {
        messageService.sendMessage(chatId, message.toEntityModel())
    }

    override suspend fun getMessagesByChatId(chatId: String): List<Message> {
        return messageService.getMessagesByChatId(chatId).map { it.toDomainModel() }
    }

    override suspend fun getMessagesBetweenUsers(user1Id: String, user2Id: String): List<Message> {
        return messageService.getMessagesBetweenUsers(user1Id, user2Id).map { it.toDomainModel() }
    }

    override suspend fun getRecentMessages(userId: String): List<Message> {
        return messageService.getRecentMessages(userId).map { it.toDomainModel() }
    }

    override suspend fun updateMessageReadStatus(messageId: String, isRead: Boolean) {
        messageService.updateMessageReadStatus(messageId, isRead)
    }
}
