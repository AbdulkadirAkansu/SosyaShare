package com.akansu.sosyashare.domain.usecase.editprofile

import com.akansu.sosyashare.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserPostsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, posts: List<String>) {
        userRepository.updateUserPosts(userId, posts)
    }
}
