package com.akansu.sosyashare.domain.usecase.share

import com.akansu.sosyashare.domain.repository.UserRepository
import javax.inject.Inject

class DeletePostUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(postUrl: String) {
        val currentUserId = userRepository.getCurrentUserId()
        currentUserId?.let {
            userRepository.deletePost(it, postUrl)
        }
    }
}
