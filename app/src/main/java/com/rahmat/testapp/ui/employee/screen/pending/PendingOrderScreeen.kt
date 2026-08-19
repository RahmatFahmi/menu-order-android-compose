package com.rahmat.testapp.ui.employee.screen.pending

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rahmat.testapp.R
import com.rahmat.testapp.common.utils.Constants
import com.rahmat.testapp.common.utils.theme.ThemeViewModel
import com.rahmat.testapp.domain.model.Order
import com.rahmat.testapp.ui.common.ObserveAsEvents
import com.rahmat.testapp.ui.common.UiEvent
import com.rahmat.testapp.ui.components.ConfirmationBottomSheet
import com.rahmat.testapp.ui.components.EmptyMenuState
import com.rahmat.testapp.ui.components.skeleton.rememberShimmerAlpha
import com.rahmat.testapp.ui.employee.screen.EmployeeMainScreen
import com.rahmat.testapp.ui.theme.AppDimens

data class EmployeeOrderItem(
    val menuName: String,
    val quantity: Int,
    val imageUrl: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeePendingOrderScreeen(
    viewModel: EmployeePendingOrderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    val context = LocalContext.current
    var orderToCancel by remember { mutableStateOf<Order?>(null) }
    var orderToQueue by remember { mutableStateOf<Order?>(null) }

    ObserveAsEvents(viewModel.uiEvent) { event ->
        when (event) {
            is UiEvent.ShowToast -> {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            placeholder = { Text("Cari Order ID...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = Color.Gray
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color(0xFFE2E8F0)
            )
        )

        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshData() },
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (val state = uiState) {
                    is EmployeePendingOrderUiState.Loading -> {
                        items(3) {
                            EmployeePendingOrderCardSkeleton()
                        }
                    }

                    is EmployeePendingOrderUiState.Success -> {
                        items(items = state.orders, key = { it.id }) { order ->
                            EmployeePendingOrderCard(
                                orderId = "#${order.id}",
                                items = order.items.map { item ->
                                    EmployeeOrderItem(
                                        menuName = item.menuName,
                                        quantity = item.jumlah,
                                        imageUrl = item.menuItemImage
                                    )
                                },
                                timeAgo = order.timeAgo,
                                totalAmount = "Rp ${String.format("%,.0f", order.totalPrice.toDouble())}",
                                paymentMethod = order.paymentMethod ?: "CASH",
                                tableName = order.tableName ?: "Tidak Terdaftar",
                                onRejectClick = {
                                    orderToCancel = order
                                },
                                onConfirmClick = {
                                    if (order.paymentMethod == "CASH") {
                                        orderToQueue = order
                                    } else {
                                        viewModel.showToast(message = "Silahkan Lakukan Pembayaran Digital")
                                    }
                                }
                            )
                        }
                    }

                    is EmployeePendingOrderUiState.Empty -> {
                        item {
                            EmptyMenuState(
                                resId = R.drawable.ic_search,
                                message = "Belum ada pesanan pending"
                            )
                        }
                    }

                    is EmployeePendingOrderUiState.Error -> {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { viewModel.refreshData() }) {
                                    Text("Coba Lagi")
                                }
                            }
                        }
                    }
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
                viewModel.rejectOrder(it.id)
                orderToCancel = null
            }
        )
    }
    orderToQueue?.let {
        ConfirmationBottomSheet(
            title = "Pesanan #${it.id}?",
            message = "Pesanan akan langsung masuk ke antrean dapur dan tidak bisa dikembalikan.",
            confirmButtonText = "Konfirmasi",
            isDangerousAction = true,
            onDismiss = {
                orderToQueue = null
            },
            onConfirm = {
                viewModel.confirmManualPaymentOrder(it.id)
                orderToQueue = null
            }
        )
    }
}

@Composable
fun EmployeePendingOrderCard(
    orderId: String,
    items: List<EmployeeOrderItem>,
    timeAgo: String,
    totalAmount: String,
    paymentMethod: String,
    tableName: String,
    onRejectClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFFD8E7FF))
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {

            // Header
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
                SuggestionChip(
                    onClick = { },
                    label = { Text(text = "MENUNGGU PEMBAYARAN", fontWeight = FontWeight.Bold, fontSize = 8.sp, color = Color(0xFF7A4B00)) },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFFFFECCE)),
                    border = null,
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.height(24.dp)
                )
            }

            // Subtitle
            Text(
                text = "${paymentMethod.uppercase()} • $timeAgo • $tableName",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth() // 2. Memenuhi lebar wadah agar perataan tengah berfungsi
                    .padding(start = 12.dp, top = 2.dp, end = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 12.dp))
            Spacer(modifier = Modifier.height(10.dp))

            // Images + summary
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
                                model = if (!item.imageUrl.isNullOrEmpty())
                                    Constants.BASE_IMAGE_URL + "storage/" + item.imageUrl
                                else null,
                                contentDescription = item.menuName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                placeholder = painterResource(id = R.drawable.ic_placeholde_food),
                                error = painterResource(id = R.drawable.ic_placeholde_food)
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

                val summaryText = items.joinToString(", ") { "${it.menuName} (${it.quantity}x)" }
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

            // Total
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Total Tagihan", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(text = totalAmount, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onRejectClick,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Tolak Pesanan",
                        modifier = Modifier.size(18.dp)
                    )
                }

                Button(
                    onClick = onConfirmClick,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                    contentPadding = PaddingValues(vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Text(text = "Konfirmasi Pembayaran", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun EmployeePendingOrderCardSkeleton() {
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

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
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
            Box(modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth(0.6f).height(11.dp).clip(RoundedCornerShape(4.dp)).background(color))

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 12.dp))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.fillMaxWidth(0.9f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(color))
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth(0.6f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(color))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(horizontal = 12.dp))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.width(100.dp).height(13.dp).clip(RoundedCornerShape(4.dp)).background(color))
                Box(modifier = Modifier.width(80.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).background(color))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.width(48.dp).height(36.dp).clip(RoundedCornerShape(6.dp)).background(color))
                Box(modifier = Modifier.weight(1f).height(36.dp).clip(RoundedCornerShape(6.dp)).background(color))
            }
        }
    }
}

