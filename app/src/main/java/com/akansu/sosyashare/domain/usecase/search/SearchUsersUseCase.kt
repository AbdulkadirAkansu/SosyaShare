package com.akansu.sosyashare.domain.usecase.search

import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.repository.UserRepository
import javax.inject.Inject

class SearchUsersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(query: String): List<User> {
        return userRepository.searchUsers(query)
    }
}
