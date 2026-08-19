package com.rahmat.testapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CustomImage(
    modifier: Modifier = Modifier,
    resId: Int,
    contentDescription: String,
    size: Dp = 100.dp,
    shape: Shape = RectangleShape,
    contentScale: ContentScale = ContentScale.Fit,
    tint: Color? = null
){
    Box(
        modifier = modifier
            .clip(shape),
        contentAlignment = Alignment.Center
    ){
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            contentScale = contentScale,
            colorFilter = if (tint != null) ColorFilter.tint(tint) else null
        )
    }
}