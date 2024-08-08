package com.akansu.sosyashare.presentation.login.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.akansu.sosyashare.R
import com.akansu.sosyashare.presentation.login.viewmodel.AuthViewModel
import com.akansu.sosyashare.presentation.ui.ErrorMessage
import com.akansu.sosyashare.util.poppinsFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val screenHeight = maxHeight

        Image(
            painter = painterResource(id = R.drawable.pic5),
            contentDescription = "Login Background",
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

            Image(
                painter = painterResource(id = R.drawable.nightlogo),
                contentDescription = "Login Logo",
                modifier = Modifier.size(screenHeight * 0.2f)
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.1f))

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
                    label = { Text("Username, email", fontFamily = poppinsFontFamily) },
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color(0xFF0D47A1),
                        unfocusedIndicatorColor = Color(0xFF0D47A1),
                        cursorColor = Color(0xFF0D47A1)
                    ),
                    textStyle = LocalTextStyle.current.copy(fontFamily = poppinsFontFamily, fontSize = 14.sp)
                )
            }

            Spacer(modifier = Modifier.height(screenHeight * 0.02f))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", fontFamily = poppinsFontFamily) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color(0xFF0D47A1),
                        unfocusedIndicatorColor = Color(0xFF0D47A1),
                        cursorColor = Color(0xFF0D47A1)
                    ),
                    textStyle = LocalTextStyle.current.copy(fontFamily = poppinsFontFamily, fontSize = 14.sp)
                )
            }

            Spacer(modifier = Modifier.height(screenHeight * 0.02f))

            Button(
                onClick = {
                    viewModel.loginUser(email, password, onSuccess = {
                        navController.navigate("home")
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
                Text("Log in", fontSize = (screenHeight * 0.02f).value.sp, color = Color.White, fontFamily = poppinsFontFamily)
            }

            Spacer(modifier = Modifier.height(screenHeight * 0.02f))

            Text(
                text = "Forgot password?",
                color = Color.White,
                fontSize = (screenHeight * 0.02f).value.sp,
                modifier = Modifier
                    .clickable {
                        navController.navigate("forgot_password")
                    },
                fontFamily = poppinsFontFamily
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.02f))

            ErrorMessage(errorMessage)

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = {
                    navController.navigate("register")
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
                Text("Create new account", fontSize = (screenHeight * 0.02f).value.sp, color = Color(0xFF0D47A1), fontFamily = poppinsFontFamily)
            }

            Spacer(modifier = Modifier.height(screenHeight * 0.02f))
        }
    }
}
