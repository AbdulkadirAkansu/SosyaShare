package com.akansu.sosyashare.util

import android.content.Context
import android.util.Log
import com.akansu.sosyashare.R
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

suspend fun getAccessToken(context: Context): String? = withContext(Dispatchers.IO) {
    try {
        // İmza hatası nedeniyle alternatif bir çözüm deneyeceğiz
    val inputStream = context.resources.openRawResource(R.raw.sosyasharetoken)
        
        // Credential oluşturmayı ve scope belirtmeyi daha basit hale getirelim
        val googleCredentials = GoogleCredentials.fromStream(inputStream)
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

        try {
            // Token'ı manuel olarak yenileyelim
            googleCredentials.refresh()
            val token = googleCredentials.accessToken.tokenValue
            Log.d("AccessToken", "Token başarıyla alındı")
            return@withContext token
        } catch (e: IOException) {
            // Hata durumunda ek bilgi logla
            Log.e("AccessToken", "Token yenilenirken hata: ${e.message}")
            return@withContext null
        }
    } catch (e: Exception) {
        Log.e("AccessToken", "Genel hata: ${e.message}")
        return@withContext null
    }
}

