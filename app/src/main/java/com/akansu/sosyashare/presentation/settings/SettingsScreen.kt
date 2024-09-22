package com.akansu.sosyashare.presentation.settings

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.akansu.sosyashare.R
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.presentation.home.components.NavigationBar
import com.akansu.sosyashare.presentation.login.viewmodel.AuthViewModel
import com.akansu.sosyashare.presentation.userprofile.viewmodel.UserViewModel
import com.akansu.sosyashare.util.poppinsFontFamily
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
                title = {
                    Text(
                        "Settings",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = poppinsFontFamily
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
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
                    .fillMaxWidth()
            ) {
                SettingsOption(
                    icon = R.drawable.empty_save,
                    label = "Saved Posts",
                    onClick = { navController.navigate("saved_posts") }
                )

                SettingsOption(
                    icon = R.drawable.private_account,
                    label = "Private Account",
                    trailing = {
                        isPrivate?.let { privateStatus ->
                            var isUpdating by remember { mutableStateOf(false) }
                            Switch(
                                checked = privateStatus,
                                onCheckedChange = { newValue ->
                                    isUpdating = true
                                    settingsViewModel.updateUserPrivacySetting(newValue)
                                    isUpdating = false
                                }
                            )
                        } ?: CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                )

                SettingsOption(
                    icon = R.drawable.block_user,
                    label = "Blocked Users",
                    onClick = { showBlockedUsersDialog = true }
                )

                SettingsOption(
                    icon = R.drawable.logout,
                    label = "Logout",
                    onClick = {
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
                )

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
fun SettingsOption(
    icon: Int,
    label: String,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val iconTintColor = MaterialTheme.colorScheme.onBackground

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick?.invoke() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .padding(end = 16.dp),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(iconTintColor)
        )
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            fontFamily = poppinsFontFamily,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.weight(1f))
        trailing?.invoke()
    }
}

@Composable
fun BlockedUsersDialog(
    blockedUsers: List<User>,
    onUnblockUser: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Blocked Users",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = poppinsFontFamily,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                blockedUsers.forEach { user ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(user.profilePictureUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = user.username,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            fontFamily = poppinsFontFamily,
                            color = MaterialTheme.colorScheme.onBackground
                        )
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
                Text(
                    "Close",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = poppinsFontFamily,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}