package com.akansu.sosyashare.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.akansu.sosyashare.data.model.CommentEntity
import com.akansu.sosyashare.data.model.ReplyEntity

@Dao
interface CommentDao {
    // Comment işlemleri
    suspend fun addComment(comment: CommentEntity)
    suspend fun getCommentsForPost(postId: String): List<CommentEntity>
    suspend fun deleteComment(commentId: String)
    suspend fun getCommentById(commentId: String): CommentEntity?
    suspend fun likeComment(commentId: String, userId: String)
    suspend fun unlikeComment(commentId: String, userId: String)

    // Reply işlemleri
    suspend fun addReply(reply: ReplyEntity)
    suspend fun getRepliesForComment(commentId: String): List<ReplyEntity>
    suspend fun deleteReply(replyId: String)
    suspend fun getReplyById(replyId: String): ReplyEntity?

    // Comment with Replies işlemleri
    suspend fun deleteCommentWithReplies(commentId: String)
}


