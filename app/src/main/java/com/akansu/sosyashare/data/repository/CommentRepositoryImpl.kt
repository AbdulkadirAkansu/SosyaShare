import android.util.Log
import com.akansu.sosyashare.data.local.CommentDao
import com.akansu.sosyashare.data.mapper.toDomainModel
import com.akansu.sosyashare.data.mapper.toEntityModel
import com.akansu.sosyashare.domain.model.Comment
import com.akansu.sosyashare.domain.repository.CommentRepository
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val commentDao: CommentDao
) : CommentRepository {

    override suspend fun addComment(comment: Comment) {
        commentDao.addComment(comment.toEntityModel())
    }

    override suspend fun getCommentsForPost(postId: String): List<Comment> {
        return commentDao.getCommentsForPost(postId).map { it.toDomainModel() }
    }

    override suspend fun deleteComment(commentId: String) {
        Log.d("CommentRepositoryImpl", "Deleting comment with id: $commentId")
        commentDao.deleteComment(commentId)
    }

    override suspend fun likeComment(commentId: String, userId: String) {
        Log.d("CommentRepositoryImpl", "Liking comment with id: $commentId by user: $userId")
        commentDao.likeComment(commentId, userId)
    }

    override suspend fun unlikeComment(commentId: String, userId: String) {
        Log.d("CommentRepositoryImpl", "Unliking comment with id: $commentId by user: $userId")
        commentDao.unlikeComment(commentId, userId)
    }

    override suspend fun getCommentById(commentId: String): Comment? {
        Log.d("CommentRepositoryImpl", "Fetching comment with id: $commentId")
        return commentDao.getCommentById(commentId)?.toDomainModel()
    }

    override suspend fun replyToComment(parentCommentId: String, reply: Comment) {
        commentDao.addReplyToComment(parentCommentId, reply.toEntityModel())
    }
}
