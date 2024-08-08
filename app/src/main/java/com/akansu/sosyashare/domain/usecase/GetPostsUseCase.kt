package com.akansu.sosyashare.domain.usecase

import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPostsUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    operator fun invoke(): Flow<List<Post>> {
        return postRepository.getAllPosts()
    }
}
