package com.akansu.sosyashare.presentation.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.akansu.sosyashare.R
import androidx.navigation.NavController

@Composable
fun NavigationBar(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    navController: NavController,
    profilePictureUrl: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(85.dp)  // NavigationBar yüksekliği 85dp olarak bırakıldı
            .background(Color.Transparent),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                R.drawable.home,
                R.drawable.search,
                R.drawable.more,
                R.drawable.trend,
                null
            )

            items.forEachIndexed { index, icon ->
                val iconSize = when (icon) {
                    R.drawable.home -> 36.dp
                    R.drawable.search -> 45.dp  // search ikonu için boyut artırıldı
                    R.drawable.trend -> 34.dp
                    R.drawable.more -> 36.dp// trend ikonu için boyut azaltıldı
                    else -> 38.dp  // Diğer ikonlar için standart boyut
                }

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
                        modifier = Modifier
                            .size(50.dp)
                            .offset(y = (-4).dp)  // İkonları yukarı kaydırmak için y-ekseni offset eklendi
                    ) {
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            tint = if (selectedItem == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            navController.navigate("userprofile")
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .offset(y = (-4).dp)  // Profil fotoğrafı da yukarı kaydırıldı
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(profilePictureUrl ?: R.drawable.profile),
                            contentDescription = null,
                            modifier = Modifier
                                .size(35.dp)
                                .clip(CircleShape)
                                .align(Alignment.CenterVertically),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}
