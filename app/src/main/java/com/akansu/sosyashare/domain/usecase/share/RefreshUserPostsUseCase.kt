package com.akansu.sosyashare.domain.usecase.share

import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.repository.PostRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class RefreshUserPostsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
) {
    suspend operator fun invoke(userId: String? = null): Flow<List<Post>> {  // suspend olarak tanımlandı
        val currentUserId = userId ?: userRepository.getCurrentUserId()  // getCurrentUserId burada çağrılıyor
        return if (currentUserId != null) {
            postRepository.getPostsByUser(currentUserId)
        } else {
            flowOf(emptyList())
        }
    }
}
