package com.akansu.sosyashare.data.repository

import com.akansu.sosyashare.data.mapper.toDomainModel
import com.akansu.sosyashare.data.mapper.toEntityModel
import com.akansu.sosyashare.data.remote.FirebasePostService
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val postService: FirebasePostService
) : PostRepository {

    override fun getAllPosts(): Flow<List<Post>> = flow {
        val posts = postService.getAllPosts()
        emit(posts.map { it.toDomainModel() })
    }

    override fun getPostsByUser(userId: String): Flow<List<Post>> = flow {
        val posts = postService.getPostsByUser(userId)
        emit(posts.map { it.toDomainModel() })  // Verileri Flow olarak yayımlıyoruz
    }

    override suspend fun createPost(post: Post) {
        val userId = post.userId
        val postEntity = post.toEntityModel()
        postService.createPost(userId, postEntity)
    }

    override suspend fun deletePost(postId: String, userId: String) {
        val postEntity = postService.getPostById(postId, userId)
        val postImageUrl = postEntity?.imageUrl ?: ""

        // Post'u sil
        postService.deletePost(postId, userId, postImageUrl)
    }

    override suspend fun likePost(postId: String, userId: String) {
        postService.likePost(postId, userId, userId)
    }

    override suspend fun unlikePost(postId: String, userId: String) {
        postService.unlikePost(postId, userId, userId)
    }

    override suspend fun getPostById(postId: String, userId: String): Post? {
        val postEntity = postService.getPostById(postId, userId)
        return postEntity?.toDomainModel()
    }
}
