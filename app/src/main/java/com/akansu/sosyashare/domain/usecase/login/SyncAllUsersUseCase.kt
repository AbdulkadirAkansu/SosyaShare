package com.akansu.sosyashare.domain.usecase.login

import com.akansu.sosyashare.domain.repository.AuthRepository
import javax.inject.Inject

class SyncAllUsersUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.syncAllUsers()
    }
}
