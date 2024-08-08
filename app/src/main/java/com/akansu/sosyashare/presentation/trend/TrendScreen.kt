package com.akansu.sosyashare.presentation.trend

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.akansu.sosyashare.util.poppinsFontFamily


@Composable
fun TrendScreen(navController: NavHostController) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Trend Screen",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = poppinsFontFamily
        )
    }
}
