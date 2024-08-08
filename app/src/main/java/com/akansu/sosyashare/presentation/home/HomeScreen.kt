package com.akansu.sosyashare.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.akansu.sosyashare.presentation.home.components.Post
import com.akansu.sosyashare.presentation.home.components.NavigationBar
import com.akansu.sosyashare.presentation.home.viewmodel.HomeViewModel
import com.akansu.sosyashare.presentation.login.viewmodel.AuthViewModel
import com.akansu.sosyashare.presentation.userprofile.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val profilePictureUrl by userViewModel.profilePictureUrl.collectAsState()
    val posts by homeViewModel.posts.collectAsState()
    val users by homeViewModel.users.collectAsState()

    LaunchedEffect(Unit) {
        homeViewModel.loadFollowedUsersPosts()
    }

    Scaffold(
        topBar = {
            TopBar(navController = navController, authViewModel = authViewModel)
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(posts) { post ->
                val user = users[post.userId]
                Post(
                    postUrl = post.imageUrl ?: "",
                    postId = post.id,
                    postUserId = post.userId,
                    username = user?.username ?: "Unknown",
                    profilePictureUrl = user?.profilePictureUrl,
                    comment = post.content,
                    isLiked = post.isLiked,
                    likeCount = post.likeCount,
                    onLikeClick = {
                        homeViewModel.likePost(post.id)
                    },
                    onUnlikeClick = {
                        homeViewModel.unlikePost(post.id)
                    }
                )
            }
        }
    }
}
