package com.rahmat.testapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rahmat.testapp.R
import com.rahmat.testapp.common.utils.Constants
import com.rahmat.testapp.domain.model.Menu
import com.rahmat.testapp.ui.theme.AppColors

@Composable
fun MenuRecommendation(
    menu: Menu,
    modifier: Modifier = Modifier,
    onItemClick: () -> Unit,
    onCartClick: () -> Unit,
) {
    Surface(
        onClick = onItemClick,
        shape = RoundedCornerShape(20.dp),
        color = AppColors.MenuCardBackground, // Pastikan warna background surface keputihan
        modifier = modifier
            .width(160.dp) // Sesuaikan lebar untuk tampilan grid
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- BAGIAN ATAS: GAMBAR & BADGE ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                AsyncImage(
                    model = Constants.BASE_IMAGE_URL + "storage/" + menu.imageUrl,
                    contentDescription = menu.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(15.dp)), // Bentuk kotak tumpul sesuai gambar
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_placeholde_food),
                    error = painterResource(id = R.drawable.ic_placeholde_food),
                )

                // Badge Diskon (Pojok Kiri Atas)
                if (!menu.discountLabel.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .size(32.dp)
                            .background(AppColors.AccentMenu, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = menu.discountLabel,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- BAGIAN TENGAH: INFO NAMA & RATING ---
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = menu.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Rating
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_star), // Gunakan icon bintang penuh
                            contentDescription = null,
                            tint = AppColors.AccentMenu,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = " ${menu.averageRating}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary.copy(alpha = 0.8f)
                        )
                    }

                    // Waktu (Timer)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_timer), // Gunakan icon timer/jam
                            contentDescription = null,
                            tint = AppColors.AccentMenu,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = " ${menu.preparationTime}",
                            fontSize = 11.sp,
                            color = AppColors.TextPrimary.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BAGIAN BAWAH: HARGA & TOMBOL PLUS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Rp. ${menu.finalPrice.toInt()}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = AppColors.TextPrimary
                    )

                    if (!menu.discountLabel.isNullOrEmpty()) {
                        Text(
                            text = "Rp. ${menu.price.toInt()}",
                            fontSize = 10.sp,
                            color = AppColors.TextPrimary.copy(alpha = 0.4f),
                            style = TextStyle(
                                textDecoration = TextDecoration.LineThrough
                            )
                        )
                    }
                }

                // Tombol Tambah (+) Lingkaran
                Surface(
                    onClick = { if (menu.isAvailable) onCartClick() },
                    shape = CircleShape,
                    color = if (menu.isAvailable) AppColors.AccentMenu else Color.Gray,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (menu.isAvailable) {
                            Text(
                                "+",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text("X", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}