package com.akansu.sosyashare.domain.usecase

import com.akansu.sosyashare.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetFollowedUserIdsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(currentUserId: String): List<String> {
        return userRepository.getFollowedUserIds(currentUserId)
    }
}