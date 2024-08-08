package com.akansu.sosyashare.domain.usecase

import com.akansu.sosyashare.domain.repository.UserRepository
import javax.inject.Inject


class IsUsernameUniqueUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(username: String): Boolean {
        return userRepository.isUsernameUnique(username)
    }
}
