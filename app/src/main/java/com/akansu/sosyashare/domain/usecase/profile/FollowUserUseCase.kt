package com.akansu.sosyashare.domain.usecase.profile

import com.akansu.sosyashare.domain.repository.UserRepository
import javax.inject.Inject

class FollowUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(currentUserId: String, followUserId: String) {
        userRepository.followUser(currentUserId, followUserId)
    }
}
