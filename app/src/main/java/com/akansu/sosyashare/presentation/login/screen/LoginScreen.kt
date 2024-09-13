package com.akansu.sosyashare.presentation.login.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.* // Box, Column, Modifier ve Spacer için gerekli
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.* // ButtonDefaults, Button, Text, TextButton
import androidx.compose.runtime.* // remember, mutableStateOf, Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.akansu.sosyashare.presentation.login.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {
    val backgroundColors = listOf(
        Color(0xFFFF007F), Color(0xFF6200EA), Color(0xFF03DAC6),
        Color(0xFFFFA726), Color(0xFF00E5FF), Color(0xFFFFEB3B)
    )

    var currentColor by remember { mutableStateOf(backgroundColors[0]) }
    var colorIndex by remember { mutableStateOf(0) }

    // Renk animasyonu, belirli aralıklarla arka plan rengini değiştirecek
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            colorIndex = (colorIndex + 1) % backgroundColors.size
            currentColor = backgroundColors[colorIndex]
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentColor)
    ) {
        AnimatedBackground()

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Animasyonlu kayan yazılar
            TypewriterText()

            Spacer(modifier = Modifier.weight(1f))

            LoginOptionsBox(navController)
        }
    }
}

@Composable
fun AnimatedBackground() {
    val circles = remember { List(50) { Pair(Math.random(), Math.random()) } }

    circles.forEach { (x, y) ->
        val animatedY by rememberInfiniteTransition().animateFloat(
            initialValue = y.toFloat(),
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = (8000 + Math.random() * 6000).toInt(), easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        Box(
            modifier = Modifier
                .size((20..40).random().dp) // Farklı boyutlarda baloncuklar
                .offset(x = (x * 400).dp, y = (animatedY * 1000).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
        )
    }
}

@Composable
fun TypewriterText() {
    val messages = listOf(
        "Paylaş ●",
        "Keşfet ●",
        "Bağlan ●",
        "Takip Et ●",
        "Beğen ●",
        "Yorum Yap ●",
        "Story Paylaş ●",
        "Trendleri Keşfet ●"
    )

    var currentMessage by remember { mutableStateOf("") }
    var messageIndex by remember { mutableStateOf(0) }
    var currentDisplayText by remember { mutableStateOf("") }

    LaunchedEffect(key1 = messageIndex) {
        currentMessage = messages[messageIndex]
        currentDisplayText = ""
        for (i in currentMessage.indices) {
            currentDisplayText = currentMessage.take(i + 1)
            delay(150) // Harf harf yazma efekti
        }
        delay(2000) // Yazı yazıldıktan sonra 2 saniye bekle
        messageIndex = (messageIndex + 1) % messages.size
    }

    Text(
        text = currentDisplayText,
        color = Color.White,
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
fun LoginOptionsBox(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.3f)
            .background(
                color = Color.Black,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            )
            .padding(20.dp)
    ) {
        LoginOptions(navController)
    }
}

@Composable
fun LoginOptions(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SocialLoginButton(
            text = "Google ile devam et",
            backgroundColor = Color(0xFFE0E0E0),
            textColor = Color.Black
        )

        SocialLoginButton(
            text = "E-posta ile kaydol",
            backgroundColor = Color(0xFF404040),
            textColor = Color.White
        )



        Divider(
            modifier = Modifier.fillMaxWidth(0.8f),
            thickness = 1.dp,
            color = Color.Gray
        )


        OutlinedButton(
            onClick = { /* TODO: Implement login */ },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(50.dp),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, Color.Gray),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) {
            Text("Oturum aç", fontSize = 16.sp)
        }
    }
}

@Composable
fun SocialLoginButton(
    text: String,
    backgroundColor: Color,
    textColor: Color
) {
    Button(
        onClick = { /* TODO: Implement social login */ },
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(28.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 16.sp
        )
    }
}
