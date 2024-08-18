package com.akansu.sosyashare.domain.repository

import com.akansu.sosyashare.domain.model.Comment
import com.akansu.sosyashare.domain.model.Reply

interface CommentRepository {
    // Comment işlemleri
    suspend fun addComment(comment: Comment)
    suspend fun getCommentsForPost(postId: String): List<Comment>
    suspend fun deleteComment(commentId: String)
    suspend fun getCommentById(commentId: String): Comment?
    suspend fun likeComment(commentId: String, userId: String)
    suspend fun unlikeComment(commentId: String, userId: String)

    // Reply işlemleri
    suspend fun addReply(reply: Reply)
    suspend fun getRepliesForComment(commentId: String): List<Reply>
    suspend fun deleteReply(replyId: String)
    suspend fun getReplyById(replyId: String): Reply?

    // Comment with Replies işlemleri
    suspend fun deleteCommentWithReplies(commentId: String)
}



