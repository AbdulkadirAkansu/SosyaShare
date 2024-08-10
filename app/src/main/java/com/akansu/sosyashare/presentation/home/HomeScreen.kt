package com.akansu.sosyashare.presentation.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.akansu.sosyashare.R
import com.akansu.sosyashare.presentation.home.components.NavigationBar
import com.akansu.sosyashare.presentation.home.viewmodel.HomeViewModel
import com.akansu.sosyashare.presentation.userprofile.viewmodel.UserViewModel
import com.akansu.sosyashare.util.poppinsFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val posts by homeViewModel.posts.collectAsState()
    val users by homeViewModel.users.collectAsState()
    var selectedItem by remember { mutableIntStateOf(0) }
    val profilePictureUrl by userViewModel.profilePictureUrl.collectAsState()

    LaunchedEffect(Unit) {
        homeViewModel.loadFollowedUsersPosts()
    }

    Scaffold(
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
            items(posts, key = { it.id }) { post ->
                val user = users[post.userId]
                PostItem(
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

@Composable
fun PostItem(
    postUrl: String,
    postId: String,
    postUserId: String,
    username: String,
    profilePictureUrl: String?,
    comment: String,
    isLiked: Boolean,
    likeCount: Int,
    onLikeClick: () -> Unit,
    onUnlikeClick: () -> Unit
) {
    var liked by remember { mutableStateOf(isLiked) }
    var likes by remember { mutableStateOf(likeCount) }

    // Başlangıçta durumu logluyoruz
    Log.d("PostItem", "Initializing PostItem: postId=$postId, isLiked=$isLiked, likeCount=$likeCount")

    LaunchedEffect(isLiked, likeCount) {
        Log.d("PostItem", "Updating State from Props: postId=$postId, isLiked=$isLiked, likeCount=$likeCount")
        liked = isLiked
        likes = likeCount
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(profilePictureUrl ?: R.drawable.profile),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = username, fontFamily = poppinsFontFamily, modifier = Modifier.weight(1f))
        }

        Image(
            painter = rememberAsyncImagePainter(postUrl),
            contentDescription = "Post Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clickable {
                    if (liked) {
                        Log.d("PostItem", "Unlike Button Clicked: postId=$postId, currentLikes=$likes")
                        likes -= 1
                        onUnlikeClick()
                    } else {
                        Log.d("PostItem", "Like Button Clicked: postId=$postId, currentLikes=$likes")
                        likes += 1
                        onLikeClick()
                    }
                    liked = !liked
                    Log.d("PostItem", "Post Click Updated State: postId=$postId, liked=$liked, likes=$likes")
                },
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (liked) {
                    Log.d("PostItem", "Unlike Icon Clicked: postId=$postId, currentLikes=$likes")
                    likes -= 1
                    onUnlikeClick()
                } else {
                    Log.d("PostItem", "Like Icon Clicked: postId=$postId, currentLikes=$likes")
                    likes += 1
                    onLikeClick()
                }
                liked = !liked
                Log.d("PostItem", "Icon Click Updated State: postId=$postId, liked=$liked, likes=$likes")
            }) {
                Image(
                    painter = if (liked) {
                        painterResource(id = R.drawable.red_heart_icon)
                    } else {
                        painterResource(id = R.drawable.heart_icon)
                    },
                    contentDescription = "Like Icon",
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "$likes beğenme",
                fontWeight = FontWeight.Bold,
                fontFamily = poppinsFontFamily,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}
