package com.akansu.sosyashare.domain.usecase.search

import com.akansu.sosyashare.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }
}
