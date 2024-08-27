package com.akansu.sosyashare.data.repository

import com.akansu.sosyashare.data.mapper.toDomainModel
import com.akansu.sosyashare.data.model.UserPrivacyEntity
import com.akansu.sosyashare.data.remote.FirebaseUserPrivacyService
import com.akansu.sosyashare.domain.model.UserPrivacy
import com.akansu.sosyashare.domain.repository.UserPrivacyRepository
import com.google.firebase.firestore.ListenerRegistration
import javax.inject.Inject

class UserPrivacyRepositoryImpl @Inject constructor(
    private val service: FirebaseUserPrivacyService
) : UserPrivacyRepository {

    override suspend fun getUserPrivacy(userId: String): UserPrivacy? {
        return service.getUserPrivacy(userId)?.toDomainModel()
    }

    override suspend fun updateUserPrivacy(userPrivacy: UserPrivacy) {
        val entity = UserPrivacyEntity(
            userId = userPrivacy.userId,
            isPrivate = userPrivacy.isPrivate,
            allowedFollowers = userPrivacy.allowedFollowers
        )
        service.updateUserPrivacyWithTransaction(entity)
    }

    override suspend fun updateUserPrivacySetting(userId: String, isPrivate: Boolean) {
        service.updateUserPrivacySetting(userId, isPrivate)
    }

    override fun addUserPrivacySettingListener(userId: String, onPrivacyChanged: (Boolean) -> Unit): ListenerRegistration {
        return service.addUserPrivacySettingListener(userId, onPrivacyChanged)
    }

    override suspend fun addAllowedFollower(userId: String, followerId: String) {
        service.addAllowedFollower(userId, followerId)
    }

    // Yeni eklenen işlev: Kullanıcıdan izin verilen bir takipçiyi kaldırır
    override suspend fun removeAllowedFollower(userId: String, followerId: String) {
        service.removeAllowedFollower(userId, followerId)
    }

}
