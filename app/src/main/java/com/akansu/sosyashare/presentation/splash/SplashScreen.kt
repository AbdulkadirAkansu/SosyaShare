package com.akansu.sosyashare.presentation.splash

import VideoBackgroundPlayerWithSurfaceView
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.akansu.sosyashare.R
import com.akansu.sosyashare.presentation.login.viewmodel.AuthViewModel
import com.akansu.sosyashare.util.poppinsFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun SplashScreen(navController: NavController, authViewModel: AuthViewModel? = null) {
    val videoUri = Uri.parse("android.resource://${LocalContext.current.packageName}/${R.raw.splash}")
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500)
        showContent = true
        delay(120000)
        val startDestination = if (authViewModel?.isUserLoggedIn() == true) "home" else "login"
        navController.navigate(startDestination) {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Video arka planı ve karartma efekti
        VideoBackgroundPlayerWithSurfaceView(videoUri = videoUri)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)) // Yarı saydam karartma efekti
        )

        // UI Bileşenleri yukarıya kaydırıldı
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.CenterVertically)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(top = 80.dp), // Hafif yukarı kaydırma
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top // En üste kaydırıldı
            ) {
                // Logoyu gösterecek bölüm
                PulsatingLogo()
                Spacer(modifier = Modifier.height(40.dp))

                // Ana başlık (Daha profesyonel metin stili)
                AnimatedText(
                    "Capture, Share & Connect",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp, // Metin boyutları daha modern
                        color = Color.White,
                        fontFamily = poppinsFontFamily,
                        letterSpacing = 1.2.sp // Biraz daha geniş aralık
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Alt başlık (Daha net bir açıklama)
                AnimatedSubtitle(
                    "Join a global community. Discover stories, create moments, and engage with people everywhere.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp, // Daha uyumlu boyut
                        color = Color.White.copy(alpha = 0.85f),
                        fontFamily = poppinsFontFamily,
                        lineHeight = 24.sp // Daha net bir okuma deneyimi
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Başlangıç butonu (buton boyutları ve animasyonu modernize edildi)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedStartButton(
                text = "Get Started",
                onClick = {
                    val destination = if (authViewModel?.isUserLoggedIn() == true) "home" else "login"
                    navController.navigate(destination) {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}

@Composable
fun PulsatingLogo() {
    val infiniteTransition = rememberInfiniteTransition()

    // Ölçek animasyonu (Logo büyüyüp küçülür)
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Parlaklık animasyonu (Logo etrafında hafif parıltı)
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(140.dp)  // Boyut sabit bırakıldı, ölçekle ayarlandı
            .scale(scale)
            .background(
                color = Color.White.copy(alpha = 0.1f),  // Hafif bir arka plan
                shape = RoundedCornerShape(70.dp)
            )
            .graphicsLayer {
                shadowElevation = 6f  // Hafif gölge efekti
                shape = RoundedCornerShape(70.dp)
                clip = true
            }
    ) {
        // Parıltı efekti (çok hafif bir parlama)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = glowAlpha * 0.2f),
                radius = size.minDimension / 2.5f,
                center = center
            )
        }

        // "S" harfi için sade stil
        Text(
            "S",
            color = Color.White,
            fontSize = 64.sp, // Boyut sabit
            fontWeight = FontWeight.Bold,
            fontFamily = poppinsFontFamily,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun AnimatedText(text: String, style: TextStyle, textAlign: TextAlign) {
    var visibleText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        text.forEachIndexed { index, char ->
            visibleText += char
            delay(30)
        }
    }

    Text(
        text = visibleText,
        style = style,
        textAlign = textAlign
    )
}

@Composable
fun AnimatedSubtitle(text: String, style: TextStyle, textAlign: TextAlign) {
    var visibleText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        text.forEachIndexed { index, char ->
            visibleText += char
            delay(20)
        }
    }

    Text(
        text = visibleText,
        style = style,
        textAlign = textAlign
    )
}

@Composable
fun AnimatedStartButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    // Infinite transition for animation
    val infiniteTransition = rememberInfiniteTransition()

    // Scale animation (button grows and shrinks slightly)
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Color animation (button color shifts subtly)
    val buttonColor by infiniteTransition.animateColor(
        initialValue = Color.White,
        targetValue = Color(0xFFBBDEFB),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Animated visibility with scaling effect
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom)
    ) {
        Button(
            onClick = onClick,
            modifier = modifier
                .scale(scale) // Apply scale effect to the button
                .fillMaxWidth(0.6f)
                .height(50.dp), // Daha uyumlu boyut
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor), // Color animation applied
            shape = RoundedCornerShape(16.dp) // Daha modern köşeler
        ) {
            Text(
                text = text,
                color = Color(0xFF3949AB),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp // Button metni daha uyumlu hale getirildi
            )
        }
    }
}
