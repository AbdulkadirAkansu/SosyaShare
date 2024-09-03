package com.akansu.sosyashare.data.remote

import android.net.Uri
import android.util.Log
import com.akansu.sosyashare.data.model.MessageEntity
import com.akansu.sosyashare.domain.model.Message
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class FirebaseMessageService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    suspend fun deleteMessage(chatId: String, messageId: String, userId: String) {
        val message = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .get()
            .await()
            .toObject(MessageEntity::class.java)

        if (message?.senderId == userId) {
            firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)
                .delete()
                .await()
        }
    }

    suspend fun sendImageMessage(senderId: String, receiverId: String, imageUri: Uri): String {
        val imageUrl = uploadImageToStorage(imageUri)

        // Mesajı Firestore’a kaydedelim
        val chatId = getChatId(senderId, receiverId)
        val message = MessageEntity(
            senderId = senderId,
            receiverId = receiverId,
            content = imageUrl, // Resim URL'sini içerik olarak kaydediyoruz
            timestamp = Date(),
            chatId = chatId
        )
        sendMessage(senderId, receiverId, message)
        return imageUrl
    }

    private suspend fun uploadImageToStorage(imageUri: Uri): String {
        val uniqueFileName = "images/${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child(uniqueFileName)
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun forwardMessage(senderId: String, receiverId: String, originalMessage: MessageEntity) {
        val forwardedMessage = originalMessage.copy(
            id = "",
            senderId = senderId,
            receiverId = receiverId,
            content = "${originalMessage.content} (İletildi)",
            timestamp = Date()
        )
        sendMessage(senderId, receiverId, forwardedMessage)
    }

    suspend fun replyToMessage(senderId: String, receiverId: String, originalMessage: MessageEntity, replyContent: String) {
        val replyMessage = MessageEntity(
            id = "", // Boş bir id kullanıyoruz, bu id veritabanında oluşturulacaktır.
            senderId = senderId,
            receiverId = receiverId,
            content = replyContent,
            replyToMessageId = originalMessage.id,
            timestamp = Date(),
            chatId = originalMessage.chatId
        )
        sendMessage(senderId, receiverId, replyMessage)
    }



    private fun getChatId(user1Id: String, user2Id: String): String {
        return if (user1Id < user2Id) {
            "$user1Id-$user2Id"
        } else {
            "$user2Id-$user1Id"
        }
    }

    fun listenForMessages(chatId: String, onMessagesChanged: (List<MessageEntity>) -> Unit) {
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    Log.e("FirebaseMessageService", "listenForMessages - Failed to listen for messages: ${e?.message}")
                    return@addSnapshotListener
                }

                val messages = snapshot.documents.mapNotNull { it.toObject(MessageEntity::class.java) }
                onMessagesChanged(messages)
            }
    }

    suspend fun sendMessage(senderId: String, receiverId: String, message: MessageEntity) {
        Log.d("FirebaseMessageService", "sendMessage - senderId: $senderId, receiverId: $receiverId, message: $message")

        val chatId = getChatId(senderId, receiverId)
        Log.d("FirebaseMessageService", "sendMessage - Generated chatId: $chatId")

        val chatRef = firestore.collection("chats").document(chatId)

        val documentRef = chatRef.collection("messages").add(message).await()

        val messageWithId = message.copy(id = documentRef.id)
        Log.d("FirebaseMessageService", "sendMessage - Message with ID after Firestore add: $messageWithId")

        chatRef.collection("messages").document(documentRef.id).set(messageWithId).await()

        chatRef.set(mapOf(
            "lastMessage" to messageWithId.content,
            "updatedAt" to messageWithId.timestamp,
            "participants" to listOf(senderId, receiverId)
        )).await()
        Log.d("FirebaseMessageService", "sendMessage - Successfully saved message in Firestore with participants: ${listOf(senderId, receiverId)}")
    }

    suspend fun getMessagesByChatId(chatId: String): List<MessageEntity> {
        Log.d("FirebaseMessageService", "getMessagesByChatId - Fetching messages for chatId: $chatId")

        val messagesSnapshot = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .get(com.google.firebase.firestore.Source.SERVER)
            .await()

        val messages = messagesSnapshot.documents.map { document ->
            val message = document.toObject(MessageEntity::class.java)
            Log.d("FirebaseMessageService", "getMessagesByChatId - Fetched message: $message")
            message?.copy(id = document.id)
        }.filterNotNull()

        Log.d("FirebaseMessageService", "getMessagesByChatId - All fetched messages: $messages")
        return messages
    }

    suspend fun getRecentChats(userId: String): List<Message> {
        Log.d("FirebaseMessageService", "getRecentChats - Fetching recent chats for userId: $userId")

        val chatsSnapshot = firestore.collection("chats")
            .whereArrayContains("participants", userId)
            .orderBy("updatedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()

        val recentChats = mutableListOf<Message>()

        for (document in chatsSnapshot.documents) {
            val chatId = document.id
            Log.d("FirebaseMessageService", "getRecentChats - Processing chatId: $chatId")

            val lastMessageDoc = document.reference.collection("messages")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
                .documents
                .firstOrNull()

            if (lastMessageDoc != null) {
                val lastMessageData = lastMessageDoc.data
                if (lastMessageData != null) {
                    val message = Message(
                        id = lastMessageDoc.id,
                        content = lastMessageData["content"] as? String ?: "",
                        timestamp = lastMessageData["timestamp"] as? Date ?: Date(),
                        senderId = lastMessageData["senderId"] as? String ?: "",
                        receiverId = lastMessageData["receiverId"] as? String ?: "",
                        isRead = lastMessageData["isRead"] as? Boolean ?: false
                    )
                    Log.d("FirebaseMessageService", "getRecentChats - Fetched message from chat: $message")
                    recentChats.add(message)
                }
            }
        }

        Log.d("FirebaseMessageService", "getRecentChats - Recent Chats: $recentChats")
        return recentChats
    }

    suspend fun updateMessageReadStatus(chatId: String, messageId: String, isRead: Boolean) {
        Log.d("FirebaseMessageService", "Attempting to update isRead for message: $messageId in chat: $chatId")
        try {
            firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)
                .update("isRead", isRead)
                .await()
            Log.d("FirebaseMessageService", "Successfully updated isRead for message: $messageId to $isRead")
        } catch (e: Exception) {
            Log.e("FirebaseMessageService", "Failed to update isRead for message: $messageId", e)
        }
    }
}
