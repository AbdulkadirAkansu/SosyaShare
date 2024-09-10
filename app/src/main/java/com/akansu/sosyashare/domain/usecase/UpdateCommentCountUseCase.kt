package com.akansu.sosyashare.domain.usecase

import com.akansu.sosyashare.domain.repository.PostRepository
import com.akansu.sosyashare.domain.repository.CommentRepository
import javax.inject.Inject

class UpdateCommentCountUseCase @Inject constructor(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(postId: String) {
        // Post'un toplam yorum sayısını günceller
        val comments = commentRepository.getCommentsForPost(postId)
        postRepository.updateCommentCount(postId, comments.size)
    }
}
