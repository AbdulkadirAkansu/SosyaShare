/*

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
import androidx.compose.ui.res.painterResource
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
    // State management for the like status and like count
    var liked by remember { mutableStateOf(isLiked) }
    var likes by remember { mutableIntStateOf(likeCount) }

    // Ensure UI updates when the external state changes
    LaunchedEffect(isLiked, likeCount) {
        liked = isLiked
        likes = likeCount
    }

    // UI representation
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(postUrl),
            contentDescription = "Post Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clickable {
                    if (liked) {
                        likes -= 1
                        onUnlikeClick()
                    } else {
                        likes += 1
                        onLikeClick()
                    }
                    liked = !liked // Toggle the like status
                },
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = {
                if (liked) {
                    likes -= 1
                    onUnlikeClick()
                } else {
                    likes += 1
                    onLikeClick()
                }
                liked = !liked // Toggle the like status
            }) {
                Image(
                    painter = if (liked) {
                        painterResource(id = R.drawable.red_heart_icon) // Dolu kırmızı kalp ikonu
                    } else {
                        painterResource(id = R.drawable.heart_icon) // Boş kalp ikonu
                    },
                    contentDescription = "Like Icon",
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                "$likes beğenme",
                fontWeight = FontWeight.Bold,
                fontFamily = poppinsFontFamily,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}
*/