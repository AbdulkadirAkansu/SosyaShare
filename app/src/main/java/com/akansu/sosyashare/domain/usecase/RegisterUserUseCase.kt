package com.akansu.sosyashare.domain.usecase

import com.akansu.sosyashare.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, username: String) {
        authRepository.registerUser(email, password, username)
    }
}
