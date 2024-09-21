package com.akansu.sosyashare.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.akansu.sosyashare.data.di.ApiService
import com.akansu.sosyashare.data.di.TokenRequest
import com.akansu.sosyashare.domain.repository.UserRepository
import com.akansu.sosyashare.presentation.login.viewmodel.AuthViewModel
import com.akansu.sosyashare.presentation.ui.theme.SosyashareTheme
import com.akansu.sosyashare.util.getAccessToken
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import okhttp3.Request

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        PermissionManager.requestNotificationPermission(this)

//        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                val token = task.result
//                Log.d("FCM", "FCM Token: $token")
//                sendTokenToServer(token)  // Token'ı burada da sunucuya gönder
//            } else {
//                Log.e("FCM", "Token alınamadı: ${task.exception}")
//            }
//        }

        setContent {
            SosyashareTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = hiltViewModel()
                val systemUiController = rememberSystemUiController()

                val useDarkIcons = !isSystemInDarkTheme()
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = useDarkIcons
                    )
                }

                Surface(color = MaterialTheme.colorScheme.background) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                    ) {
                        NavGraph(
                            navController = navController,
                            authViewModel = authViewModel,
                            userRepository = userRepository
                        )
                    }
                }
            }
        }
    }

//    // `sendTokenToServer` fonksiyonu CoroutineScope içinde çalıştırılmalı
//    private fun sendTokenToServer(token: String) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val accessToken = getAccessToken(this@MainActivity)
//                val url = "https://fcm.googleapis.com/v1/projects/sosyashare/messages:send"
//
//                val jsonBody = """
//            {
//                "message": {
//                    "token": "$token",
//                    "notification": {
//                        "title": "Test Title",
//                        "body": "Test Body"
//                    }
//                }
//            }
//            """
//
//                val client = OkHttpClient()
//                val request = Request.Builder()
//                    .url(url)
//                    .post(jsonBody.toRequestBody("application/json".toMediaType()))
//                    .addHeader("Authorization", "Bearer $accessToken")
//                    .build()
//
//                client.newCall(request).execute().use { response ->
//                    if (response.isSuccessful) {
//                        Log.d("FCM", "Bildirim başarıyla gönderildi")
//                    } else {
//                        Log.e("FCM", "Token gönderiminde hata: ${response.code} - ${response.message}")
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("FCM", "Token gönderim hatası: ${e.message}")
//            }
//        }
//    }
}