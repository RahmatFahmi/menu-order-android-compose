package com.rahmat.testapp.ui.customers.screen.cart

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rahmat.testapp.R
import com.rahmat.testapp.common.utils.Constants
import com.rahmat.testapp.domain.model.CartItem
import com.rahmat.testapp.domain.model.Menu
import com.rahmat.testapp.ui.components.EmptyMenuState
import com.rahmat.testapp.ui.components.skeleton.rememberShimmerAlpha
import com.rahmat.testapp.ui.theme.AppColors
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun CartScreen(
    viewModel: CartViewModel = hiltViewModel(),
    onNavigateToPayment: (String) -> Unit, // Callback baru untuk pindah ke halaman pembayaran penuh
    onOrderSuccess: () -> Unit // Callback baru jika memilih CASH dan sukses
) {
    val state by viewModel.uiState.collectAsState()
    var isProcessingOrder by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        when (val res = state) {
            is CartUiState.Loading -> {
                CartSkeleton()
            }
            is CartUiState.Empty -> {
                EmptyMenuState(
                    resId = R.drawable.ic_cart_filled,
                    message = "Keranjang Masih Kosong"
                )
            }
            is CartUiState.Error -> {
                Text(
                    text = res.message,
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Red
                )
            }
            is CartUiState.Success -> {
                CartContent(
                    state = res,
                    onIncrease = { item -> viewModel.updateQuantity(item.menu.id, item.quantity, 1) },
                    onDecrease = { item -> if (item.quantity > 1) viewModel.updateQuantity(item.menu.id, item.quantity, -1) },
                    onDelete = { item -> viewModel.removeItem(item.menu.id) },
                    isProcessingOrder = isProcessingOrder,
                    onConfirmOrder = { method ->
                        isProcessingOrder = true

                        viewModel.checkoutOrder(method) { url, errorMessage ->
                            isProcessingOrder = false // Matikan loading tombol setelah dapat respon

                            if (errorMessage != null) {
                                Log.d("Error Cash", "CartScreen: $errorMessage")
                                Toast.makeText(context, "Gagal! Hubungi Kasir", Toast.LENGTH_LONG).show()
                            } else {
                                if (method.equals("NON_CASH", ignoreCase = true) && !url.isNullOrEmpty()) {
                                    val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                                    onNavigateToPayment(encodedUrl)
                                } else if (method.equals("CASH", ignoreCase = true)) {
                                    Toast.makeText(context, "Pesanan Tunai Berhasil Dibuat!", Toast.LENGTH_LONG).show()
                                    onOrderSuccess()
                                } else {
                                    Toast.makeText(context, "Metode pembayaran tidak dikenali", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CartSkeleton() {
    val alpha = rememberShimmerAlpha()
    val color = Color.LightGray.copy(alpha = alpha)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        repeat(5) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(AppColors.MenuCardBackground)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier
                                .weight(1f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color))
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier
                                .weight(1f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color))
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier
                                .weight(1f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier
                            .size(80.dp, 28.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color))
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(width = 48.dp, height = 38.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, bottomEnd = 24.dp))
                        .background(color)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartContent(
    state: CartUiState.Success,
    onIncrease: (CartItem) -> Unit,
    onDecrease: (CartItem) -> Unit,
    onDelete: (CartItem) -> Unit,
    isProcessingOrder: Boolean,
    onConfirmOrder: (paymentMethod: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf("NON_CASH") }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 220.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items = state.cartItems, key = { it.menu.id }) { item ->
                CartItemCard(
                    menu = item.menu,
                    quantity = item.quantity,
                    onIncrease = { onIncrease(item) },
                    onDecrease = { onDecrease(item) },
                    onDelete = { onDelete(item) }
                )
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            CheckoutSection(
                subTotal = state.subTotal,
                discount = state.discount,
                total = state.total,
                onCheckoutClick = { showBottomSheet = true  },
            )
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { if (!isProcessingOrder) showBottomSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .background(
                                Color.LightGray.copy(alpha = 0.5f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, bottom = 40.dp)
                ) {
                    Text(
                        text = "Konfirmasi Pesanan",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = AppColors.MenuCardBackground.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Pembayaran",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                            Text(
                                text = "Rp ${state.total}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppColors.AccentMenu
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Pilih Metode Pembayaran",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val isCash = selectedPaymentMethod == "CASH"
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(90.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(enabled = !isProcessingOrder) {
                                    selectedPaymentMethod = "CASH"
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isCash) AppColors.AccentMenu.copy(alpha = 0.06f) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isCash) 2.dp else 1.dp,
                                color = if (isCash) AppColors.AccentMenu else Color.LightGray.copy(alpha = 0.6f)
                            )
                        ) {
                            // 💡 PAKAI BOX: Supaya bisa menumpuk ikon di pojok kanan atas tanpa merusak posisi teks
                            Box(modifier = Modifier.fillMaxSize()) {

                                // Tanda Indikator Centang (Muncul hanya jika dipilih)
                                if (isCash) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Terpilih",
                                        tint = AppColors.AccentMenu,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd) // Geser ke pojok kanan atas
                                            .padding(8.dp)
                                            .size(18.dp) // Ukuran ikon yang pas dan proporsional
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .align(Alignment.Center), // Memastikan teks tetap presisi di tengah kartu
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Tunai / Kasir",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCash) AppColors.AccentMenu else AppColors.TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Bayar langsung",
                                        fontSize = 11.sp,
                                        color = if (isCash) AppColors.AccentMenu.copy(alpha = 0.7f) else Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        val isNonCash = selectedPaymentMethod == "NON_CASH"
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(90.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(enabled = !isProcessingOrder) {
                                    selectedPaymentMethod = "NON_CASH"
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isNonCash) AppColors.AccentMenu.copy(alpha = 0.06f) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isNonCash) 2.dp else 1.dp,
                                color = if (isNonCash) AppColors.AccentMenu else Color.LightGray.copy(alpha = 0.6f)
                            )
                        ) {
                            // 💡 PAKAI BOX JUGA DI SINI
                            Box(modifier = Modifier.fillMaxSize()) {

                                // Tanda Indikator Centang
                                if (isNonCash) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Terpilih",
                                        tint = AppColors.AccentMenu,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .size(18.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .align(Alignment.Center),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Digital Payment",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isNonCash) AppColors.AccentMenu else AppColors.TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Otomatis & aman",
                                        fontSize = 11.sp,
                                        color = if (isNonCash) AppColors.AccentMenu.copy(alpha = 0.7f) else Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    Button(
                        onClick = {
                            onConfirmOrder(selectedPaymentMethod)
                            //showBottomSheet = false
                        },
                        enabled = !isProcessingOrder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.AccentMenu,
                            disabledContainerColor = AppColors.AccentMenu.copy(alpha = 0.5f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (isProcessingOrder) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = if (selectedPaymentMethod == "CASH") "Selesai & Pesan" else "Lanjut ke Pembayaran",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    menu: Menu,
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = AppColors.MenuCardBackground,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(80.dp)) {
                    AsyncImage(
                        model = Constants.BASE_IMAGE_URL + "storage/" + menu.imageUrl,
                        contentDescription = menu.name,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(80.dp),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.ic_placeholde_food),
                        error = painterResource(id = R.drawable.ic_placeholde_food)
                    )

                    if (!menu.discountLabel.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .offset(x = (-4).dp, y = (-4).dp)
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

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = menu.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Rp. ${menu.finalPrice.toInt()}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (!menu.discountLabel.isNullOrEmpty()) {
                            Text(
                                text = "Rp. ${menu.price.toInt()}",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                style = androidx.compose.ui.text.TextStyle(textDecoration = TextDecoration.LineThrough),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = AppColors.AccentMenu,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = " ${menu.preparationTime}",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            onClick = onDecrease,
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_minus),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(16.dp)
                            )
                        }

                        Text(
                            text = quantity.toString(),
                            modifier = Modifier.padding(horizontal = 12.dp),
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )

                        Surface(
                            onClick = onIncrease,
                            shape = RoundedCornerShape(4.dp),
                            color = AppColors.AccentMenu
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_plus),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(16.dp)
                            )
                        }
                    }
                }
            }

            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                Surface(
                    onClick = onDelete,
                    color = AppColors.AccentMenu,
                    shape = RoundedCornerShape(topStart = 16.dp, bottomEnd = 24.dp),
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_trash),
                        contentDescription = "Hapus",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CheckoutSection(
    subTotal: Int,
    discount: Int,
    total: Int,
    onCheckoutClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = AppColors.MenuCardBackground,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 30.dp, top = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            PriceRow(label = "Sub Total", price = subTotal)
            Spacer(modifier = Modifier.height(8.dp))
            PriceRow(label = "Discount", price = discount)
            Spacer(modifier = Modifier.height(8.dp))
            PriceRow(label = "Total", price = total, isTotal = true)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onCheckoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.AccentMenu)
            ) {
                Text(text = "Checkout", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PriceRow(label: String, price: Int, isTotal: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = label,
            fontSize = if (isTotal) 16.sp else 12.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Medium,
            color = AppColors.TextPrimary
        )
        Text(
            text = "Rp. $price",
            fontSize = if (isTotal) 16.sp else 12.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Medium,
            color = AppColors.TextPrimary
        )
    }
}