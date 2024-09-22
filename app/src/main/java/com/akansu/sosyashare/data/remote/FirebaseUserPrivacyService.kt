package com.akansu.sosyashare.data.remote

import android.util.Log
import com.akansu.sosyashare.data.model.UserPrivacyEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseUserPrivacyService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val TAG = "FirebaseUserPrivacyService"

    suspend fun fetchIsPrivateDirectly(userId: String): Boolean {
        return try {
            Log.d(TAG, "Fetching isPrivate value directly for userId: $userId")

            val document = firestore.collection("user_privacy")
                .document(userId)
                .get()
                .await()

            val isPrivate = document.getBoolean("isPrivate") ?: false
            Log.d(TAG, "Fetched isPrivate value for userId $userId: $isPrivate")
            isPrivate
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching isPrivate directly for userId: $userId", e)
            false
        }
    }

    suspend fun getUserPrivacy(userId: String): UserPrivacyEntity? {
        return try {
            val document = firestore.collection("user_privacy")
                .document(userId)
                .get(Source.SERVER)
                .await()

            if (document.exists()) {
                val userPrivacy = document.toObject(UserPrivacyEntity::class.java)
                Log.d(TAG, "Fetched isPrivate value: ${userPrivacy?.isPrivate}")
                userPrivacy
            } else {
                Log.w(TAG, "Document does not exist for userId: $userId")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user privacy for userId: $userId", e)
            null
        }
    }

    suspend fun updateUserPrivacySetting(userId: String, isPrivate: Boolean) {
        try {
            firestore.collection("user_privacy").document(userId)
                .update("isPrivate", isPrivate)
                .await()
            Log.d(TAG, "Successfully updated isPrivate to $isPrivate for userId: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update isPrivate for userId: $userId", e)
            throw e
        }
    }

    fun addUserPrivacySettingListener(
        userId: String,
        onPrivacyChanged: (Boolean) -> Unit
    ): ListenerRegistration {
        return firestore.collection("user_privacy").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val isPrivate = snapshot.getBoolean("isPrivate") ?: false
                    Log.d(TAG, "Real-time update received for isPrivate: $isPrivate")
                    onPrivacyChanged(isPrivate)
                } else {
                    Log.d(TAG, "No data for userId: $userId")
                }
            }
    }

    suspend fun updateUserPrivacyWithTransaction(
        userId: String,
        followerId: String,
        isFollowing: Boolean
    ) {
        try {
            firestore.runTransaction { transaction ->
                val documentRef = firestore.collection("user_privacy").document(userId)
                val snapshot = transaction.get(documentRef)

                val currentAllowedFollowers =
                    snapshot.get("allowedFollowers") as? List<String> ?: emptyList()
                val updatedFollowers = if (isFollowing) {
                    currentAllowedFollowers.toMutableList()
                        .apply { if (!contains(followerId)) add(followerId) }
                } else {
                    currentAllowedFollowers.toMutableList().apply { remove(followerId) }
                }

                transaction.update(documentRef, "allowedFollowers", updatedFollowers)
            }.await()
            Log.d(TAG, "Successfully updated allowed followers for userId: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update allowed followers for userId: $userId", e)
            throw e
        }
    }

    suspend fun addAllowedFollower(userId: String, followerId: String) {
        try {
            updateUserPrivacyWithTransaction(userId, followerId, true)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding allowed follower $followerId for userId: $userId", e)
            throw e
        }
    }

    suspend fun removeAllowedFollower(userId: String, followerId: String) {
        try {
            updateUserPrivacyWithTransaction(userId, followerId, false)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing allowed follower $followerId for userId: $userId", e)
            throw e
        }
    }
}
