package com.akansu.sosyashare.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.akansu.sosyashare.presentation.login.viewmodel.AuthViewModel
import com.akansu.sosyashare.presentation.ui.theme.SosyashareTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

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
                    NavGraph(navController = navController, authViewModel = authViewModel)
                }
            }
        }
    }
}
