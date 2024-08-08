package com.akansu.sosyashare.domain.usecase.profile

import com.akansu.sosyashare.domain.repository.UserRepository
import javax.inject.Inject

class CheckIfFollowingUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(currentUserId: String, userId: String): Boolean {
        return userRepository.checkIfFollowing(currentUserId, userId)
    }
}
