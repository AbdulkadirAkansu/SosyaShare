package com.akansu.sosyashare.domain.usecase

import android.net.Uri
import com.akansu.sosyashare.domain.repository.StorageRepository
import javax.inject.Inject

class UploadProfilePictureUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(uri: Uri): String {
        return storageRepository.uploadProfilePicture(uri)
    }
}
