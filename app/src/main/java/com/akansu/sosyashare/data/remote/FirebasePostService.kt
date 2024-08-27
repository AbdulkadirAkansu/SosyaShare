package com.akansu.sosyashare.data.remote

import android.util.Log
import com.akansu.sosyashare.data.model.PostEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebasePostService @Inject constructor(
    private val firebaseStorage: FirebaseStorage
) {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getUserPosts(userId: String): List<PostEntity> {
        val postsRef = firestore.collection("posts").whereEqualTo("userId", userId)
        val result = postsRef.get().await()
        return result.toObjects(PostEntity::class.java)
    }

    suspend fun createPost(userId: String, post: PostEntity) {
        val postRef = firestore.collection("posts")
            .document(post.id)
        postRef.set(post).await()
    }

    suspend fun deletePost(postId: String, postImageUrl: String) {
        try {
            val postRef = firestore.collection("posts").document(postId)
            postRef.delete().await()

            if (postImageUrl.isNotEmpty()) {
                val storageRef = firebaseStorage.getReferenceFromUrl(postImageUrl)
                storageRef.delete().await()
                Log.d("FirebasePostService", "Deleted image at: $postImageUrl")
            }
            Log.d("FirebasePostService", "Deleted post with postId: $postId")
        } catch (e: IllegalArgumentException) {
            Log.e("FirebasePostService", "Invalid URL: ${e.message}")
        } catch (e: Exception) {
            Log.e("FirebasePostService", "Error deleting post or image: ${e.message}")
        }
    }

    suspend fun likePost(postId: String, likerId: String) {
        Log.d("FirebasePostService", "Liking Post: postId=$postId by User: $likerId")
        val postRef = firestore.collection("posts").document(postId)
        val likeRef = postRef.collection("likes").document(likerId)

        firestore.runTransaction { transaction ->
            val postSnapshot = transaction.get(postRef)
            val likeSnapshot = transaction.get(likeRef)

            if (!likeSnapshot.exists()) {
                val newLikeCount = (postSnapshot.getLong("likeCount") ?: 0) + 1
                transaction.update(postRef, "likeCount", newLikeCount)
                val likedByList = postSnapshot.get("likedBy") as? MutableList<String> ?: mutableListOf()
                likedByList.add(likerId)
                transaction.update(postRef, "likedBy", likedByList)

                transaction.set(likeRef, mapOf("userId" to likerId))

                Log.d("FirebasePostService", "Post Liked: postId=$postId, newLikeCount=$newLikeCount, likedBy=$likedByList")
            } else {
                Log.d("FirebasePostService", "User $likerId has already liked post $postId")
            }
        }.await()
    }

    suspend fun getLikedUserIds(postId: String): List<String> {
        return try {
            val likesCollection = firestore.collection("posts")
                .document(postId)
                .collection("likes")
                .get()
                .await()

            likesCollection.documents.mapNotNull { it.id }
        } catch (e: Exception) {
            Log.e("FirebasePostService", "Error fetching liked user IDs for postId $postId", e)
            emptyList()
        }
    }



    suspend fun unlikePost(postId: String, likerId: String) {
        Log.d("FirebasePostService", "Unliking Post: postId=$postId by User: $likerId")
        val postRef = firestore.collection("posts").document(postId)
        val likeRef = postRef.collection("likes").document(likerId)

        firestore.runTransaction { transaction ->
            val postSnapshot = transaction.get(postRef)
            val likeSnapshot = transaction.get(likeRef)

            if (likeSnapshot.exists()) {
                // Post güncellemesi
                val newLikeCount = (postSnapshot.getLong("likeCount") ?: 0) - 1
                transaction.update(postRef, "likeCount", newLikeCount)

                // likedBy listesi güncellemesi
                val likedByList = postSnapshot.get("likedBy") as? MutableList<String> ?: mutableListOf()
                likedByList.remove(likerId)
                transaction.update(postRef, "likedBy", likedByList)

                // Beğeni kaydının silinmesi
                transaction.delete(likeRef)

                Log.d("FirebasePostService", "Post Unliked: postId=$postId, newLikeCount=$newLikeCount, likedBy=$likedByList")
            } else {
                Log.d("FirebasePostService", "User $likerId had not liked post $postId")
            }
        }.await()
    }

    suspend fun getPostById(postId: String): PostEntity? {
        return try {
            val document = firestore.collection("posts")
                .document(postId)
                .get()
                .await()
            document.toObject(PostEntity::class.java)
        } catch (e: Exception) {
            Log.e("FirebasePostService", "Error fetching post for postId $postId", e)
            null
        }
    }

    suspend fun getAllPosts(): List<PostEntity> {
        val result = firestore.collection("posts").get().await()
        return result.toObjects(PostEntity::class.java)
    }

    suspend fun getPostsByUser(userId: String): List<PostEntity> {
        val postsRef = firestore.collection("posts")
            .whereEqualTo("userId", userId)

        val result = postsRef.get().await()
        return result.toObjects(PostEntity::class.java)
    }

    suspend fun getLikeStatus(postId: String, likerId: String): Boolean {
        val likeRef = firestore.collection("posts")
            .document(postId)
            .collection("likes")
            .document(likerId)
            .get()
            .await()

        return likeRef.exists()
    }
}
