package com.akansu.sosyashare.data.remote

import com.akansu.sosyashare.data.model.MessageEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseMessageService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun sendMessage(chatId: String, message: MessageEntity) {
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message)
            .await()
    }

    suspend fun getMessagesByChatId(chatId: String): List<MessageEntity> {
        val messagesSnapshot = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .get()
            .await()

        return messagesSnapshot.toObjects(MessageEntity::class.java)
    }

    suspend fun getMessagesBetweenUsers(user1Id: String, user2Id: String): List<MessageEntity> {
        val messagesSnapshot = firestore.collection("messages")
            .whereIn("senderId", listOf(user1Id, user2Id))
            .whereIn("receiverId", listOf(user2Id, user1Id))
            .orderBy("timestamp")
            .get()
            .await()

        return messagesSnapshot.toObjects(MessageEntity::class.java)
    }

    suspend fun getRecentMessages(userId: String): List<MessageEntity> {
        val messagesSnapshot = firestore.collection("messages")
            .whereEqualTo("receiverId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()

        return messagesSnapshot.toObjects(MessageEntity::class.java)
    }

    suspend fun updateMessageReadStatus(messageId: String, isRead: Boolean) {
        firestore.collection("messages")
            .document(messageId)
            .update("isRead", isRead)
            .await()
    }
}
