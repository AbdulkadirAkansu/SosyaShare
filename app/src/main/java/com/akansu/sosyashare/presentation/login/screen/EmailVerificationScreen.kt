package com.akansu.sosyashare.presentation.login.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.akansu.sosyashare.presentation.login.viewmodel.AuthViewModel
import com.akansu.sosyashare.presentation.ui.ErrorMessage
import com.akansu.sosyashare.presentation.ui.SuccessMessage
import com.akansu.sosyashare.util.poppinsFontFamily
import kotlinx.coroutines.delay

@Composable
fun EmailVerificationScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isVerified by remember { mutableStateOf(false) }


    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        while (!isVerified) {
            viewModel.reloadUser(
                onSuccess = {
                    val user = viewModel.getCurrentUser()
                    if (user?.isEmailVerified == true) {
                        isVerified = true
                        navController.navigate("home")
                    }
                },
                onFailure = { exception ->
                    errorMessage = exception.message
                }
            )
            delay(5000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF3949AB),
                        Color(0xFF5C6BC0),
                        Color(0xFF7986CB),
                        Color(0xFF9FA8DA)
                    )
                )
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (!isVerified) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Verify Email",
                        tint = Color.White,
                        modifier = Modifier
                            .size(88.dp)
                            .align(Alignment.Center)
                            .scale(scale)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Verify Your Email",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = poppinsFontFamily
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "We've sent a verification link to your email address. Please click on the link to verify your account.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = poppinsFontFamily
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        viewModel.sendEmailVerification(
                            onSuccess = {
                                successMessage = "Verification email sent. Please check your inbox."
                            },
                            onFailure = { exception ->
                                errorMessage = exception.message
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text(
                        "Resend Verification Email",
                        color = Color(0xFF3949AB),
                        fontSize = 16.sp,
                        fontFamily = poppinsFontFamily,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.reloadUser(
                            onSuccess = {
                                val user = viewModel.getCurrentUser()
                                if (user?.isEmailVerified == true) {
                                    navController.navigate("home")
                                } else {
                                    errorMessage = "Email not verified. Please check your inbox."
                                }
                            },
                            onFailure = { exception ->
                                errorMessage = exception.message
                            }
                        )
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(2.dp, Color.White),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text(
                        "I've Verified My Email",
                        fontSize = 16.sp,
                        fontFamily = poppinsFontFamily,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(visible = errorMessage != null || successMessage != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ErrorMessage(errorMessage)
                        SuccessMessage(successMessage)
                    }
                }
            }
        }
    }
}
