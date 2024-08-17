package com.akansu.sosyashare.domain.repository

import com.akansu.sosyashare.domain.model.Comment

interface CommentRepository {
    suspend fun addComment(comment: Comment)
    suspend fun getCommentsForPost(postId: String): List<Comment>
    suspend fun deleteComment(commentId: String)
    suspend fun likeComment(commentId: String, userId: String)
    suspend fun unlikeComment(commentId: String, userId: String)
    suspend fun getCommentById(commentId: String): Comment? // Bu fonksiyonu ekleyin
    suspend fun replyToComment(commentId: String, reply: Comment) // Cevap ekleme fonksiyonu
}


