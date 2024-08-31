package com.akansu.sosyashare.data.remote

import com.akansu.sosyashare.data.model.MessageEntity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class FirebaseMessageService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun getChatId(user1Id: String, user2Id: String): String {
        return if (user1Id < user2Id) {
            "$user1Id-$user2Id"
        } else {
            "$user2Id-$user1Id"
        }
    }

    suspend fun sendMessage(senderId: String, receiverId: String, message: MessageEntity) {
        val chatId = getChatId(senderId, receiverId)
        val chatRef = firestore.collection("chats").document(chatId)

        // Mesajı alt koleksiyona ekle
        chatRef.collection("messages").add(message).await()

        // Son mesaj ve zaman bilgilerini güncelle
        chatRef.set(mapOf(
            "lastMessage" to message.content,
            "updatedAt" to message.timestamp,
            "participants" to listOf(senderId, receiverId)
        )).await()
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

    suspend fun getRecentChats(userId: String): List<Map<String, Any>> {
        val chatsSnapshot = firestore.collection("chats")
            .whereArrayContains("participants", userId)
            .orderBy("updatedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()

        return chatsSnapshot.documents.map { document ->
            document.data?.let {
                // `Timestamp` türündeki `updatedAt` alanını `Date` türüne dönüştürelim
                val updatedAtTimestamp = it["updatedAt"] as? Timestamp
                val updatedAtDate = updatedAtTimestamp?.toDate()

                // `Map` içinde `updatedAt` alanını `Date` ile güncelleyelim
                it.toMutableMap().apply {
                    this["updatedAt"] = updatedAtDate
                }
            } ?: emptyMap()
        }
    }

    suspend fun updateMessageReadStatus(chatId: String, messageId: String, isRead: Boolean) {
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .update("isRead", isRead)
            .await()
    }
}
