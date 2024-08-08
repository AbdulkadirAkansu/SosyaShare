package com.akansu.sosyashare.data.repository

import android.util.Log
import com.akansu.sosyashare.data.mapper.toDomainModel
import com.akansu.sosyashare.data.mapper.toEntityModel
import com.akansu.sosyashare.data.model.PostEntity
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
        val posts = postService.getAllPosts().map { it.toDomainModel() }
        emit(posts)
        Log.d("PostRepositoryImpl", "All posts: $posts")
    }

    override suspend fun createPost(post: Post) {
        postService.createPost(post.toEntityModel())
        Log.d("PostRepositoryImpl", "Post created: $post")
    }

    override suspend fun deletePost(postId: String) {
        postService.deletePost(postId)
        Log.d("PostRepositoryImpl", "Post deleted: $postId")
    }

    override suspend fun likePost(postId: String, userId: String) {
        postService.likePost(postId, userId)
        Log.d("PostRepositoryImpl", "Post liked: $postId by user: $userId")
    }

    override suspend fun unlikePost(postId: String, userId: String) {
        postService.unlikePost(postId, userId)
        Log.d("PostRepositoryImpl", "Post unliked: $postId by user: $userId")
    }

    override suspend fun getPostById(postId: String): Post? {
        val postEntity = postService.getPostById(postId)
        Log.d("PostRepositoryImpl", "Fetched post for postId $postId: $postEntity")
        return postEntity?.toDomainModel()
    }
}