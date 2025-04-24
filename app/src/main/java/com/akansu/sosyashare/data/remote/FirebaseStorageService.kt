package com.akansu.sosyashare.data.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import com.akansu.sosyashare.util.NetworkUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID
import javax.inject.Inject

class FirebaseStorageService @Inject constructor(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val context: Context
) {
    private val offlineErrorMessage = "İnternet bağlantısı yok. Lütfen internet bağlantınızı kontrol edin."

    private fun checkNetworkConnection() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.e("FirebaseStorageService", "No internet connection")
            throw FirebaseFirestoreException("İnternet bağlantısı yok", FirebaseFirestoreException.Code.UNAVAILABLE)
        }
    }

    suspend fun uploadProfilePicture(file: File): String {
        try {
            checkNetworkConnection()
            val userId = auth.currentUser?.uid ?: throw Exception("No authenticated user")
            val uniqueFileName = "${UUID.randomUUID()}_${file.name}"
            val ref = storage.reference.child("profile_pictures/$userId/$uniqueFileName.jpg")
            ref.putFile(Uri.fromFile(file)).await()
            return ref.downloadUrl.await().toString()
        } catch (e: FirebaseFirestoreException) {
            Log.e("FirebaseStorageService", "Network error during profile picture upload: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.e("FirebaseStorageService", "Error uploading profile picture: ${e.message}")
            throw e
        }
    }

    suspend fun uploadPostPicture(file: File): String {
        try {
            checkNetworkConnection()
            val userId = auth.currentUser?.uid ?: throw Exception("No authenticated user")
            val uniqueFileName = "${UUID.randomUUID()}_${file.name}"
            val ref = storage.reference.child("post_pictures/$userId/$uniqueFileName")
            ref.putFile(Uri.fromFile(file)).await()
            return ref.downloadUrl.await().toString()
        } catch (e: FirebaseFirestoreException) {
            Log.e("FirebaseStorageService", "Network error during post picture upload: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.e("FirebaseStorageService", "Error uploading post picture: ${e.message}")
            throw e
        }
    }

    suspend fun uploadBackgroundImage(file: File): String {
        try {
            checkNetworkConnection()
            val userId = auth.currentUser?.uid ?: throw Exception("No authenticated user")
            val uniqueFileName = "${UUID.randomUUID()}_${file.name}"
            val ref = storage.reference.child("background_images/$userId/$uniqueFileName.jpg")
            ref.putFile(Uri.fromFile(file)).await()
            return ref.downloadUrl.await().toString()
        } catch (e: FirebaseFirestoreException) {
            Log.e("FirebaseStorageService", "Network error during background image upload: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.e("FirebaseStorageService", "Error uploading background image: ${e.message}")
            throw e
        }
    }
}
