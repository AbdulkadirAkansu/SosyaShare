package com.akansu.sosyashare.data.remote

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID
import javax.inject.Inject

class FirebaseStorageService @Inject constructor() {
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun uploadProfilePicture(file: File): String {
        val userId = auth.currentUser?.uid ?: throw Exception("No authenticated user")
        val uniqueFileName = "${UUID.randomUUID()}_${file.name}"
        val ref = storage.reference.child("profile_pictures/$userId/$uniqueFileName.jpg")
        ref.putFile(Uri.fromFile(file)).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadPostPicture(file: File): String {
        val userId = auth.currentUser?.uid ?: throw Exception("No authenticated user")
        val uniqueFileName = "${UUID.randomUUID()}_${file.name}"
        val ref = storage.reference.child("post_pictures/$userId/$uniqueFileName")
        ref.putFile(Uri.fromFile(file)).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadBackgroundImage(file: File): String {
        val userId = auth.currentUser?.uid ?: throw Exception("No authenticated user")
        val uniqueFileName = "${UUID.randomUUID()}_${file.name}"
        val ref = storage.reference.child("background_images/$userId/$uniqueFileName.jpg")
        ref.putFile(Uri.fromFile(file)).await()
        return ref.downloadUrl.await().toString()
    }
}
