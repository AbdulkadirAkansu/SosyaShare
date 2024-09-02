package com.akansu.sosyashare.data.repository

import java.io.File
import com.akansu.sosyashare.data.remote.FirebaseStorageService
import com.akansu.sosyashare.domain.repository.StorageRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepositoryImpl @Inject constructor(
    private val firebaseStorageService: FirebaseStorageService
) : StorageRepository {

    override suspend fun uploadProfilePicture(file: File): String {
        return firebaseStorageService.uploadProfilePicture(file)
    }

    override suspend fun uploadPostPicture(file: File): String {
        return firebaseStorageService.uploadPostPicture(file)
    }
}
