package com.akansu.sosyashare.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.akansu.sosyashare.R
import com.akansu.sosyashare.presentation.login.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController, authViewModel: AuthViewModel) {
    LaunchedEffect(Unit) {
        delay(3000) // 2 saniye bekleme süresi
        val startDestination = if (authViewModel.isUserLoggedIn()) {
            "home"
        } else {
            "login"
        }
        navController.navigate(startDestination) {
            popUpTo("splash") { inclusive = true }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val screenHeight = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.67f)
                    .clip(
                        shape = RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp)
                    ),
                contentAlignment = Alignment.TopCenter
            ) {
                Image(
                    painter = painterResource(id = R.drawable.socialmedia),
                    contentDescription = stringResource(id = R.string.app_name),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(screenHeight * 0.07f))

            Text(
                text = "Welcome to SosyaShare",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = poppinsFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Share your moments with the world!",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = poppinsFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            AnimatedGradientButton(
                text = "Get Started",
                onClick = { navController.navigate("login") }
            )
        }
    }
}

@Composable
fun AnimatedGradientButton(
    text: String,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Spacer(modifier = Modifier.height(7.dp))

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .scale(scale)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                    ),
                    shape = RoundedCornerShape(50)
                )
                .padding(horizontal = 30.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = poppinsFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )
        }
    }
}


private val poppinsFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_bold, FontWeight.Bold)
)
