package com.akansu.sosyashare.domain.repository

import com.akansu.sosyashare.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun getAllPosts(): Flow<List<Post>>
    fun getPostsByUser(userId: String): Flow<List<Post>>
    suspend fun createPost(post: Post)
    suspend fun deletePost(postId: String, postImageUrl: String)
    suspend fun likePost(postId: String, likerId: String)
    suspend fun unlikePost(postId: String, likerId: String)
    suspend fun getPostById(postId: String): Post?
    suspend fun getLikeStatus(postId: String, likerId: String): Boolean
    suspend fun getLikedUserIds(postId: String): List<String>
    suspend fun getUserPosts(userId: String): List<Post>
    suspend fun updateCommentCount(postId: String, newCommentCount: Int)
}
