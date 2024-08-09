package com.akansu.sosyashare.data.remote

import android.util.Log
import com.akansu.sosyashare.data.model.PostEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class FirebasePostService @Inject constructor(
    private val firebaseStorage: FirebaseStorage // FirebaseStorage burada enjekte ediliyor
) {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun createPost(userId: String, post: PostEntity) {
        val postRef = firestore.collection("users")
            .document(userId)
            .collection("posts")
            .document(post.id)
        postRef.set(post).await()
    }

    suspend fun deletePost(userId: String, postId: String, postImageUrl: String) {
        // Gönderiyi Firestore'dan silme
        val postRef = firestore.collection("users").document(userId).collection("posts").document(postId)
        postRef.delete().await()

        // Gönderiye ait resmi Firebase Storage'dan silme
        if (postImageUrl.isNotEmpty()) {
            val storageRef = firebaseStorage.getReferenceFromUrl(postImageUrl)
            storageRef.delete().await()
        }
        Log.d("FirebasePostService", "Deleted post with postId: $postId for userId: $userId")
    }

    suspend fun likePost(postId: String, userId: String, likerId: String) {
        try {
            val postRef = firestore.collection("users")
                .document(userId)
                .collection("posts")
                .document(postId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                if (snapshot.exists()) {
                    val post = snapshot.toObject(PostEntity::class.java)
                    val updatedLikeCount = post?.likeCount?.plus(1) ?: 1
                    val likedBy = post?.likedBy?.toMutableList() ?: mutableListOf()

                    if (!likedBy.contains(likerId)) {
                        likedBy.add(likerId)
                    }

                    transaction.update(postRef, mapOf("likeCount" to updatedLikeCount, "likedBy" to likedBy))
                } else {
                    transaction.set(postRef, mapOf(
                        "id" to postId,
                        "likeCount" to 1,
                        "likedBy" to listOf(likerId)
                    ))
                }
            }.await()
        } catch (e: Exception) {
            Log.e("FirebasePostService", "Error liking post with postId: $postId", e)
        }
    }

    suspend fun unlikePost(postId: String, userId: String, likerId: String) {
        try {
            val postRef = firestore.collection("users")
                .document(userId)
                .collection("posts")
                .document(postId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val post = snapshot.toObject(PostEntity::class.java)
                val updatedLikeCount = post?.likeCount?.minus(1) ?: 0
                val likedBy = post?.likedBy?.toMutableList() ?: mutableListOf()

                if (likedBy.contains(likerId)) {
                    likedBy.remove(likerId)
                }

                transaction.update(postRef, mapOf("likeCount" to updatedLikeCount, "likedBy" to likedBy))
            }.await()
        } catch (e: Exception) {
            Log.e("FirebasePostService", "Error unliking post with postId: $postId", e)
        }
    }

    suspend fun getAllPosts(): List<PostEntity> {
        val result = firestore.collectionGroup("posts").get().await()
        return result.toObjects(PostEntity::class.java)
    }

    suspend fun getPostById(postId: String, userId: String): PostEntity? {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .collection("posts")
                .document(postId)
                .get()
                .await()
            document.toObject(PostEntity::class.java)
        } catch (e: Exception) {
            Log.e("FirebasePostService", "Error fetching post for postId $postId", e)
            null
        }
    }

    suspend fun getPostsByUser(userId: String): List<PostEntity> {
        val postsRef = firestore.collection("users")
            .document(userId)
            .collection("posts")

        val result = postsRef.get().await()
        return result.toObjects(PostEntity::class.java)
    }

    suspend fun migrateOldPosts(userId: String, oldPosts: List<String>) {
        oldPosts.forEach { imageUrl ->
            val post = PostEntity(
                id = UUID.randomUUID().toString(),
                content = "Old post migrated", // İçeriği burada belirleyin
                imageUrl = imageUrl
            )
            createPost(userId, post)
        }
    }
}
