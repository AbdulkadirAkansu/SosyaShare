package com.akansu.sosyashare.domain.usecase

import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.repository.PostRepository
import javax.inject.Inject

class CreatePostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(post: Post) {
        postRepository.createPost(post)
    }
}
