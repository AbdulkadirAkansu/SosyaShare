package com.akansu.sosyashare.data.remote

import android.util.Log
import com.akansu.sosyashare.data.local.CommentDao
import com.akansu.sosyashare.data.model.CommentEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseCommentService @Inject constructor() : CommentDao {
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun addComment(comment: CommentEntity) {
        try {
            firestore.collection("comments")
                .document(comment.id)
                .set(comment)
                .await()
            Log.d("com.akansu.sosyashare.data.remote.FirebaseCommentService", "Comment added successfully: $comment")
        } catch (e: Exception) {
            Log.e("com.akansu.sosyashare.data.remote.FirebaseCommentService", "Failed to add comment: ${e.message}", e)
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
            Log.e("com.akansu.sosyashare.data.remote.FirebaseCommentService", "Failed to get comments for post: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun deleteComment(commentId: String) {
        try {
            val commentRef = firestore.collection("comments").document(commentId)
            val commentSnapshot = commentRef.get().await()

            if (commentSnapshot.exists()) {
                val comment = commentSnapshot.toObject(CommentEntity::class.java)
                if (comment?.parentCommentId == null) {
                    // Ana yorum ise, bağlı tüm yanıtları sil
                    val repliesSnapshot = firestore.collection("comments")
                        .whereEqualTo("parentCommentId", commentId)
                        .get()
                        .await()
                    val batch = firestore.batch()
                    for (reply in repliesSnapshot.documents) {
                        batch.delete(reply.reference)
                    }
                    batch.commit().await()
                }
                // Ana yorumu sil
                commentRef.delete().await()
                Log.d("com.akansu.sosyashare.data.remote.FirebaseCommentService", "Comment deleted successfully: $commentId")
            } else {
                Log.e("com.akansu.sosyashare.data.remote.FirebaseCommentService", "Comment does not exist, ID: $commentId")
            }
        } catch (e: Exception) {
            Log.e("com.akansu.sosyashare.data.remote.FirebaseCommentService", "Failed to delete comment: ${e.message}", e)
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
            Log.e("com.akansu.sosyashare.data.remote.FirebaseCommentService", "Failed to like comment: ${e.message}", e)
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
            Log.e("com.akansu.sosyashare.data.remote.FirebaseCommentService", "Failed to unlike comment: ${e.message}", e)
        }
    }

    override suspend fun getCommentById(commentId: String): CommentEntity? {
        return try {
            val commentSnapshot = firestore.collection("comments")
                .document(commentId)
                .get()
                .await()

            commentSnapshot.toObject(CommentEntity::class.java)
        } catch (e: Exception) {
            Log.e("com.akansu.sosyashare.data.remote.FirebaseCommentService", "Failed to get comment by ID: ${e.message}", e)
            null
        }
    }

    override suspend fun addReplyToComment(commentId: String, reply: CommentEntity) {
        try {
            firestore.collection("comments").document(reply.id).set(reply).await()
            Log.d("com.akansu.sosyashare.data.remote.FirebaseCommentService", "Reply added successfully: $reply")
        } catch (e: Exception) {
            Log.e("com.akansu.sosyashare.data.remote.FirebaseCommentService", "Failed to add reply: ${e.message}", e)
        }
    }
}
