package com.akansu.sosyashare.presentation.settings

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.akansu.sosyashare.R
import com.akansu.sosyashare.presentation.home.components.NavigationBar
import com.akansu.sosyashare.presentation.login.viewmodel.AuthViewModel
import com.akansu.sosyashare.presentation.userprofile.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedItem by remember { mutableIntStateOf(4) }
    val profilePictureUrl by userViewModel.profilePictureUrl.collectAsState()
    val isPrivate by settingsViewModel.isPrivate.collectAsState()

    LaunchedEffect(Unit) {
        Log.d("SettingsScreen", "Initializing Settings Screen")
        settingsViewModel.initialize()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontSize = 30.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
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
        content = { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth().clickable { navController.navigate("saved_posts") }.padding(16.dp)) {
                    Icon(painterResource(id = R.drawable.save), contentDescription = "Saved Posts")
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Saved Posts")
                }

                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Private Account", modifier = Modifier.weight(1f))
                    isPrivate?.let { privateStatus ->
                        var isUpdating by remember { mutableStateOf(false) }
                        Switch(checked = privateStatus, onCheckedChange = { newValue ->
                            isUpdating = true
                            Log.d("SettingsScreen", "Updating Private Account Setting to $newValue")
                            settingsViewModel.updateUserPrivacySetting(newValue)
                            isUpdating = false
                        })
                        if (isUpdating) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } ?: CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }

                Row(modifier = Modifier.fillMaxWidth().clickable {
                    coroutineScope.launch {
                        authViewModel.logoutUser(
                            onSuccess = { navController.navigate("login") { popUpTo("home") { inclusive = true } } },
                            onFailure = { e -> Log.e("SettingsScreen", "Logout failed: ${e.message}", e) }
                        )
                    }
                }.padding(16.dp)) {
                    Icon(Icons.Default.Settings, contentDescription = "Logout")
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Logout")
                }
            }
        }
    )
}

