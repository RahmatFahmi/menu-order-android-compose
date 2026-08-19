package com.rahmat.testapp.ui.components


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rahmat.testapp.domain.model.Menu
import com.rahmat.testapp.ui.theme.AppColors
import com.rahmat.testapp.ui.theme.White_100
import com.rahmat.testapp.R
import com.rahmat.testapp.common.utils.Constants

@Composable
fun MenuCard(
    menu: Menu, // Langsung terima objek Menu
    modifier: Modifier = Modifier,
    onItemClick: () -> Unit,
    onCartClick: () -> Unit,
) {
    Surface(
        onClick = onItemClick,
        shape = RoundedCornerShape(20.dp),
        color = AppColors.MenuCardBackground,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(110.dp)) {
                    AsyncImage(
                        model = Constants.BASE_IMAGE_URL + "storage/"+ menu.imageUrl,
                        contentDescription = menu.name,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(110.dp),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.ic_placeholde_food),
                        error = painterResource(id = R.drawable.ic_placeholde_food),
                    )

                    // Menggunakan discountLabel ("10%")
                    if (!menu.discountLabel.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .offset(x = (-2).dp, y = (-2).dp)
                                .size(36.dp)
                                .background(AppColors.AccentMenu, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = menu.discountLabel,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 45.dp)
                ) {
                    Text(
                        text = menu.name, // Pakai menu.name
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = menu.description,
                        fontSize = 12.sp,
                        color = AppColors.TextPrimary.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = AppColors.AccentMenu,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = " ${menu.averageRating} (${menu.totalRatings})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = AppColors.AccentMenu,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = " ${menu.preparationTime}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Harga Final
                        Text(
                            text = "Rp ${menu.finalPrice.toInt()}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppColors.TextPrimary
                        )

                        // Harga Coret (Hanya tampil jika ada diskon)
                        if (!menu.discountLabel.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Rp ${menu.price.toInt()}",
                                fontSize = 11.sp,
                                color = AppColors.TextPrimary.copy(alpha = 0.5f),
                                style = androidx.compose.ui.text.TextStyle(
                                    textDecoration = TextDecoration.LineThrough
                                )
                            )
                        }
                    }
                }
            }

            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                if(menu.isAvailable){
                    Surface(
                        onClick = onCartClick,
                        color = AppColors.CartButton,
                        shape = RoundedCornerShape(topStart = 16.dp),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .size(20.dp)
                        )
                    }
                }else{
                    Surface(
                        color = AppColors.CartButton,
                        shape = RoundedCornerShape(topStart = 16.dp),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Text(
                            text = "Habis",
                            color = White_100,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                }

            }
        }
    }
}