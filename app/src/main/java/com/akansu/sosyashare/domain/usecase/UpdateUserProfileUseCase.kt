package com.akansu.sosyashare.domain.usecase

import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User) {
        userRepository.updateUser(user)
    }
}
