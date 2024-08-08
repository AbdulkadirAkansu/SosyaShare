package com.akansu.sosyashare.domain.usecase.share

import com.akansu.sosyashare.domain.repository.UserRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class RefreshUserPostsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String? = null): List<String> {
        val currentUserId = userId ?: userRepository.getCurrentUserId()
        currentUserId?.let {
            val user = userRepository.getUserById(it).firstOrNull()
            user?.let {
                return user.posts
            }
        }
        return emptyList()
    }
}
