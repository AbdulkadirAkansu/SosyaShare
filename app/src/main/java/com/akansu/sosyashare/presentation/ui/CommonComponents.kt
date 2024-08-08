package com.akansu.sosyashare.presentation.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ErrorMessage(errorMessage: String?) {
    errorMessage?.let {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = it,
            color = Color.Red,
            modifier = Modifier
        )
    }
}

@Composable
fun SuccessMessage(successMessage: String?) {
    successMessage?.let {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = it,
            color = Color.Green,
            modifier = Modifier
        )
    }
}
