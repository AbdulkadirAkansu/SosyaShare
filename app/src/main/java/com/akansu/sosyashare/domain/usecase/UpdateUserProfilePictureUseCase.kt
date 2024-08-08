package com.akansu.sosyashare.domain.usecase

import com.akansu.sosyashare.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserProfilePictureUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, profilePictureUrl: String) {
        userRepository.updateUserProfilePicture(userId, profilePictureUrl)
    }
}
