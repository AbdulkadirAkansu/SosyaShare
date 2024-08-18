package com.akansu.sosyashare.data.repository

import com.akansu.sosyashare.data.local.CommentDao
import com.akansu.sosyashare.data.mapper.toDomainModel
import com.akansu.sosyashare.data.mapper.toEntityModel
import com.akansu.sosyashare.domain.model.Comment
import com.akansu.sosyashare.domain.model.Reply
import com.akansu.sosyashare.domain.repository.CommentRepository
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val commentDao: CommentDao
) : CommentRepository {

    // Comment işlemleri
    override suspend fun addComment(comment: Comment) {
        commentDao.addComment(comment.toEntityModel())
    }

    override suspend fun getCommentsForPost(postId: String): List<Comment> {
        return commentDao.getCommentsForPost(postId).map { it.toDomainModel() }
    }

    override suspend fun deleteComment(commentId: String) {
        commentDao.deleteComment(commentId)
    }

    override suspend fun getCommentById(commentId: String): Comment? {
        return commentDao.getCommentById(commentId)?.toDomainModel()
    }

    override suspend fun likeComment(commentId: String, userId: String) {
        commentDao.likeComment(commentId, userId)
    }

    override suspend fun unlikeComment(commentId: String, userId: String) {
        commentDao.unlikeComment(commentId, userId)
    }

    // Reply işlemleri
    override suspend fun addReply(reply: Reply) {
        commentDao.addReply(reply.toEntityModel())
    }

    override suspend fun getRepliesForComment(commentId: String): List<Reply> {
        return commentDao.getRepliesForComment(commentId).map { it.toDomainModel() }
    }

    override suspend fun deleteReply(replyId: String) {
        commentDao.deleteReply(replyId)
    }

    override suspend fun getReplyById(replyId: String): Reply? {
        return commentDao.getReplyById(replyId)?.toDomainModel()
    }

    // Comment with Replies işlemleri
    override suspend fun deleteCommentWithReplies(commentId: String) {
        commentDao.deleteCommentWithReplies(commentId)
    }
}
