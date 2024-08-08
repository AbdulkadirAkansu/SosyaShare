package com.akansu.sosyashare.presentation.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.akansu.sosyashare.R
import com.akansu.sosyashare.util.poppinsFontFamily

@Composable
fun Post(
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
    val scale by animateFloatAsState(if (liked) 1.2f else 1f, tween(300))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        // Post header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(profilePictureUrl ?: R.drawable.profile),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    username,
                    fontWeight = FontWeight.Bold,
                    fontFamily = poppinsFontFamily,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }

        // Post image
        Image(
            painter = rememberAsyncImagePainter(postUrl),
            contentDescription = "Post Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clickable {
                    liked = !liked
                    if (liked) {
                        likes += 1
                        onLikeClick()
                    } else {
                        likes -= 1
                        onUnlikeClick()
                    }
                },
            contentScale = ContentScale.Crop
        )

        // Post actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = {
                liked = !liked
                if (liked) {
                    likes += 1
                    onLikeClick()
                } else {
                    likes -= 1
                    onUnlikeClick()
                }
            }) {
                Icon(
                    if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Like",
                    modifier = Modifier.size(24.dp).scale(scale),
                    tint = if (liked) Color.Red else Color.Gray
                )
            }
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Filled.Check, contentDescription = "Comment", modifier = Modifier.size(24.dp))
            }
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Filled.Share, contentDescription = "Share", modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Filled.Star, contentDescription = "Save", modifier = Modifier.size(24.dp))
            }
        }

        // Likes count
        Text(
            "$likes beğenme",
            fontWeight = FontWeight.Bold,
            fontFamily = poppinsFontFamily,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // User comment
        comment.let {
            Text(
                it,
                fontFamily = poppinsFontFamily,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )
        }
    }
}
