package com.rahmat.testapp.ui.components.skeleton

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rahmat.testapp.ui.theme.AppDimens


@Composable
fun MainCategoriesSkeleton(shimmerAlpha: Float) {
    val color = Color.LightGray.copy(alpha = shimmerAlpha)
    Column {
        // Label "Categories"
        Box(modifier = Modifier.fillMaxWidth(0.3f).height(16.dp).clip(RoundedCornerShape(4.dp)).background(color))
        Spacer(modifier = Modifier.height(AppDimens.paddingItem))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(4) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(color))
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.size(40.dp, 10.dp).clip(RoundedCornerShape(4.dp)).background(color))
                }
            }
        }
    }
}