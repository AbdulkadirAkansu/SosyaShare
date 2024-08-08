package com.akansu.sosyashare.domain.usecase.profile

import com.akansu.sosyashare.domain.repository.UserRepository
import javax.inject.Inject

class GetCurrentUserIdUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(): String? {
        return userRepository.getCurrentUserId()
    }
}
