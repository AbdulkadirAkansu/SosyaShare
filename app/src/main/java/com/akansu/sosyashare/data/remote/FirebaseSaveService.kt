package com.akansu.sosyashare.data.remote

import android.util.Log
import com.akansu.sosyashare.data.mapper.SaveMapper
import com.akansu.sosyashare.data.model.SaveEntity
import com.akansu.sosyashare.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseSaveService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository
) {

    private fun currentUserCollection(currentUserId: String) =
        firestore.collection("users").document(currentUserId).collection("saved_posts")

    suspend fun savePost(postId: String) {
        val currentUserId = userRepository.getCurrentUserId() ?: return
        Log.d("FirebaseSaveService", "Saving post with postId=$postId for user=$currentUserId")

        if (postId.isEmpty()) {
            Log.e("FirebaseSaveService", "postId is empty, cannot save.")
            return
        }

        val saveEntity = SaveEntity(postId = postId, userId = currentUserId)
        currentUserCollection(currentUserId).document(postId).set(SaveMapper.fromEntity(saveEntity))
            .await()
    }

    suspend fun removeSavedPost(postId: String) {
        val currentUserId = userRepository.getCurrentUserId() ?: return
        Log.d(
            "FirebaseSaveService",
            "Removing saved post with postId=$postId for user=$currentUserId"
        )
        currentUserCollection(currentUserId).document(postId).delete().await()
    }

    suspend fun getSavedPosts(): List<SaveEntity> {
        val currentUserId = userRepository.getCurrentUserId() ?: return emptyList()
        Log.d("FirebaseSaveService", "Fetching saved posts for user $currentUserId")
        val documents = currentUserCollection(currentUserId).get().await().documents
        val savedPosts = documents.mapNotNull { doc ->
            val saveEntity = SaveMapper.toEntity(doc.data ?: emptyMap())
            if (saveEntity == null || saveEntity.postId.isEmpty()) {
                Log.e("FirebaseSaveService", "Invalid SaveEntity or postId is empty: $doc")
                null
            } else {
                Log.d("FirebaseSaveService", "Loaded save entity: $saveEntity")
                saveEntity
            }
        }
        Log.d(
            "FirebaseSaveService",
            "Fetched ${savedPosts.size} saved posts for user $currentUserId"
        )
        return savedPosts
    }
}
