package com.akansu.sosyashare.util

import android.content.Context
import com.akansu.sosyashare.R
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


suspend fun getAccessToken(context: Context): String? = withContext(Dispatchers.IO) {
    val inputStream = context.resources.openRawResource(R.raw.sosyasharetoken)
    val googleCredentials = GoogleCredentials
        .fromStream(inputStream)
        .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))

    googleCredentials.refreshIfExpired()
    return@withContext googleCredentials.accessToken.tokenValue
}

