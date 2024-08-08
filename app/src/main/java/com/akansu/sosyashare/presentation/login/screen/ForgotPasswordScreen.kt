package com.akansu.sosyashare.presentation.login.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.akansu.sosyashare.R
import com.akansu.sosyashare.presentation.login.viewmodel.AuthViewModel
import com.akansu.sosyashare.presentation.ui.ErrorMessage
import com.akansu.sosyashare.presentation.ui.SuccessMessage
import com.akansu.sosyashare.util.poppinsFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {
    var email by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val screenHeight = maxHeight

        Image(
            painter = painterResource(id = R.drawable.pic7),
            contentDescription = "Forgot Password Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(screenHeight * 0.05f))

            Text(
                text = "Forgot Password",
                fontSize = (screenHeight * 0.05f).value.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = poppinsFontFamily
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.01f))

            Text(
                text = "Enter your email to reset your password",
                fontSize = (screenHeight * 0.025f).value.sp,
                color = Color.White,
                fontFamily = poppinsFontFamily
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.07f))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", fontFamily = poppinsFontFamily) },
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color(0xFF0D47A1),
                        unfocusedIndicatorColor = Color(0xFF0D47A1),
                        cursorColor = Color(0xFF0D47A1)
                    ),
                    textStyle = LocalTextStyle.current.copy(fontFamily = poppinsFontFamily, fontSize = (screenHeight * 0.018f).value.sp)
                )
            }

            Spacer(modifier = Modifier.height(screenHeight * 0.03f))

            Button(
                onClick = {
                    viewModel.resetPassword(email, onSuccess = {
                        successMessage = "Password reset email sent. Please check your inbox."
                    }, onFailure = { exception ->
                        errorMessage = exception.message
                    })
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Reset Password", fontSize = (screenHeight * 0.025f).value.sp, color = Color.White, fontFamily = poppinsFontFamily)
            }

            Spacer(modifier = Modifier.height(screenHeight * 0.02f))

            SuccessMessage(successMessage)
            ErrorMessage(errorMessage)

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = {
                    navController.navigate("login")
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF0D47A1)
                ),
                border = BorderStroke(1.dp, Color(0xFF0D47A1)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Back to Login", fontSize = (screenHeight * 0.025f).value.sp, color = Color(0xFF0D47A1), fontFamily = poppinsFontFamily)
            }

            Spacer(modifier = Modifier.height(screenHeight * 0.03f))
        }
    }
}
