package com.akansu.sosyashare.domain.usecase

import com.akansu.sosyashare.domain.repository.PostRepository
import javax.inject.Inject

class UnlikePostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String, userId: String) {
        postRepository.unlikePost(postId, userId)
    }
}
