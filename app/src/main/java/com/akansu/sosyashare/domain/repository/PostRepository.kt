package com.akansu.sosyashare.domain.repository

import com.akansu.sosyashare.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun getAllPosts(): Flow<List<Post>>
    suspend fun createPost(post: Post)
    suspend fun deletePost(postId: String)
    suspend fun likePost(postId: String, userId: String)
    suspend fun unlikePost(postId: String, userId: String)
    suspend fun getPostById(postId: String): Post?
}
