package com.akansu.sosyashare.domain.usecase.share

import android.net.Uri
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.repository.PostRepository
import com.akansu.sosyashare.domain.repository.StorageRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import kotlinx.coroutines.flow.firstOrNull
import java.util.*
import javax.inject.Inject

class UploadPostPictureUseCase @Inject constructor(
    private val storageRepository: StorageRepository,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository // PostRepository ekleniyor
) {
    suspend operator fun invoke(uri: Uri, comment: String): Post {
        val url = storageRepository.uploadPostPicture(uri)
        val userId = userRepository.getCurrentUserId() ?: throw IllegalStateException("User ID cannot be null")

        // Post oluşturma
        val post = Post(
            id = UUID.randomUUID().toString(),
            userId = userId,
            content = comment,
            imageUrl = url,
            createdAt = Date(),
            likeCount = 0,
            likedBy = emptyList()
        )

        postRepository.createPost(post) // Post'u veritabanına kaydetme
        return post
    }
}

