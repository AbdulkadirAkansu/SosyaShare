package com.akansu.sosyashare.data.remote

import android.util.Log
import com.akansu.sosyashare.data.model.PostEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebasePostService @Inject constructor() {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun createPost(post: PostEntity) {
        firestore.collection("posts").document(post.id).set(post).await()
        Log.d("FirebasePostService", "Post created: $post")
    }

    suspend fun deletePost(postId: String) {
        firestore.collection("posts").document(postId).delete().await()
        Log.d("FirebasePostService", "Post deleted: $postId")
    }

    suspend fun likePost(postId: String, userId: String) {
        val postRef = firestore.collection("posts").document(postId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val post = snapshot.toObject(PostEntity::class.java)
            val updatedLikeCount = post?.likeCount?.plus(1) ?: 0
            transaction.update(postRef, "likeCount", updatedLikeCount)
        }.await()
        Log.d("FirebasePostService", "Post liked: $postId by user: $userId")
    }

    suspend fun unlikePost(postId: String, userId: String) {
        val postRef = firestore.collection("posts").document(postId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val post = snapshot.toObject(PostEntity::class.java)
            val updatedLikeCount = post?.likeCount?.minus(1) ?: 0
            transaction.update(postRef, "likeCount", updatedLikeCount)
        }.await()
        Log.d("FirebasePostService", "Post unliked: $postId by user: $userId")
    }

    suspend fun getAllPosts(): List<PostEntity> {
        val result = firestore.collection("posts").get().await()
        val posts = result.toObjects(PostEntity::class.java)
        Log.d("FirebasePostService", "All posts loaded: $posts")
        return posts
    }

    suspend fun getPostById(postId: String): PostEntity? {
        return try {
            val document = firestore.collection("posts").document(postId).get().await()
            val post = document.toObject(PostEntity::class.java)
            Log.d("FirebasePostService", "Fetched post for postId $postId: $post")
            post
        } catch (e: Exception) {
            Log.e("FirebasePostService", "Error fetching post for postId $postId", e)
            null
        }
    }
}
