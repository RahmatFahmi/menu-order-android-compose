package com.rahmat.testapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp

@Composable
fun QRScannerOverlay(instructionText: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Gunakan 'size' asli milik DrawScope, bukan import java.nio
            val width = size.width
            val height = size.height
            val boxSize = size.minDimension * 0.7f

            val left = (width - boxSize) / 2
            val top = (height - boxSize) / 2

            // 1. Gambar Latar Belakang Gelap dengan Lubang
            with(drawContext.canvas.nativeCanvas) {
                val checkPoint = saveLayer(null, null)

                // Lapisan hitam transparan
                drawRect(Color.Black.copy(alpha = 0.7f))

                // Membuat lubang di tengah
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(left, top),
                    size = Size(boxSize, boxSize),
                    cornerRadius = CornerRadius(24.dp.toPx()),
                    blendMode = BlendMode.Clear
                )
                restoreToCount(checkPoint)
            }

            // 2. Gambar Border Putih (Gunakan Stroke dari drawscope)
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(left, top),
                size = Size(boxSize, boxSize),
                cornerRadius = CornerRadius(24.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // 3. Teks Instruksi
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(bottom = 100.dp)
            ) {
                Text(
                    text = instructionText,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}