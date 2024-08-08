package com.akansu.sosyashare.domain.repository

import android.net.Uri

interface StorageRepository {
    suspend fun uploadProfilePicture(uri: Uri): String
    suspend fun uploadPostPicture(uri: Uri): String
}
