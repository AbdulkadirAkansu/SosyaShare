/*

package com.akansu.sosyashare.presentation.home

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.akansu.sosyashare.presentation.login.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController, authViewModel: AuthViewModel) {
    val coroutineScope = rememberCoroutineScope()

    TopAppBar(
        title = { Text("SosyaShare", color = MaterialTheme.colorScheme.onBackground) },
        actions = {
            IconButton(onClick = {
                coroutineScope.launch {
                    try {
                        authViewModel.logoutUser(
                            onSuccess = {
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onFailure = { e ->
                                e.printStackTrace()
                            }
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = MaterialTheme.colorScheme.onBackground)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        modifier = Modifier.height(70.dp)
    )
}
*/