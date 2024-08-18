package com.akansu.sosyashare.presentation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.akansu.sosyashare.domain.repository.UserRepository
import com.akansu.sosyashare.presentation.comment.screen.CommentScreen
import com.akansu.sosyashare.presentation.editprofile.EditProfileScreen
import com.akansu.sosyashare.presentation.login.screen.*
import com.akansu.sosyashare.presentation.login.viewmodel.AuthViewModel
import com.akansu.sosyashare.presentation.profile.screen.ProfileScreen
import com.akansu.sosyashare.presentation.home.HomeScreen
import com.akansu.sosyashare.presentation.postdetail.screen.PostDetailScreen
import com.akansu.sosyashare.presentation.search.screen.SearchScreen
import com.akansu.sosyashare.presentation.userprofile.UserProfileScreen
import com.akansu.sosyashare.presentation.share.PostCreationScreen
import com.akansu.sosyashare.presentation.share.ShareScreen
import com.akansu.sosyashare.presentation.trend.TrendScreen
import com.akansu.sosyashare.presentation.settings.SettingsScreen
import com.akansu.sosyashare.presentation.splash.SplashScreen
import kotlinx.coroutines.launch

@Composable
fun NavGraph(navController: NavHostController, authViewModel: AuthViewModel, userRepository: UserRepository) {
    val startDestination = if (authViewModel.isUserLoggedIn()) "home" else "splash"
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("trend") {
            TrendScreen(navController = navController)
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }
        composable("forgot_password") {
            ForgotPasswordScreen(navController = navController)
        }
        composable("email_verification") {
            EmailVerificationScreen(navController = navController)
        }
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("search") {
            SearchScreen(navController = navController)
        }
        composable("splash") {
            SplashScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(
            route = "profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            ProfileScreen(navController = navController, userId = userId)
        }
        composable(
            route = "userprofile",
        ) {
            UserProfileScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("share") {
            ShareScreen(navController = navController)
        }
        composable(
            route = "post_creation?imageUri={imageUri}",
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val imageUri = Uri.parse(backStackEntry.arguments?.getString("imageUri"))
            PostCreationScreen(navController = navController, imageUri = imageUri)
        }
        composable("settings") {
            SettingsScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(
            route = "post_detail/{userId}/{initialPostIndex}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("initialPostIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            val initialPostIndex = backStackEntry.arguments?.getInt("initialPostIndex") ?: 0
            PostDetailScreen(navController, userId, initialPostIndex)
        }
        composable("editprofile") {
            EditProfileScreen(navController = navController)
        }

        composable(
            route = "comments/{postId}/{currentUserId}",
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType },
                navArgument("currentUserId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
            val currentUserId = backStackEntry.arguments?.getString("currentUserId") ?: return@composable
            val currentUserProfileUrl = authViewModel.getCurrentUserProfilePictureUrl() ?: ""

            // Coroutine Scope to fetch current user name
            var currentUserName by remember { mutableStateOf("") }
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    currentUserName = userRepository.getCurrentUserName() ?: ""
                    if (currentUserName.isNotEmpty()) {
                        navController.navigate("comments_screen/$postId/$currentUserId/$currentUserName/$currentUserProfileUrl")
                    }
                }
            }
        }

        composable(
            route = "comments/{postId}/{currentUserId}",
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType },
                navArgument("currentUserId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
            val currentUserId = backStackEntry.arguments?.getString("currentUserId") ?: return@composable
            val currentUserProfileUrl = authViewModel.getCurrentUserProfilePictureUrl() ?: ""

            // Coroutine Scope and LaunchedEffect to get current user name
            var currentUserName by remember { mutableStateOf<String?>(null) }
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    currentUserName = userRepository.getCurrentUserName()
                }
            }

            if (currentUserName != null) {
                CommentScreen(
                    postId = postId,
                    currentUserId = currentUserId,
                    currentUserName = currentUserName ?: "",
                    currentUserProfileUrl = currentUserProfileUrl,
                    backgroundContent = {
                        PostDetailScreen(navController = navController, userId = currentUserId, initialPostIndex = 0)
                    }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
