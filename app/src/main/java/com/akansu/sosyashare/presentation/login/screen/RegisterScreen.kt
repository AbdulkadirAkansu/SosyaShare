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
import androidx.compose.ui.text.font.FontWeight
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
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) } // Şifreyi göstermek için
    var errorMessage by remember { mutableStateOf<String?>(null) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val screenHeight = maxHeight

        Image(
            painter = painterResource(id = R.drawable.register1),
            contentDescription = "Register Background",
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
            Spacer(modifier = Modifier.height(screenHeight * 0.08f))

            // Night logo
            Image(
                painter = painterResource(id = R.drawable.nightlogo),
                contentDescription = "Night Logo",
                modifier = Modifier.size(screenHeight * 0.17f)
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.03f))

            Text(
                text = "Create Account",
                fontSize = (screenHeight * 0.04f).value.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = poppinsFontFamily
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.01f))

            Text(
                text = "Sign up to get started!",
                fontSize = (screenHeight * 0.02f).value.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontFamily = poppinsFontFamily
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.08f))

            // Username field
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                TextField(
                    value = username,
                    onValueChange = {
                        if (it.length <= 30 && it == it.lowercase()) {
                            username = it
                        }
                    },
                    label = { Text("Username", fontFamily = poppinsFontFamily) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color(0xFF0D47A1),
                        unfocusedIndicatorColor = Color(0xFF0D47A1),
                        cursorColor = Color(0xFF0D47A1)
                    ),
                    textStyle = LocalTextStyle.current.copy(fontFamily = poppinsFontFamily, fontSize = 14.sp)
                )
            }

            Spacer(modifier = Modifier.height(screenHeight * 0.02f))

            // Email field
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
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color(0xFF0D47A1),
                        unfocusedIndicatorColor = Color(0xFF0D47A1),
                        cursorColor = Color(0xFF0D47A1)
                    ),
                    textStyle = LocalTextStyle.current.copy(fontFamily = poppinsFontFamily, fontSize = 14.sp)
                )
            }

            Spacer(modifier = Modifier.height(screenHeight * 0.02f))

            // Password field with visibility toggle
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
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
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
                    textStyle = LocalTextStyle.current.copy(fontFamily = poppinsFontFamily, fontSize = 14.sp)
                )
            }

            Spacer(modifier = Modifier.height(screenHeight * 0.05f))

            Button(
                onClick = {
                    when {
                        username.isEmpty() -> {
                            errorMessage = "Username cannot be empty."
                        }
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                            errorMessage = "Invalid email format."
                        }
                        password.length < 6 -> {
                            errorMessage = "Password must be at least 6 characters long."
                        }
                        else -> {
                            viewModel.isUsernameUnique(username) { isUnique ->
                                if (!isUnique) {
                                    errorMessage = "This username is already taken. Please choose a different one."
                                } else {
                                    viewModel.registerUser(email, password, username, onSuccess = {
                                        navController.navigate("email_verification")
                                    }, onFailure = { exception ->
                                        errorMessage = exception.message
                                    })
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp) // Button genişliğini TextField ile eşleştirildi.
                    .height(50.dp), // Buton yüksekliği sabitlenmiş.
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Sign Up", fontSize = 18.sp, fontFamily = poppinsFontFamily)
            }

            Spacer(modifier = Modifier.height(screenHeight * 0.02f))

            // Error message with Snackbar
            if (errorMessage != null) {
                Snackbar(
                    action = {
                        TextButton(onClick = { errorMessage = null }) {
                            Text("OK", color = Color.White)
                        }
                    },
                    modifier = Modifier.padding(16.dp),
                    containerColor = Color(0xFF0D47A1)
                ) {
                    Text(text = errorMessage ?: "", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Back to Login text (Bold)
            Text(
                text = "Already have an account? Log In",
                modifier = Modifier
                    .clickable {
                        navController.navigate("login")
                    }
                    .padding(16.dp),
                color = Color.White,
                fontSize = (screenHeight * 0.015f).value.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = poppinsFontFamily
            )
        }
    }
}