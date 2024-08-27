package com.akansu.sosyashare.data.repository

import com.akansu.sosyashare.data.mapper.toDomainModel
import com.akansu.sosyashare.data.mapper.toEntityModel
import com.akansu.sosyashare.data.remote.FirebasePostService
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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
        emit(posts.map { it.toDomainModel() })
    }

    override suspend fun createPost(post: Post) {
        val postEntity = post.toEntityModel()
        postService.createPost(postEntity.userId, postEntity)
    }

    override suspend fun deletePost(postId: String, postImageUrl: String) {
        postService.deletePost(postId, postImageUrl)
    }

    override suspend fun likePost(postId: String, likerId: String) {
        postService.likePost(postId, likerId)
    }

    override suspend fun unlikePost(postId: String, likerId: String) {
        postService.unlikePost(postId, likerId)
    }

    override suspend fun getPostById(postId: String): Post? {
        val postEntity = postService.getPostById(postId)
        return postEntity?.toDomainModel()
    }

    override suspend fun getLikeStatus(postId: String, likerId: String): Boolean {
        return postService.getLikeStatus(postId, likerId)
    }

    override suspend fun getLikedUserIds(postId: String): List<String> {
        return postService.getLikedUserIds(postId)
    }

    override suspend fun getUserPosts(userId: String): List<Post> {
        val postEntities = postService.getUserPosts(userId)
        return postEntities.map { it.toDomainModel() }
    }

}
