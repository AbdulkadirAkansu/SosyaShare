package com.akansu.sosyashare.presentation.savedposts


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.akansu.sosyashare.domain.model.Post
import com.akansu.sosyashare.presentation.home.viewmodel.HomeViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SavedPostsScreen(navController: NavController, homeViewModel: HomeViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) {
        homeViewModel.loadSavedPosts()
    }

    val savedPosts by homeViewModel.savedPosts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Posts") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { paddingValues ->
            if (savedPosts.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Text("No saved posts to display.")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3), // 3 sütunlu grid
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(1.dp), // Kenar boşluklarını minimuma indiriyoruz
                    horizontalArrangement = Arrangement.spacedBy(1.dp), // Yatay boşlukları minimumda tut
                    verticalArrangement = Arrangement.spacedBy(1.dp) // Dikey boşlukları minimumda tut
                ) {
                    items(savedPosts.size) { index ->
                        val post = savedPosts[index]
                        Image(
                            painter = rememberAsyncImagePainter(post.imageUrl),
                            contentDescription = "Saved Post Image",
                            contentScale = ContentScale.Crop, // Resmi kesmeden sığdır
                            modifier = Modifier
                                .aspectRatio(1f) // Kare görünümü sağlamak için
                                .clickable {
                                    navController.navigate("post_detail/${post.userId}/$index/true")

                                }
                        )
                    }
                }
            }
        }
    )
}