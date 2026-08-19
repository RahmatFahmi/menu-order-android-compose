package com.rahmat.testapp.ui.customers.screen.detail

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rahmat.testapp.R
import com.rahmat.testapp.common.utils.Constants
import com.rahmat.testapp.domain.model.Menu
import com.rahmat.testapp.ui.common.ObserveAsEvents
import com.rahmat.testapp.ui.common.UiEvent
import com.rahmat.testapp.ui.components.MenuRecommendation
import com.rahmat.testapp.ui.components.skeleton.RecommendationCardSkeleton
import com.rahmat.testapp.ui.theme.AppColors
import com.rahmat.testapp.ui.theme.AppDimens
import kotlinx.coroutines.launch

@Composable
fun ProductDetailScreen(
    viewModel: DetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onMenuClick: (Int) -> Unit
) {
    val menu by viewModel.menu.collectAsState()
    val recommendationState by viewModel.uiState.collectAsState()
    var quantity by remember { mutableIntStateOf(1) }
    val context = LocalContext.current
    var showRatingDialog by remember { mutableStateOf(false) }
    var userRating by remember { mutableStateOf(4.5f) }

    if (menu == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AppColors.AccentMenu)
        }
        return
    }

    if (showRatingDialog) {
        val (emoji, label) = getRatingExpression(userRating)

        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            title = {
                Text(
                    "Rating untuk ${menu!!.name}",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRatingDialog = false
                    viewModel.storeRate(userRating)
                }) { Text("Kirim") }
            },
            dismissButton = {
                TextButton(onClick = { showRatingDialog = false }) { Text("Batal") }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tampilan Emoji yang Berubah-ubah
                    Text(
                        text = emoji,
                        fontSize = 60.sp // Buat ukurannya besar!
                    )

                    Text(
                        text = label + "${userRating}" ,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomRatingBar(
                        rating = userRating,
                        onRatingChange = { userRating = it },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        starSize = 40.dp
                    )
                }
            }
        )
    }

    ObserveAsEvents(viewModel.uiEvent) { event ->
        when (event) {
            is UiEvent.ShowToast -> {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val currentMenu = menu!!

    Scaffold(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        bottomBar = {
            BottomCartBar(menu = currentMenu, quantity = quantity, onCartClick = { id, qty -> viewModel.addMenuToCart(id, qty) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            ) {
                AsyncImage(
                    model = Constants.BASE_IMAGE_URL + "storage/"+ currentMenu.imageUrl,
                    contentDescription = currentMenu.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(270.dp),
                    contentScale = ContentScale.Fit,
                    placeholder = painterResource(id = R.drawable.ic_placeholde_food),
                    error = painterResource(id = R.drawable.ic_placeholde_food),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppDimens.HorizontalScreenPadding),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailIconButton(
                        resId = R.drawable.ic_arrow_left,
                        onClick = onBackClick
                    )
                    DetailIconButton(
                        resId = if (currentMenu.isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_hearth,
                        tint = if (currentMenu.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface,
                        onClick = { viewModel.toggleFavorite() }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-30).dp)
                    .clip(RoundedCornerShape(topStart = 35.dp, topEnd = 35.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 24.dp, horizontal = AppDimens.HorizontalScreenPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColors.AccentMenu.copy(alpha = 0.2f))
                            .clickable{ showRatingDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AppColors.AccentMenu,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = currentMenu.averageRating.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.AccentMenu)
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CounterButton("-") { if (quantity > 1) quantity-- }
                        Text(
                            text = "$quantity",
                            modifier = Modifier.padding(horizontal = 12.dp),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        CounterButton("+") { quantity++ }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentMenu.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_timer),
                            contentDescription = null,
                            tint = AppColors.AccentMenu,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = " 10 Menit",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentMenu.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Rekomendasi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                when (val state = recommendationState){
                    is RecommendationUiState.Success -> {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            items(state.latestMenus, key = { it.id }) { item ->
                                MenuRecommendation(
                                    menu = item,
                                    onItemClick = {onMenuClick(item.id)},
                                    onCartClick = {
                                        viewModel.addMenuToCart(item.id, 1)
                                    }
                                )
                            }
                        }
                    }
                    is RecommendationUiState.Error -> {
                        Text(
                            text = state.message,
                            modifier = Modifier.padding(horizontal = AppDimens.HorizontalScreenPadding),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    is RecommendationUiState.Loading -> {
                        RecommendationCardSkeleton()
                    }
                    else -> Unit
                }
            }
        }
    }
}

@Composable
fun DetailIconButton(resId: Int, tint: Color = MaterialTheme.colorScheme.onSurface,onClick: () -> Unit = {}) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp,
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = resId),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = tint
            )
        }
    }
}

@Composable
fun CounterButton(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.2f),
        modifier = Modifier.size(30.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(2.dp)
            )
        }
    }
}

@Composable
fun BottomCartBar(
    menu: Menu,
    quantity: Int,
    onCartClick: (Int, Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 8.dp,
        shadowElevation = 20.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = AppDimens.HorizontalScreenPadding, vertical = 20.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Total Harga",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Text(
                    text = "Rp. ${String.format("%,.0f", menu.price * quantity)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Button(
                onClick = {
                    onCartClick(menu.id, quantity)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.AccentMenu),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .wrapContentHeight()
                    .width(150.dp)
            ) {
                Text(
                    text = "Tambah Keranjang",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun CustomRatingBar(
    modifier: Modifier = Modifier,
    rating: Float,
    onRatingChange: (Float) -> Unit,
    starCount: Int = 5,
    starSize: Dp = 32.dp
) {
    Row(modifier = modifier) {
        repeat(starCount) { index ->
            val starIndex = index + 1
            Box(
                modifier = Modifier
                    .size(starSize)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val value = if (offset.x < size.width / 2) {
                                starIndex - 0.5f
                            } else {
                                starIndex.toFloat()
                            }
                            onRatingChange(value)
                        }
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.StarBorder,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.fillMaxSize()
                )

                val fillWidth = when {
                    rating >= starIndex -> 1f
                    rating >= starIndex - 0.5f -> 0.5f
                    else -> 0f
                }

                if (fillWidth > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(FractionalClip(fillWidth))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB400),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

private class FractionalClip(val fraction: Float) : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): androidx.compose.ui.graphics.Outline {
        return androidx.compose.ui.graphics.Outline.Rectangle(
            androidx.compose.ui.geometry.Rect(0f, 0f, size.width * fraction, size.height)
        )
    }
}

fun getRatingExpression(rating: Float): Pair<String, String> {
    return when {
        rating <= 1.0f -> "🤢" to "Buruk Sekali"
        rating <= 2.0f -> "😕" to "Kurang Enak"
        rating <= 3.0f -> "😐" to "Biasa Saja"
        rating <= 4.0f -> "😋" to "Enak!"
        rating >= 4.5f -> "🤩" to "Luar Biasa!"
        else -> "🤔" to "Pilih Rating"
    }
}
