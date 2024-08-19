package com.akansu.sosyashare.data.repository

import com.akansu.sosyashare.data.model.SaveEntity
import com.akansu.sosyashare.data.remote.FirebaseSaveService
import com.akansu.sosyashare.domain.model.Save
import com.akansu.sosyashare.domain.repository.SaveRepository
import javax.inject.Inject

class SaveRepositoryImpl @Inject constructor(
    private val saveService: FirebaseSaveService
) : SaveRepository {

    override suspend fun savePost(postId: String) {
        saveService.savePost(postId)
    }

    override suspend fun removeSavedPost(postId: String) {
        saveService.removeSavedPost(postId)
    }

    override suspend fun getSavedPosts(): List<Save> {
        return saveService.getSavedPosts().map { entity ->
            Save(
                postId = entity.postId,
                userId = entity.userId,
                savedAt = entity.timestamp
            )
        }
    }
}
