package com.akansu.sosyashare.presentation.home.components

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.akansu.sosyashare.R

@Composable
fun NavigationBar(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    navController: NavController,
    profilePictureUrl: String?,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        R.drawable.home,
        R.drawable.search,
        R.drawable.more,
        R.drawable.trending,
        null
    )

    val context = LocalContext.current
    val activity = context as? Activity
    val window = activity?.window
    val darkTheme = isSystemInDarkTheme()

    DisposableEffect(Unit) {
        window?.let {
            it.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(it, it.decorView).isAppearanceLightNavigationBars = !darkTheme
        }
        onDispose { /* Clean up if necessary */ }
    }

    Surface(
        color = Color.Transparent, // Transparent color for the Surface
        modifier = modifier
            .fillMaxWidth()
            .height(66.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 1.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, icon ->
                if (icon != null) {
                    IconButton(
                        onClick = {
                            onItemSelected(index)
                            when (index) {
                                0 -> navController.navigate("home")
                                1 -> navController.navigate("search")
                                2 -> navController.navigate("share")
                                3 -> navController.navigate("trend")
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            tint = if (selectedItem == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            onItemSelected(index)
                            navController.navigate("userprofile")
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        if (profilePictureUrl != null) {
                            Image(
                                painter = rememberAsyncImagePainter(profilePictureUrl),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.profile),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}
