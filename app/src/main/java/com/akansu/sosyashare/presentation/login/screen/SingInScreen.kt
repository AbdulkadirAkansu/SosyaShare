package com.akansu.sosyashare.presentation.login.screen


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.akansu.sosyashare.R
import com.akansu.sosyashare.presentation.login.viewmodel.AuthViewModel
import com.akansu.sosyashare.util.poppinsFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingInScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val screenHeight = maxHeight

        // Arkaplan resmi
        Image(
            painter = painterResource(id = R.drawable.giris2),
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

            // Logo
            Image(
                painter = painterResource(id = R.drawable.nightlogo),
                contentDescription = "Login Logo",
                modifier = Modifier.size(screenHeight * 0.2f)
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.2f))

            // E-posta alanı
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
                    label = { Text("Email Address", fontFamily = poppinsFontFamily) },
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color(0xFF0D47A1),
                        unfocusedIndicatorColor = Color(0xFF0D47A1),
                        cursorColor = Color(0xFF0D47A1)
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = poppinsFontFamily,
                        fontSize = 14.sp
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                )
            }

            Spacer(modifier = Modifier.height(screenHeight * 0.02f))

            // Şifre alanı
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
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            painterResource(id = R.drawable.eye_password_hide)
                        else painterResource(id = R.drawable.eye_password)

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(painter = image, contentDescription = null)
                        }
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color(0xFF0D47A1),
                        unfocusedIndicatorColor = Color(0xFF0D47A1),
                        cursorColor = Color(0xFF0D47A1)
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = poppinsFontFamily,
                        fontSize = 14.sp
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
                )
            }

            Spacer(modifier = Modifier.height(screenHeight * 0.02f))

            // Giriş butonu
            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        errorMessage = "E-posta ve şifre alanları boş olamaz."
                    } else {
                        viewModel.loginUser(email, password, onSuccess = {
                            navController.navigate("home")
                        }, onFailure = { exception ->
                            errorMessage = when (exception.message) {
                                "INVALID_EMAIL" -> "Geçersiz e-posta adresi. Lütfen tekrar deneyin."
                                "WRONG_PASSWORD" -> "Yanlış şifre. Lütfen doğru şifreyi girin."
                                else -> "Giriş başarısız oldu. Lütfen tekrar deneyin."
                            }
                        })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Sign In",
                    fontSize = (screenHeight * 0.02f).value.sp,
                    color = Color.White,
                    fontFamily = poppinsFontFamily
                )
            }

            Spacer(modifier = Modifier.height(screenHeight * 0.02f))

            Text(
                text = "Forgot Password?",
                color = Color.White,
                fontSize = (screenHeight * 0.02f).value.sp,
                modifier = Modifier
                    .clickable {
                        navController.navigate("forgot_password")
                    },
                fontFamily = poppinsFontFamily
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.02f))

            if (errorMessage != null) {
                Snackbar(
                    action = {
                        TextButton(onClick = { errorMessage = null }) {
                            Text("Tamam", color = Color.White)
                        }
                    },
                    modifier = Modifier.padding(16.dp),
                    containerColor = Color(0xFF0D47A1)
                ) {
                    Text(text = errorMessage ?: "", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = {
                    navController.navigate("register")
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFFFFFFFF)
                ),
                border = BorderStroke(1.dp, Color(0xFFFFFFFF)),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    "Create a new account",
                    fontSize = (screenHeight * 0.018f).value.sp,
                    color = Color(0xFFFFFFFF),
                    fontFamily = poppinsFontFamily
                )
            }

            Spacer(modifier = Modifier.height(screenHeight * 0.02f))
        }
    }
}
