package com.akansu.sosyashare.data.repository

import android.net.Uri
import com.akansu.sosyashare.data.remote.FirebaseStorageService
import com.akansu.sosyashare.domain.repository.StorageRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepositoryImpl @Inject constructor(
    private val firebaseStorageService: FirebaseStorageService
) : StorageRepository {
    override suspend fun uploadProfilePicture(uri: Uri): String {
        return firebaseStorageService.uploadProfilePicture(uri)
    }

    override suspend fun uploadPostPicture(uri: Uri): String {
        return firebaseStorageService.uploadPostPicture(uri)
    }
}
