package com.rahmat.testapp.ui.customers.screen.order.pending

import android.R.attr.order
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rahmat.testapp.R
import com.rahmat.testapp.common.utils.Constants
import com.rahmat.testapp.domain.model.Order
import com.rahmat.testapp.domain.model.OrderDisplayStatus
import com.rahmat.testapp.ui.common.ObserveAsEvents
import com.rahmat.testapp.ui.common.UiEvent
import com.rahmat.testapp.ui.components.ConfirmationBottomSheet
import com.rahmat.testapp.ui.components.EmptyMenuState
import com.rahmat.testapp.ui.components.skeleton.rememberShimmerAlpha
import com.rahmat.testapp.ui.theme.AppDimens
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// Model lokal untuk membantu mapping ke UI Card
data class OrderItem(
    val menuName: String,
    val quantity: Int,
    val imageUrl: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingOrderScreen(
    viewModel: PendingOrderViewModel = hiltViewModel(),
    onNavigateToPayment: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // 1. Setup Pull-to-Refresh State
    val pullToRefreshState = rememberPullToRefreshState()
    val context = LocalContext.current
    var orderToCancel by remember { mutableStateOf<Order?>(null) }

    ObserveAsEvents(viewModel.uiEvent) { event ->
        when (event) {
            is UiEvent.ShowToast -> {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 2. Langsung bungkus layar dengan PullToRefreshBox (API Terbaru, lebih simpel!)
    PullToRefreshBox(
        state = pullToRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshData() },
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = uiState) {
            is PendingOrderUiState.Loading -> {
                PendingOrderCardSkeleton()
            }

            is PendingOrderUiState.Empty -> {
                // Tambahkan verticalScroll agar tetap bisa ditarik ke bawah meski kosong
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyMenuState(
                        resId = R.drawable.ic_search,
                        message = "Belum ada pesanan pending di meja ini."
                    )
                }
            }

            is PendingOrderUiState.Error -> {
                // Tambahkan verticalScroll agar tetap bisa ditarik ke bawah saat error
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = state.message, color = Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.refreshData() }) {
                        Text(text = "Coba Lagi")
                    }
                }
            }

            is PendingOrderUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = AppDimens.HorizontalScreenPadding, vertical = 12.dp)
                ) {
                    items(state.orders, key = { it.id }) { order ->
                        val mappedItems = order.items.map { lineItem ->
                            OrderItem(
                                menuName = lineItem.menuName,
                                quantity = lineItem.jumlah,
                                imageUrl = lineItem.menuItemImage
                            )
                        }

                        PendingOrderCard(
                            orderId = "#ORD-${order.id}",
                            items = mappedItems,
                            timeAgo = order.timeAgo,
                            totalAmount = "Rp ${order.totalPrice}",
                            paymentMethod = order.paymentMethod ?: "CASH",
                            status = order.displayStatus,
                            onCancelClick = {
                                orderToCancel = order
                            },
                            onRepayClick = {
                                viewModel.repayOrder(
                                    order.id,
                                    onResult = { snapUrl, errorMessage ->
                                        if (errorMessage != null) {
                                            Toast.makeText(context, "Gagal! Hubungi Kasir", Toast.LENGTH_SHORT).show()
                                        }else{
                                            if(!snapUrl.isNullOrEmpty()){
                                                val encodedUrl = URLEncoder.encode(snapUrl, StandardCharsets.UTF_8.toString())
                                                onNavigateToPayment(encodedUrl)
                                            }else{
                                                Toast.makeText(context, "Metode pembayaran tidak dikenali", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
        orderToCancel?.let {
            ConfirmationBottomSheet(
                title = "Batalkan Pesanan #${it.id}?",
                message = "Pesanan akan langsung dihapus dari antrean dapur dan tidak bisa dikembalikan.",
                confirmButtonText = "Ya, Batalkan Pesanan",
                isDangerousAction = true,
                onDismiss = {
                    orderToCancel = null
                },
                onConfirm = {
                    viewModel.cancelOrder(it.id) // Eksekusi ke backend
                    orderToCancel = null
                }
            )
        }
    }
}

@Composable
fun PendingOrderCard(
    orderId: String,
    items: List<OrderItem>,
    timeAgo: String,
    totalAmount: String,
    status: OrderDisplayStatus,
    paymentMethod: String,
    onCancelClick: () -> Unit,
    onRepayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD8E7FF))
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "ORDER ID", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(text = orderId, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                val chipProps = when (status) {
                    OrderDisplayStatus.WaitingPayment -> Triple("MENUNGGU PEMBAYARAN", Color(0xFF7A4B00), Color(0xFFFFECCE))
                    OrderDisplayStatus.KitchenQueue -> Triple("ANTREAN DAPUR", Color(0xFF1565C0), Color(0xFFE3F2FD))
                   else -> null
                }

                chipProps?.let { (text, textColor, containerColor) ->
                    SuggestionChip(
                        onClick = { },
                        label = {
                            Text(
                                text = text,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                color = textColor
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = containerColor
                        ),
                        border = null,
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.height(24.dp)
                    )
                }

            }

            Text(
                text = "Metode: ${paymentMethod.uppercase()} • Placed $timeAgo",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 12.dp, top = 2.dp, end = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 12.dp))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.padding(end = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    val maxImages = 3
                    val displayItems = items.take(maxImages)

                    displayItems.forEachIndexed { index, item ->
                        Box(
                            modifier = Modifier
                                .padding(start = (index * 24).dp)
                                .size(40.dp)
                                .border(2.dp, Color.White, CircleShape)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        ) {
                            AsyncImage(
                                model = if (!item.imageUrl.isNullOrEmpty()) {
                                    Constants.BASE_IMAGE_URL + "storage/" + item.imageUrl.replace("\\/", "/")
                                } else {
                                    null
                                },
                                contentDescription = "Menu Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                placeholder = painterResource(id = R.drawable.ic_launcher_background),
                                error = painterResource(id = R.drawable.ic_launcher_background)
                            )
                        }
                    }

                    if (items.size > maxImages) {
                        Box(
                            modifier = Modifier
                                .padding(start = (maxImages * 24).dp)
                                .size(40.dp)
                                .border(2.dp, Color.White, CircleShape)
                                .clip(CircleShape)
                                .background(Color(0xFFE0E0E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${items.size - maxImages}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                val summaryText = items.joinToString(separator = ", ") { "${it.menuName} (${it.quantity}x)" }
                Text(
                    text = summaryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(horizontal = 12.dp))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Total Pembayaran", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(text = totalAmount, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onCancelClick,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).padding(end = 6.dp)
                ) {
                    Text(text = "Batalkan", color = Color.Red, fontSize = 12.sp)
                }

                if (paymentMethod.equals("NON_CASH", ignoreCase = true)) {
                    Button(
                        onClick = onRepayClick,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(text = "Bayar Ulang", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PendingOrderCardSkeleton() {
    val alpha = rememberShimmerAlpha()
    val color = Color.LightGray.copy(alpha = alpha)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFFD8E7FF))
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {

            // Header — Order ID + Status chip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Box(modifier = Modifier.width(60.dp).height(10.dp).clip(RoundedCornerShape(4.dp)).background(color))
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.width(100.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).background(color))
                }
                Box(modifier = Modifier.width(70.dp).height(24.dp).clip(RoundedCornerShape(50.dp)).background(color))
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle — metode + waktu
            Box(modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth(0.6f).height(11.dp).clip(RoundedCornerShape(4.dp)).background(color))

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 12.dp))
            Spacer(modifier = Modifier.height(10.dp))

            // Images + summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Overlapping circles
                Box(modifier = Modifier.padding(end = 8.dp)) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .padding(start = (index * 24).dp)
                                .size(40.dp)
                                .border(2.dp, Color.White, CircleShape)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text summary
                Column(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.fillMaxWidth(0.9f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(color))
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth(0.6f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(color))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(horizontal = 12.dp))
            Spacer(modifier = Modifier.height(8.dp))

            // Total
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.width(100.dp).height(13.dp).clip(RoundedCornerShape(4.dp)).background(color))
                Box(modifier = Modifier.width(80.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).background(color))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Box(modifier = Modifier.width(80.dp).height(32.dp).clip(RoundedCornerShape(6.dp)).background(color))
                Spacer(modifier = Modifier.width(6.dp))
                Box(modifier = Modifier.width(90.dp).height(32.dp).clip(RoundedCornerShape(6.dp)).background(color))
            }
        }
    }
}