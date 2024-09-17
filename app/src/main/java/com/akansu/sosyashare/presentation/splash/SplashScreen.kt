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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
        delay(150000)
        val startDestination = if (authViewModel?.isUserLoggedIn() == true) "home" else "login"
        navController.navigate(startDestination) {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Video background
        VideoBackgroundPlayerWithSurfaceView(videoUri = videoUri)

        // Darken the video slightly for better readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )

        // Enhanced bubble animation
        BubbleAnimation()

        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = tween(1000)) + expandVertically(expandFrom = Alignment.Top)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo stays at the top, no change in its position
                OptimizedLogo(modifier = Modifier.padding(top = 104.dp))

                // Text content, shifted upwards
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top, // Yazıları yukarıya hizalar
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 150.dp) // Yazıları daha yukarı almak için padding ekledim
                ) {
                    // Main title
                    AnimatedText(
                        "Capture, Share & Connect",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 36.sp,
                            color = Color.White,
                            letterSpacing = 1.5.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp)) // Daha küçük boşluk

                    // Subtitle
                    AnimatedSubtitle(
                        "Join a global community. Discover stories, create moments, and engage with people everywhere.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 24.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                // Button stays at the bottom
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
}

@Composable
fun OptimizedLogo(modifier: Modifier = Modifier) {
    val scaleAnimation = rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rotationAnimation = rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing)
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(150.dp)
            .scale(scaleAnimation.value)
    ) {
        Canvas(modifier = Modifier.size(150.dp)) {
            rotate(rotationAnimation.value) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.7f),
                            Color.White.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    radius = size.minDimension / 2,
                    style = Stroke(width = 8.dp.toPx())
                )
            }
        }
        Text(
            "S",
            color = Color.White,
            fontSize = 80.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun BubbleAnimation() {
    val bubbleCount = 40
    val bubbles = remember {
        List(bubbleCount) {
            EnhancedBubble(
                x = Animatable(Random.nextFloat()),
                y = Animatable(Random.nextFloat()),
                size = Random.nextInt(10, 40).dp, // Boyutları biraz daha büyük tuttum
                speed = Random.nextFloat() * 0.015f + 0.005f, // Daha yumuşak ve yavaş hızlar
                color = Color.White.copy(alpha = Random.nextFloat() * 0.4f + 0.2f) // Sadece beyaz tonları ve saydamlık
            )
        }
    }

    LaunchedEffect(Unit) {
        bubbles.forEach { bubble ->
            launch {
                while (true) {
                    bubble.y.animateTo(
                        targetValue = -0.2f,
                        animationSpec = tween(
                            durationMillis = (10000..20000).random(),
                            easing = LinearEasing
                        )
                    )
                    bubble.x.snapTo(Random.nextFloat())
                    bubble.y.snapTo(1.2f)
                }
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        bubbles.forEach { bubble ->
            val xPos = bubble.x.value * size.width
            val yPos = bubble.y.value * size.height
            drawCircle(
                color = bubble.color,
                radius = bubble.size.toPx() / 2,
                center = Offset(xPos, yPos),
                style = Stroke(width = 2f) // Daha ince ve modern çerçeve efekti
            )
        }
    }
}

@Composable
fun AnimatedText(text: String, style: TextStyle, textAlign: TextAlign) {
    var visibleText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        text.forEach { char ->
            visibleText += char
            delay(30)
        }
    }

    Text(
        text = visibleText,
        style = style,
        textAlign = textAlign,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun AnimatedSubtitle(text: String, style: TextStyle, textAlign: TextAlign) {
    var visibleText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        text.forEach { char ->
            visibleText += char
            delay(20)
        }
    }

    Text(
        text = visibleText,
        style = style,
        textAlign = textAlign,
        modifier = Modifier.fillMaxWidth(0.8f)
    )
}

@Composable
fun AnimatedStartButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val buttonScale = rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .scale(buttonScale.value)
            .fillMaxWidth(0.7f)
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFFFFF).copy(alpha = 0.4f))
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

data class EnhancedBubble(
    val x: Animatable<Float, AnimationVector1D>,
    val y: Animatable<Float, AnimationVector1D>,
    val size: Dp,
    val speed: Float,
    val color: Color
)