package com.akansu.sosyashare.presentation.login.screen

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.akansu.sosyashare.presentation.login.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.delay
import com.akansu.sosyashare.R

@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {
    val gradients = listOf(
        Brush.verticalGradient(listOf(Color(0xFFFF6B6B), Color(0xFF4ECDC4))),
        Brush.verticalGradient(listOf(Color(0xFF6A82FB), Color(0xFFFC5C7D))),
        Brush.verticalGradient(listOf(Color(0xFF00B4DB), Color(0xFF0083B0))),
        Brush.verticalGradient(listOf(Color(0xFFFDC830), Color(0xFFF37335)))
    )

    var currentGradientIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            currentGradientIndex = (currentGradientIndex + 1) % gradients.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradients[currentGradientIndex])
    ) {
        AnimatedBackground()

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.weight(1f))
            TypewriterText()
            Spacer(modifier = Modifier.weight(1f))
            LoginOptionsBox(navController, viewModel)
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
                .size((20..40).random().dp)
                .offset(x = (x * 400).dp, y = (animatedY * 1000).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
        )
    }
}

@Composable
fun TypewriterText() {
    val messages = listOf(
        "Share ●",
        "Explore ●",
        "Connect ●",
        "Follow ●",
        "Like ●",
        "Comment ●",
        "Share a Story ●",
        "Discover Trends ●"
    )

    var currentMessage by remember { mutableStateOf("") }
    var messageIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            val message = messages[messageIndex]
            for (i in message.indices) {
                currentMessage = message.take(i + 1)
                delay(100)
            }
            delay(2000)
            currentMessage = ""
            messageIndex = (messageIndex + 1) % messages.size
        }
    }

    Text(
        text = currentMessage,
        color = Color.White,
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun LoginOptionsBox(navController: NavController, viewModel: AuthViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            )
            .padding(24.dp)
    ) {
        LoginOptions(navController, viewModel)
    }
}

@Composable
fun LoginOptions(navController: NavController, viewModel: AuthViewModel) {
    val context = LocalContext.current

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                viewModel.loginWithGoogle(account,
                    onSuccess = {
                        navController.navigate("home")
                    },
                    onFailure = {
                        // Hata durumunda yapılacaklar
                    }
                )
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignInError", "Google Sign-In failed: ${e.statusCode}")
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SocialLoginButton(
            text = "Continue with Google",
            backgroundColor = Color.White,
            textColor = Color.Black,
            onClick = {
                val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        )

        SocialLoginButton(
            text = "Sign up with Email",
            backgroundColor = Color(0xFF404040),
            textColor = Color.White,
            onClick = {
                navController.navigate("register")
            }
        )

        Divider(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 8.dp),
            thickness = 1.dp,
            color = Color.Gray
        )

        OutlinedButton(
            onClick = {
                navController.navigate("sign_in")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) {
            Text("Sign In", fontSize = 16.sp)
        }
    }
}

@Composable
fun SocialLoginButton(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
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
