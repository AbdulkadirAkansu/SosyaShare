package com.akansu.sosyashare.data.repository

import com.akansu.sosyashare.data.mapper.toDomainModel
import com.akansu.sosyashare.data.mapper.toEntityModel
import com.akansu.sosyashare.data.remote.FirebaseCommentService
import com.akansu.sosyashare.domain.model.Comment
import com.akansu.sosyashare.domain.model.Reply
import com.akansu.sosyashare.domain.repository.CommentRepository
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val commentService: FirebaseCommentService
) : CommentRepository {

    // Comment işlemleri
    override suspend fun addComment(comment: Comment) {
        commentService.addComment(comment.toEntityModel())
    }

    override suspend fun getCommentsForPost(postId: String): List<Comment> {
        return commentService.getCommentsForPost(postId).map { it.toDomainModel() }
    }

    override suspend fun deleteComment(commentId: String) {
        commentService.deleteComment(commentId)
    }

    override suspend fun getCommentById(commentId: String): Comment? {
        return commentService.getCommentById(commentId)?.toDomainModel()
    }

    override suspend fun likeComment(commentId: String, userId: String) {
        commentService.likeComment(commentId, userId)
    }

    override suspend fun unlikeComment(commentId: String, userId: String) {
        commentService.unlikeComment(commentId, userId)
    }

    // Reply işlemleri
    override suspend fun addReply(reply: Reply) {
        commentService.addReply(reply.toEntityModel())
    }

    override suspend fun getRepliesForComment(commentId: String): List<Reply> {
        return commentService.getRepliesForComment(commentId).map { it.toDomainModel() }
    }

    override suspend fun deleteReply(replyId: String) {
        commentService.deleteReply(replyId)
    }

    override suspend fun getReplyById(replyId: String): Reply? {
        return commentService.getReplyById(replyId)?.toDomainModel()
    }

    // Comment with Replies işlemleri
    override suspend fun deleteCommentWithReplies(commentId: String) {
        commentService.deleteCommentWithReplies(commentId)
    }
}
