package com.akansu.sosyashare.domain.usecase.share

import com.akansu.sosyashare.domain.repository.UserRepository
import javax.inject.Inject

class GetCurrentUserIdUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): String? {
        return userRepository.getCurrentUserId()
    }
}
