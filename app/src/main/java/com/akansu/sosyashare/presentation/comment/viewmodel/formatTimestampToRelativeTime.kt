package com.akansu.sosyashare.presentation.comment.viewmodel

import java.util.Date

fun formatTimestampToRelativeTime(timestamp: Date): String {
    val now = Date()
    val diffInMillis = now.time - timestamp.time

    val seconds = diffInMillis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7
    val months = days / 30
    val years = days / 365

    return when {
        years >= 1 -> "${years}y"
        months >= 1 -> "${months}m"
        weeks >= 1 -> "${weeks}w"
        days >= 1 -> "${days}d"
        hours >= 1 -> "${hours}h"
        minutes >= 1 -> "${minutes}m"
        else -> "${seconds}s"
    }
}
