package com.rahmat.testapp.ui.employee.screen.process

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rahmat.testapp.R
import com.rahmat.testapp.common.utils.Constants
import com.rahmat.testapp.domain.model.Order
import com.rahmat.testapp.ui.common.ObserveAsEvents
import com.rahmat.testapp.ui.common.UiEvent
import com.rahmat.testapp.ui.components.ConfirmationBottomSheet
import com.rahmat.testapp.ui.components.EmptyMenuState
import com.rahmat.testapp.ui.components.skeleton.rememberShimmerAlpha
import com.rahmat.testapp.ui.theme.AppDimens
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

data class ProcessOrderItem(
    val menuName: String,
    val quantity: Int,
    val imageUrl: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeProcessOrderScreeen(
    viewModel: EmployeeProcessOrderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    val context = LocalContext.current
    var orderToFinish by remember { mutableStateOf<Order?>(null) }

    ObserveAsEvents(viewModel.uiEvent) { event ->
        when (event) {
            is UiEvent.ShowToast -> {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    PullToRefreshBox(
        state = pullToRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshData() },
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = uiState) {
            is EmployeeProcessOrderUiState.Loading -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(3) {
                        EmployeeProcessOrderCardSkeleton()
                    }
                }
            }

            is EmployeeProcessOrderUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = AppDimens.HorizontalScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = state.orders, key = { it.id }) { order ->
                        EmployeeProcessOrderCard(
                            orderId = "#${order.id}",
                            items = order.items.map { item ->
                                ProcessOrderItem(
                                    menuName = item.menuName,
                                    quantity = item.jumlah,
                                    imageUrl = item.menuItemImage
                                )
                            },
                            timeAgo = order.timeAgo,
                            totalAmount = "Rp ${String.format("%,.0f", order.totalPrice.toDouble())}",
                            paymentMethod = order.paymentMethod ?: "CASH",
                            tableName = order.tableName ?: "Tidak Terdaftar",
                            onCompleteClick = {
                                orderToFinish = order
                            }
                        )
                    }
                }
            }

            is EmployeeProcessOrderUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyMenuState(
                        resId = R.drawable.ic_search,
                        message = "Tidak ada pesanan yang sedang dimasak"
                    )
                }
            }

            is EmployeeProcessOrderUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
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

    orderToFinish?.let {
        ConfirmationBottomSheet(
            title = "Pesanan #${it.id}?",
            message = "Pesanan akan langsung masuk ke daftar selesai dan tidak bisa dikembalikan.",
            confirmButtonText = "Konfirmasi",
            isDangerousAction = true,
            onDismiss = {
                orderToFinish = null
            },
            onConfirm = {
                viewModel.finishOrder(it.id)
                orderToFinish = null
            }
        )
    }
}

@Composable
fun EmployeeProcessOrderCard(
    orderId: String,
    items: List<ProcessOrderItem>,
    timeAgo: String,
    totalAmount: String,
    paymentMethod: String,
    tableName: String,
    onCompleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDetailDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { showDetailDialog = true },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFFD8E7FF))
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

                SuggestionChip(
                    onClick = { },
                    label = { Text(text = "SEDANG DIMASAK", fontWeight = FontWeight.Bold, fontSize = 8.sp, color = Color(0xFFE65100)) },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFFFFF3E0)),
                    border = null,
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.height(24.dp)
                )
            }

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

            Button(
                onClick = onCompleteClick,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                contentPadding = PaddingValues(vertical = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .padding(horizontal = 12.dp)
            ) {
                Text(text = "Pesanan Selesai", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }

    if (showDetailDialog) {
        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            title = {
                Text(text = "Detail Pesanan $orderId", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
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

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.menuName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Jumlah: ${item.quantity}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetailDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
fun EmployeeProcessOrderCardSkeleton() {
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
                Box(modifier = Modifier.width(80.dp).height(24.dp).clip(RoundedCornerShape(50.dp)).background(color))
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .height(38.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
            )
        }
    }
}