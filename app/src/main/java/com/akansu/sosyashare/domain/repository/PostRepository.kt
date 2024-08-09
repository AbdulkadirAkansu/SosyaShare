package com.akansu.sosyashare.domain.repository

import com.akansu.sosyashare.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun getAllPosts(): Flow<List<Post>>
    fun getPostsByUser(userId: String): Flow<List<Post>>  // Burada türü Flow olarak değiştirdik
    suspend fun createPost(post: Post)
    suspend fun deletePost(postId: String, userId: String)
    suspend fun likePost(postId: String, userId: String)
    suspend fun unlikePost(postId: String, userId: String)
    suspend fun getPostById(postId: String, userId: String): Post?
}
