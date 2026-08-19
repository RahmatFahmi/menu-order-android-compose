package com.rahmat.testapp.ui.components

import android.R.attr.label
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahmat.testapp.ui.theme.AppColors

@Composable
fun CategoryItem(
    label: String,
    iconRes: Int, // Menggunakan resource ID untuk gambar
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .width(63.dp)
            .clip(RoundedCornerShape(25.dp)) // Membuat background lonjong
            .background(AppColors.CategoryBackground)
            .clickable{ onClick() }
            .padding(vertical = 15.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Lingkaran Oranye untuk Ikon
        Box(
            modifier = Modifier
                .size(45.dp)
                .background(AppColors.AccentMenu, shape = CircleShape), // Warna oranye
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier
                    .padding(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Label Teks
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
    }
}