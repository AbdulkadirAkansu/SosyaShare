
package com.akansu.sosyashare.data.remote

import android.util.Log
import com.akansu.sosyashare.data.model.PrivateAccountEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebasePrivateAccountService @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getPrivateAccount(userId: String): PrivateAccountEntity? {
        return try {
            val document = firestore.collection("private_accounts").document(userId).get().await()
            document.toObject(PrivateAccountEntity::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updatePrivateAccount(privateAccount: PrivateAccountEntity): Boolean {
        return try {
            firestore.collection("private_accounts")
                .document(privateAccount.userId)
                .set(privateAccount)
                .await()
            true
        } catch (e: Exception) {
            Log.e("FirebasePrivateAccountService", "Failed to update private account", e)
            false
        }
    }

    suspend fun addAllowedFollower(userId: String, followerId: String) {
        val privateAccount = getPrivateAccount(userId)
        privateAccount?.let {
            val updatedFollowers = it.allowedFollowers.toMutableList()
            if (!updatedFollowers.contains(followerId)) {
                updatedFollowers.add(followerId)
                updatePrivateAccount(it.copy(allowedFollowers = updatedFollowers))
            }
        }
    }

    suspend fun removeAllowedFollower(userId: String, followerId: String) {
        val privateAccount = getPrivateAccount(userId)
        privateAccount?.let {
            val updatedFollowers = it.allowedFollowers.toMutableList()
            if (updatedFollowers.contains(followerId)) {
                updatedFollowers.remove(followerId)
                updatePrivateAccount(it.copy(allowedFollowers = updatedFollowers))
            }
        }
    }

    suspend fun updateUserPrivacySetting(userId: String, isPrivate: Boolean) {
        firestore.collection("private_accounts").document(userId).update("isPrivate", isPrivate).await()
    }

    fun addUserPrivacySettingListener(userId: String, onPrivacyChanged: (Boolean) -> Unit): ListenerRegistration {
        return firestore.collection("private_accounts").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("FirebasePrivateAccountService", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val isPrivate = snapshot.getBoolean("isPrivate") ?: false
                    onPrivacyChanged(isPrivate)
                } else {
                    Log.d("FirebasePrivateAccountService", "Current data: null")
                }
            }
    }

    suspend fun updatePrivateAccountWithTransaction(privateAccount: PrivateAccountEntity): Boolean {
        return try {
            firestore.runTransaction { transaction ->
                val documentRef = firestore.collection("private_accounts").document(privateAccount.userId)
                transaction.set(documentRef, privateAccount)
                null
            }.await()
            true
        } catch (e: Exception) {
            Log.e("FirebasePrivateAccountService", "Failed to update private account in transaction", e)
            false
        }
    }


}
