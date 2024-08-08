package com.akansu.sosyashare.data.remote

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseStorageService @Inject constructor() {
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun uploadProfilePicture(uri: Uri): String {
        val userId = auth.currentUser?.uid ?: throw Exception("No authenticated user")
        val ref = storage.reference.child("profile_pictures/$userId.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadPostPicture(uri: Uri): String {
        val userId = auth.currentUser?.uid ?: throw Exception("No authenticated user")
        val ref = storage.reference.child("post_pictures/${userId}/${uri.lastPathSegment}")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}
