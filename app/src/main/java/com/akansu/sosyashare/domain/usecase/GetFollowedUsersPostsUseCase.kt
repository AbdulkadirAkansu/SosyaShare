package com.akansu.sosyashare.domain.usecase

import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFollowedUsersPostsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userIds: List<String>): Flow<List<Post>> {
        return userRepository.getFollowedUsersPosts(userIds)
    }
}
