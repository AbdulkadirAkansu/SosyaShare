package com.akansu.sosyashare.data.remote

import android.content.Context
import android.util.Log
import com.akansu.sosyashare.data.model.PostEntity
import com.akansu.sosyashare.util.NetworkUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebasePostService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val context: Context
) {
    private val offlineErrorMessage = "İnternet bağlantısı yok. Lütfen internet bağlantınızı kontrol edin."

    private fun checkNetworkConnection() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.e("FirebasePostService", "No internet connection")
            throw FirebaseFirestoreException("İnternet bağlantısı yok", FirebaseFirestoreException.Code.UNAVAILABLE)
        }
    }

    suspend fun getUserPosts(userId: String): List<PostEntity> {
        try {
            checkNetworkConnection()
            val currentUser = auth.currentUser
            if (currentUser == null) {
                throw Exception("Giriş yapmamış kullanıcı.")
            }
            val postsRef = firestore.collection("posts").whereEqualTo("userId", userId)
            val result = postsRef.get().await()
            return result.toObjects(PostEntity::class.java)
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                Log.e("FirebasePostService", offlineErrorMessage)
            } else {
                Log.e("FirebasePostService", "Error getting user posts: ${e.message}")
            }
            return emptyList()
        } catch (e: Exception) {
            Log.e("FirebasePostService", "Error getting user posts: ${e.message}")
            return emptyList()
        }
    }

    suspend fun createPost(userId: String, post: PostEntity) {
        try {
            checkNetworkConnection()
            val postRef = firestore.collection("posts")
                .document(post.id)
            postRef.set(post).await()
        } catch (e: Exception) {
            Log.e("FirebasePostService", "Error creating post: ${e.message}")
            throw e
        }
    }

    suspend fun deletePost(postId: String, postImageUrl: String) {
        try {
            checkNetworkConnection()
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
            throw e
        }
    }

    suspend fun likePost(postId: String, likerId: String) {
        try {
            checkNetworkConnection()
            Log.d("FirebasePostService", "Liking Post: postId=$postId by User: $likerId")
            val postRef = firestore.collection("posts").document(postId)
            val likeRef = postRef.collection("likes").document(likerId)

            firestore.runTransaction { transaction ->
                val postSnapshot = transaction.get(postRef)
                val likeSnapshot = transaction.get(likeRef)

                if (!likeSnapshot.exists()) {
                    val newLikeCount = (postSnapshot.getLong("likeCount") ?: 0) + 1
                    transaction.update(postRef, "likeCount", newLikeCount)
                    val likedByList =
                        postSnapshot.get("likedBy") as? MutableList<String> ?: mutableListOf()
                    likedByList.add(likerId)
                    transaction.update(postRef, "likedBy", likedByList)

                    transaction.set(likeRef, mapOf("userId" to likerId))

                    Log.d(
                        "FirebasePostService",
                        "Post Liked: postId=$postId, newLikeCount=$newLikeCount, likedBy=$likedByList"
                    )
                } else {
                    Log.d("FirebasePostService", "User $likerId has already liked post $postId")
                }
            }.await()
        } catch (e: Exception) {
            Log.e("FirebasePostService", "Error liking post: ${e.message}")
            throw e
        }
    }

    suspend fun getLikedUserIds(postId: String): List<String> {
        return try {
            checkNetworkConnection()
            val likesCollection = firestore.collection("posts")
                .document(postId)
                .collection("likes")
                .get()
                .await()

            likesCollection.documents.mapNotNull { it.id }
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                Log.e("FirebasePostService", offlineErrorMessage)
            } else {
                Log.e("FirebasePostService", "Error fetching liked user IDs for postId $postId", e)
            }
            emptyList()
        } catch (e: Exception) {
            Log.e("FirebasePostService", "Error fetching liked user IDs for postId $postId", e)
            emptyList()
        }
    }

    suspend fun updatePost(post: PostEntity) {
        try {
            checkNetworkConnection()
            firestore.collection("posts").document(post.id).set(post).await()
            Log.d("FirebasePostService", "Post updated: ${post.id}")
        } catch (e: Exception) {
            Log.e("FirebasePostService", "Error updating post: ${e.message}")
            throw e
        }
    }

    suspend fun unlikePost(postId: String, likerId: String) {
        try {
            checkNetworkConnection()
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
                    val likedByList =
                        postSnapshot.get("likedBy") as? MutableList<String> ?: mutableListOf()
                    likedByList.remove(likerId)
                    transaction.update(postRef, "likedBy", likedByList)

                    // Beğeni kaydının silinmesi
                    transaction.delete(likeRef)

                    Log.d(
                        "FirebasePostService",
                        "Post Unliked: postId=$postId, newLikeCount=$newLikeCount, likedBy=$likedByList"
                    )
                } else {
                    Log.d("FirebasePostService", "User $likerId had not liked post $postId")
                }
            }.await()
        } catch (e: Exception) {
            Log.e("FirebasePostService", "Error unliking post: ${e.message}")
            throw e
        }
    }

    suspend fun getPostById(postId: String): PostEntity? {
        return try {
            checkNetworkConnection()
            val document = firestore.collection("posts")
                .document(postId)
                .get()
                .await()
            document.toObject(PostEntity::class.java)
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                Log.e("FirebasePostService", offlineErrorMessage)
            } else {
                Log.e("FirebasePostService", "Error fetching post for postId $postId", e)
            }
            null
        } catch (e: Exception) {
            Log.e("FirebasePostService", "Error fetching post for postId $postId", e)
            null
        }
    }

    suspend fun getAllPosts(): List<PostEntity> {
        return try {
            checkNetworkConnection()
            val result = firestore.collection("posts").get().await()
            result.toObjects(PostEntity::class.java)
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                Log.e("FirebasePostService", offlineErrorMessage)
            } else {
                Log.e("FirebasePostService", "Error getting all posts: ${e.message}")
            }
            emptyList()
        } catch (e: Exception) {
            Log.e("FirebasePostService", "Error getting all posts: ${e.message}")
            emptyList()
        }
    }

    suspend fun getPostsByUser(userId: String): List<PostEntity> {
        return try {
            checkNetworkConnection()
            val postsRef = firestore.collection("posts")
                .whereEqualTo("userId", userId)

            val result = postsRef.get().await()
            result.toObjects(PostEntity::class.java)
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                Log.e("FirebasePostService", offlineErrorMessage)
            } else {
                Log.e("FirebasePostService", "Error getting posts by user: ${e.message}")
            }
            emptyList()
        } catch (e: Exception) {
            Log.e("FirebasePostService", "Error getting posts by user: ${e.message}")
            emptyList()
        }
    }

    suspend fun getLikeStatus(postId: String, likerId: String): Boolean {
        return try {
            checkNetworkConnection()
            val likeRef = firestore.collection("posts")
                .document(postId)
                .collection("likes")
                .document(likerId)
                .get()
                .await()

            likeRef.exists()
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                Log.e("FirebasePostService", offlineErrorMessage)
            } else {
                Log.e("FirebasePostService", "Error getting like status: ${e.message}")
            }
            false
        } catch (e: Exception) {
            Log.e("FirebasePostService", "Error getting like status: ${e.message}")
            false
        }
    }
}
