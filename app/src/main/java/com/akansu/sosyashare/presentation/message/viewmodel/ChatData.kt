package com.akansu.sosyashare.presentation.message.viewmodel


import androidx.compose.ui.graphics.Color

data class ChatColors(
    val backgroundColor: Color,
    val surfaceColor: Color,
    val textColor: Color,
    val bubbleColorOwn: Color,
    val bubbleColorOther: Color
)

fun getChatColors(isDarkTheme: Boolean): ChatColors {
    return ChatColors(
        backgroundColor = if (isDarkTheme) Color(0xFF121212) else Color(0xFFF5F5F5),
        surfaceColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color(0xFFFFFFFF),
        textColor = if (isDarkTheme) Color.White else Color.Black,
        bubbleColorOwn = if (isDarkTheme) Color(0xFFFFF6E2) else Color(0xFFDCF8C6),
        bubbleColorOther = if (isDarkTheme) Color(0xFF252525) else Color(0xFFE4E6EB)
    )
}

