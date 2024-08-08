package com.akansu.sosyashare.domain.usecase.share

import android.net.Uri
import com.akansu.sosyashare.domain.repository.StorageRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class UploadPostPictureUseCase @Inject constructor(
    private val storageRepository: StorageRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(uri: Uri, comment: String): List<String> {
        val url = storageRepository.uploadPostPicture(uri)
        val userId = userRepository.getCurrentUserId()
        if (userId != null) {
            val user = userRepository.getUserById(userId).firstOrNull()
            user?.let {
                val updatedPosts = user.posts.toMutableList().apply { add(url) }
                val updatedComments = user.comments.toMutableList().apply { add(comment) }
                userRepository.updateUser(user.copy(posts = updatedPosts, comments = updatedComments))
                userRepository.updateUserPosts(userId, updatedPosts)
                return updatedPosts
            }
        }
        return emptyList()
    }
}
