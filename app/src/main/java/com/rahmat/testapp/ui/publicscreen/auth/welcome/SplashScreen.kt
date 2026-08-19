package com.rahmat.testapp.ui.publicscreen.auth.welcome

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahmat.testapp.R
import com.rahmat.testapp.ui.components.CustomImage
import com.rahmat.testapp.ui.components.CustomText
import com.rahmat.testapp.ui.theme.AppColors
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToNextScreen: () -> Unit
) {
    // Value animasi untuk Scale (Ukuran) & Alpha (Transparansi)
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // Jalankan animasi Logo secara bersamaan
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            )
        )
    }

    LaunchedEffect(key1 = true) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
        // Delay total sebelum pindah screen (misal 2 detik agar animasi terlihat bagus)
        delay(1800)
        onNavigateToNextScreen()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Content Tengah (Logo + Text Cafe)
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .scale(scale.value)
                .alpha(alpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CustomImage(
                modifier = Modifier.size(140.dp),
                resId = R.drawable.ic_universe,
                contentDescription = "Cafe Logo",
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(20.dp))
            CustomText(
                text = "C A F E",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        // Animated Loading Dots di Bagian Bawah
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoadingDotsIndicator()
        }
    }
}

/**
 * Komponen Animasi 3 Titik Berdenyut di Bawah (Sangat Ringan & Native Compose)
 */
@Composable
fun LoadingDotsIndicator(
    circleColor: Color = Color(0xFF35898F), // Sesuaikan dengan warna primer/brand kamu
    circleSize: Float = 10f
) {
    val transition = rememberInfiniteTransition(label = "dots_transition")

    val alpha1 by transition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "dot1"
    )
    val alpha2 by transition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 200), RepeatMode.Reverse), label = "dot2"
    )
    val alpha3 by transition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 400), RepeatMode.Reverse), label = "dot3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(circleSize.dp).alpha(alpha1).background(circleColor, CircleShape))
        Box(modifier = Modifier.size(circleSize.dp).alpha(alpha2).background(circleColor, CircleShape))
        Box(modifier = Modifier.size(circleSize.dp).alpha(alpha3).background(circleColor, CircleShape))
    }
}

@Preview
@Composable
fun SplashScreenPreview() {
    SplashScreen(onNavigateToNextScreen = {})
}