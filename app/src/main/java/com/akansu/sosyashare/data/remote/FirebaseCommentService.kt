package com.akansu.sosyashare.data.remote

import android.util.Log
import com.akansu.sosyashare.data.local.CommentDao
import com.akansu.sosyashare.data.model.CommentEntity
import com.akansu.sosyashare.data.model.ReplyEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseCommentService @Inject constructor() : CommentDao {
    private val firestore = FirebaseFirestore.getInstance()

    // Comment işlemleri
    override suspend fun addComment(comment: CommentEntity) {
        try {
            firestore.collection("comments")
                .document(comment.id)
                .set(comment)
                .await()
            Log.d("FirebaseCommentService", "Comment added successfully: ${comment.username}") // Log ile username'i kontrol edin
        } catch (e: Exception) {
            Log.e("FirebaseCommentService", "Failed to add comment: ${e.message}", e)
        }
    }

    override suspend fun likeComment(commentId: String, userId: String) {
        try {
            val commentRef = firestore.collection("comments").document(commentId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(commentRef)
                val likes = snapshot.get("likes") as? MutableList<String> ?: mutableListOf()
                if (!likes.contains(userId)) {
                    likes.add(userId)
                    transaction.update(commentRef, "likes", likes)
                }
            }.await()
        } catch (e: Exception) {
            Log.e("FirebaseCommentService", "Failed to like comment: ${e.message}", e)
        }
    }

    override suspend fun unlikeComment(commentId: String, userId: String) {
        try {
            val commentRef = firestore.collection("comments").document(commentId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(commentRef)
                val likes = snapshot.get("likes") as? MutableList<String> ?: mutableListOf()
                if (likes.contains(userId)) {
                    likes.remove(userId)
                    transaction.update(commentRef, "likes", likes)
                }
            }.await()
        } catch (e: Exception) {
            Log.e("FirebaseCommentService", "Failed to unlike comment: ${e.message}", e)
        }
    }

    override suspend fun getCommentsForPost(postId: String): List<CommentEntity> {
        return try {
            val result = firestore.collection("comments")
                .whereEqualTo("postId", postId)
                .get()
                .await()
            result.toObjects(CommentEntity::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseCommentService", "Failed to get comments for post: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun deleteComment(commentId: String) {
        try {
            firestore.collection("comments").document(commentId).delete().await()
            Log.d("FirebaseCommentService", "Comment deleted successfully: $commentId")
        } catch (e: Exception) {
            Log.e("FirebaseCommentService", "Failed to delete comment: ${e.message}", e)
        }
    }

    override suspend fun getCommentById(commentId: String): CommentEntity? {
        return try {
            val snapshot = firestore.collection("comments").document(commentId).get().await()
            snapshot.toObject(CommentEntity::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseCommentService", "Failed to get comment by ID: ${e.message}", e)
            null
        }
    }

    // Reply işlemleri
    override suspend fun addReply(reply: ReplyEntity) {
        try {
            firestore.collection("replies")
                .document(reply.id)
                .set(reply)
                .await()
            Log.d("FirebaseCommentService", "Reply added successfully: $reply")
        } catch (e: Exception) {
            Log.e("FirebaseCommentService", "Failed to add reply: ${e.message}", e)
        }
    }

    override suspend fun getRepliesForComment(commentId: String): List<ReplyEntity> {
        return try {
            val result = firestore.collection("replies")
                .whereEqualTo("commentId", commentId)
                .get()
                .await()
            result.toObjects(ReplyEntity::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseCommentService", "Failed to get replies for comment: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun deleteReply(replyId: String) {
        try {
            firestore.collection("replies").document(replyId).delete().await()
            Log.d("FirebaseCommentService", "Reply deleted successfully: $replyId")
        } catch (e: Exception) {
            Log.e("FirebaseCommentService", "Failed to delete reply: ${e.message}", e)
        }
    }

    override suspend fun getReplyById(replyId: String): ReplyEntity? {
        return try {
            val snapshot = firestore.collection("replies").document(replyId).get().await()
            snapshot.toObject(ReplyEntity::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseCommentService", "Failed to get reply by ID: ${e.message}", e)
            null
        }
    }

    // Comment with Replies işlemleri
    override suspend fun deleteCommentWithReplies(commentId: String) {
        try {
            val repliesSnapshot = firestore.collection("replies")
                .whereEqualTo("commentId", commentId)
                .get()
                .await()

            firestore.runTransaction { transaction ->
                // Ana yorumu sil
                val commentRef = firestore.collection("comments").document(commentId)
                transaction.delete(commentRef)

                // Ona bağlı tüm yanıtları sil
                for (reply in repliesSnapshot.documents) {
                    transaction.delete(reply.reference)
                }
            }

            Log.d("FirebaseCommentService", "Comment and replies deleted successfully: $commentId")
        } catch (e: Exception) {
            Log.e("FirebaseCommentService", "Failed to delete comment with replies: ${e.message}", e)
        }
    }
}
