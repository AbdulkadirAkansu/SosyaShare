package com.akansu.sosyashare.domain.usecase.profile

import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class GetUserDetailsUseCase(private val userRepository: UserRepository) {
    operator fun invoke(userId: String): Flow<User?> {
        return userRepository.getUserById(userId)
    }
}
