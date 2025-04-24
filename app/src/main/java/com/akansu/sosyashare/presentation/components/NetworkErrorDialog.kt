package com.akansu.sosyashare.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

/**
 * İnternet bağlantısı hatası durumunda gösterilecek dialog
 * @param onDismiss Dialog kapatıldığında çağrılacak fonksiyon
 */
@Composable
fun NetworkErrorDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "İnternet Bağlantısı Yok",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Lütfen internet bağlantınızı kontrol edip tekrar deneyin."
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Tamam")
            }
        }
    )
} 