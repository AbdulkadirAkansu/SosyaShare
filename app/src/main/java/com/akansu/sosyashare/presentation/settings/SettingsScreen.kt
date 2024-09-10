package com.akansu.sosyashare.presentation.settings

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.akansu.sosyashare.R
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.presentation.home.components.NavigationBar
import com.akansu.sosyashare.presentation.login.viewmodel.AuthViewModel
import com.akansu.sosyashare.presentation.userprofile.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedItem by remember { mutableIntStateOf(4) }
    val profilePictureUrl by userViewModel.profilePictureUrl.collectAsState()
    val isPrivate by settingsViewModel.isPrivate.collectAsState()
    val blockedUsers by settingsViewModel.blockedUsers.collectAsState()

    var showBlockedUsersDialog by remember { mutableStateOf(false) }

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
                navController = navController,
                profilePictureUrl = profilePictureUrl
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("saved_posts") }
                        .padding(16.dp)
                ) {
                    Icon(painterResource(id = R.drawable.save), contentDescription = "Saved Posts")
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Saved Posts")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showBlockedUsersDialog = true }
                        .padding(16.dp)
                ) {
                    Icon(painterResource(id = R.drawable.notification), contentDescription = "Blocked Users")
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Blocked Users")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            coroutineScope.launch {
                                authViewModel.logoutUser(
                                    onSuccess = {
                                        navController.navigate("login") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    },
                                    onFailure = { e ->
                                        Log.e("SettingsScreen", "Logout failed: ${e.message}", e)
                                    }
                                )
                            }
                        }
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Logout")
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Logout")
                }

                if (showBlockedUsersDialog) {
                    BlockedUsersDialog(
                        blockedUsers = blockedUsers,
                        onUnblockUser = { userId ->
                            coroutineScope.launch {
                                settingsViewModel.unblockUser(userId)
                            }
                        },
                        onDismiss = { showBlockedUsersDialog = false }
                    )
                }
            }
        }
    )
}

@Composable
fun BlockedUsersDialog(
    blockedUsers: List<User>,
    onUnblockUser: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Blocked Users") },
        text = {
            Column {
                blockedUsers.forEach { user ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(user.profilePictureUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = user.username, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = { onUnblockUser(user.id) }) {
                            Text("Unblock")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
