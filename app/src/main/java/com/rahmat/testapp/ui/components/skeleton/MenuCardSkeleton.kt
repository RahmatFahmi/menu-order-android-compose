package com.rahmat.testapp.ui.components.skeleton

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rahmat.testapp.ui.theme.AppColors

@Composable
fun MenuCardSkeleton(
    shimmerAlpha: Float = rememberShimmerAlpha()
) {
    val color = Color.LightGray.copy(alpha = shimmerAlpha)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.MenuCardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kotak Gambar (Lingkaran)
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(color)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 45.dp) // Beri space agar tidak tabrakan dengan tombol cart
            ) {
                // Judul
                Box(modifier = Modifier.fillMaxWidth(0.7f).height(18.dp).clip(RoundedCornerShape(4.dp)).background(color))
                Spacer(modifier = Modifier.height(8.dp))

                // Deskripsi
                Box(modifier = Modifier.fillMaxWidth(0.9f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(color))
                Spacer(modifier = Modifier.height(12.dp))

                // Row Rating & Waktu
                Row {
                    Box(modifier = Modifier.size(40.dp, 12.dp).clip(RoundedCornerShape(4.dp)).background(color))
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(modifier = Modifier.size(50.dp, 12.dp).clip(RoundedCornerShape(4.dp)).background(color))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Harga
                Box(modifier = Modifier.size(80.dp, 16.dp).clip(RoundedCornerShape(4.dp)).background(color))
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd) // Posisikan di pojok kanan bawah
                .size(width = 52.dp, height = 32.dp) // Ukuran mirip dengan tombol asli
                .clip(RoundedCornerShape(topStart = 16.dp))
                .background(color) // Gunakan warna pulse yang sama
        )
    }
}

@Composable
fun rememberShimmerAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "shimmer"
    )
    return alpha
}

