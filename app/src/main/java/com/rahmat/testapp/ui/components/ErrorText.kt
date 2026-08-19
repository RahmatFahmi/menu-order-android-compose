package com.rahmat.testapp.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import android.util.Log

@Composable
fun ErrorText(message: String?) {
    val errorMessage = message ?: "Terjadi kesalahan tidak diketahui"

    // Langsung munculkan di Logcat setiap kali komponen ini muncul
    Log.d("Gagal Login", "Error: $errorMessage")

    Text(
        text = errorMessage,
        color = Color.Red,
        fontSize = 12.sp
    )
}