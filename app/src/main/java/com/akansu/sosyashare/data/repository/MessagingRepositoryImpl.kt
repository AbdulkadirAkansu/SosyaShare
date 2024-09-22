package com.akansu.sosyashare.data.repository

import android.content.Context
import com.akansu.sosyashare.data.remote.FirebaseMessagingService
import com.akansu.sosyashare.domain.repository.MessagingRepository
import javax.inject.Inject

class MessagingRepositoryImpl @Inject constructor(
    private val messagingService: FirebaseMessagingService
) : MessagingRepository {

    override suspend fun getFCMTokenByUserId(userId: String): String? {
        return messagingService.getFCMTokenByUserId(userId)
    }

    override suspend fun sendFCMNotification(
        context: Context,
        fcmToken: String,
        title: String,
        message: String
    ) {
        messagingService.sendFCMNotification(context, fcmToken, title, message)
    }
}