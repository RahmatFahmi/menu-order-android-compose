package com.rahmat.testapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomAuthHeader(
    modifier: Modifier = Modifier,
    resId: Int,
    contentDescription: String?,
    title: String,
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
    tint: Color = titleColor,
    size: Dp = 100.dp,
    contentScale: ContentScale = ContentScale.Fit
){
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(),
    ){
        Image(
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            modifier = Modifier
                .size(size),
            contentScale = contentScale,
            colorFilter = ColorFilter.tint(tint)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            color = titleColor,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }

}