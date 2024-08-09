package com.akansu.sosyashare.domain.usecase.postdetail

import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.repository.PostRepository
import javax.inject.Inject

class GetPostDetailsUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String, userId: String): Post? {
        return postRepository.getPostById(postId, userId)
    }
}