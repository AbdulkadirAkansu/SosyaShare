package com.akansu.sosyashare.domain.repository

import com.akansu.sosyashare.domain.model.Save

interface SaveRepository {
    suspend fun savePost(postId: String)
    suspend fun removeSavedPost(postId: String)
    suspend fun getSavedPosts(): List<Save>
}
