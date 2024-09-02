package com.akansu.sosyashare.domain.repository

import java.io.File

interface StorageRepository {
    suspend fun uploadProfilePicture(file: File): String
    suspend fun uploadPostPicture(file: File): String
}
