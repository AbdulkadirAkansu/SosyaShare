package com.akansu.sosyashare.domain.usecase

import com.akansu.sosyashare.domain.repository.AuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String) {
        authRepository.resetPassword(email)
    }
}
