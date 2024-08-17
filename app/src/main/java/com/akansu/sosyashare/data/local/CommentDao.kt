package com.akansu.sosyashare.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.akansu.sosyashare.data.model.CommentEntity

@Dao
interface CommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addComment(comment: CommentEntity)
    suspend fun getCommentsForPost(postId: String): List<CommentEntity>
    suspend fun deleteComment(commentId: String)
    suspend fun getCommentById(commentId: String): CommentEntity?
    suspend fun likeComment(commentId: String, userId: String)
    suspend fun unlikeComment(commentId: String, userId: String)
    suspend fun addReplyToComment(commentId: String, reply: CommentEntity)
}


