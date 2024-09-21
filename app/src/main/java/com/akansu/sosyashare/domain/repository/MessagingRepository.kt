package com.akansu.sosyashare.domain.repository

import android.content.Context

interface MessagingRepository {
    suspend fun getFCMTokenByUserId(userId: String): String?
    suspend fun sendFCMNotification(context: Context, fcmToken: String, title: String, message: String)
}