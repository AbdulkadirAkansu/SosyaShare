package com.akansu.sosyashare.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.akansu.sosyashare.presentation.home.components.NavigationBar
import com.akansu.sosyashare.presentation.login.viewmodel.AuthViewModel
import com.akansu.sosyashare.presentation.userprofile.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, authViewModel: AuthViewModel, userViewModel: UserViewModel = hiltViewModel()) {
    val coroutineScope = rememberCoroutineScope()
    var selectedItem by remember { mutableIntStateOf(4) }
    val profilePictureUrl by userViewModel.profilePictureUrl.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                modifier = Modifier.height(70.dp)
            )
        },
        bottomBar = {
            NavigationBar(
                selectedItem = selectedItem,
                onItemSelected = { selectedItem = it },
                navController = navController,
                profilePictureUrl = profilePictureUrl
            )
        },
        modifier = Modifier.padding(WindowInsets.systemBars.asPaddingValues())
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
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
                    }
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Logout",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Logout", style = MaterialTheme.typography.bodyLarge)
            }
            // Diğer ayarlar için Row eklemeleri yapılabilir.
        }
    }
}
