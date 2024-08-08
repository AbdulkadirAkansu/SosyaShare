package com.akansu.sosyashare.domain.usecase.editprofile

import com.akansu.sosyashare.domain.repository.UserRepository
import javax.inject.Inject

class UpdateBioUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, bio: String) {
        userRepository.updateBioInFirebase(userId, bio)
    }
}
