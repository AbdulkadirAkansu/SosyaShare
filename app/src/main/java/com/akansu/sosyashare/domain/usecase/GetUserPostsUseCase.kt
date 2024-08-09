package com.akansu.sosyashare.domain.usecase.post

import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserPostsUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    operator fun invoke(userId: String): Flow<List<Post>> {
        return postRepository.getPostsByUser(userId)
    }
}
