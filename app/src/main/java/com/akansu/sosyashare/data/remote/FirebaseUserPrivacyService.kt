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

    suspend fun getUserPrivacy(userId: String): UserPrivacyEntity? {
        return try {
            val document = firestore.collection("user_privacy")
                .document(userId)
                .get(Source.SERVER)
                .await()

            if (document.exists()) {
                val userPrivacyEntity = document.toObject(UserPrivacyEntity::class.java)
                Log.d("FirebaseUserPrivacyService", "Fetched UserPrivacyEntity: $userPrivacyEntity")
                userPrivacyEntity
            } else {
                Log.e("FirebaseUserPrivacyService", "Document does not exist for userId: $userId")
                null
            }
        } catch (e: Exception) {
            Log.e("FirebaseUserPrivacyService", "Error getting user privacy for userId: $userId", e)
            null
        }
    }

    suspend fun updateUserPrivacySetting(userId: String, isPrivate: Boolean) {
        try {
            firestore.collection("user_privacy").document(userId).update("isPrivate", isPrivate).await()
            Log.d("FirebaseUserPrivacyService", "Successfully updated isPrivate to $isPrivate for userId: $userId")
        } catch (e: Exception) {
            Log.e("FirebaseUserPrivacyService", "Failed to update isPrivate for userId: $userId", e)
        }
    }

    fun addUserPrivacySettingListener(userId: String, onPrivacyChanged: (Boolean) -> Unit): ListenerRegistration {
        return firestore.collection("user_privacy").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("FirebaseUserPrivacyService", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val isPrivate = snapshot.getBoolean("isPrivate") ?: false
                    onPrivacyChanged(isPrivate)
                } else {
                    Log.d("FirebaseUserPrivacyService", "Current data: null")
                }
            }
    }

    suspend fun updateUserPrivacyWithTransaction(userPrivacyEntity: UserPrivacyEntity): Boolean {
        return try {
            firestore.runTransaction { transaction ->
                val documentRef = firestore.collection("user_privacy").document(userPrivacyEntity.userId)
                transaction.set(documentRef, userPrivacyEntity)
                null
            }.await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseUserPrivacyService", "Failed to update user privacy in transaction", e)
            false
        }
    }

    // Takipçi ekleme işlevi
    suspend fun addAllowedFollower(userId: String, followerId: String) {
        val userPrivacy = getUserPrivacy(userId)
        userPrivacy?.let {
            val updatedFollowers = it.allowedFollowers.toMutableList()
            if (!updatedFollowers.contains(followerId)) {
                updatedFollowers.add(followerId)
                updateUserPrivacyWithTransaction(it.copy(allowedFollowers = updatedFollowers))
            }
        }
    }

    // Takipçi çıkarma işlevi
    suspend fun removeAllowedFollower(userId: String, followerId: String) {
        val userPrivacy = getUserPrivacy(userId)
        userPrivacy?.let {
            val updatedFollowers = it.allowedFollowers.toMutableList()
            if (updatedFollowers.contains(followerId)) {
                updatedFollowers.remove(followerId)
                updateUserPrivacyWithTransaction(it.copy(allowedFollowers = updatedFollowers))
            }
        }
    }
}
