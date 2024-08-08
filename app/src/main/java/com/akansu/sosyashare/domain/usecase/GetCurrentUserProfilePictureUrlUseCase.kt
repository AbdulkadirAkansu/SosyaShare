package com.akansu.sosyashare.domain.usecase

import com.akansu.sosyashare.domain.repository.UserRepository
import javax.inject.Inject

class GetCurrentUserProfilePictureUrlUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): String? {
        return userRepository.getCurrentUserProfilePictureUrl()
    }
}
