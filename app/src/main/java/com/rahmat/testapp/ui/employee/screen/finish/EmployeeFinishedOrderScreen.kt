package com.rahmat.testapp.ui.employee.screen.finish

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rahmat.testapp.R
import com.rahmat.testapp.common.utils.Constants
import com.rahmat.testapp.domain.model.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeFinishedOrderScreeen(
    viewModel: EmployeeFinishedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val selectedCustomDate by viewModel.selectedCustomDate.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var showDatePickerDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val pullToRefreshState = rememberPullToRefreshState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // ----------------------------------------------------
        // 1. BAGIAN SEARCH BAR
        // ----------------------------------------------------
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Cari Order ID atau Menu...", fontSize = 13.sp) },
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

        Spacer(modifier = Modifier.height(8.dp))

        // ----------------------------------------------------
        // 2. BAGIAN FILTER CHIPS (Semua, Hari Ini, Bulan Ini, Pilih Tanggal)
        // ----------------------------------------------------
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(DateFilterPeriod.entries.toTypedArray()) { period ->
                val isSelected = selectedPeriod == period

                val chipLabel = if (period == DateFilterPeriod.CUSTOM && selectedCustomDate != null && isSelected) {
                    val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(Date(selectedCustomDate!!))
                    "Tanggal: $formattedDate"
                } else {
                    period.label
                }

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (period == DateFilterPeriod.CUSTOM) {
                            showDatePickerDialog = true
                        } else {
                            viewModel.onPeriodSelected(period)
                        }
                    },
                    label = { Text(chipLabel, fontSize = 12.sp) },
                    leadingIcon = if (period == DateFilterPeriod.CUSTOM) {
                        {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ----------------------------------------------------
        // 3. DIALOG KALENDER (MATERIAL 3 DATE PICKER)
        // ----------------------------------------------------
        if (showDatePickerDialog) {
            DatePickerDialog(
                onDismissRequest = { showDatePickerDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                viewModel.onCustomDateSelected(millis)
                            }
                            showDatePickerDialog = false
                        }
                    ) {
                        Text("Pilih")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePickerDialog = false }) {
                        Text("Batal")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ----------------------------------------------------
        // 4. PENANGANAN STATE UI (Loading, Empty, Error, Success) + PULL TO REFRESH
        // ----------------------------------------------------
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshData() },
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is EmployeeFinishedOrderUiState.Loading -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(3) {
                            EmployeeFinishedOrderCardSkeleton()
                        }
                    }
                }

                is EmployeeFinishedOrderUiState.Empty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyFinishedOrderState()
                    }
                }

                is EmployeeFinishedOrderUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                is EmployeeFinishedOrderUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = state.orders,
                            key = { order -> order.id }
                        ) { order ->
                            EmployeeFinishedOrderCard(order = order)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// KOMPONEN KARTU DETAIL PESANAN
// ----------------------------------------------------
@Composable
fun EmployeeFinishedOrderCard(
    order: Order,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
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
                    Text(
                        text = "#ORD-${order.id}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                SuggestionChip(
                    onClick = { },
                    label = { Text(text = "SELESAI", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF1E4620)) },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFFDCFCE7)),
                    border = null,
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.height(24.dp)
                )
            }

            Text(
                text = "Metode: ${(order.paymentMethod ?: "N/A").uppercase()} • ${order.createdAt ?: ""}",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 12.dp, top = 2.dp, end = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 12.dp))
            Spacer(modifier = Modifier.height(10.dp))

            val itemsList = order.items ?: emptyList()
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
                    val displayItems = itemsList.take(maxImages)

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
                                model = if (!item.menuItemImage.isNullOrEmpty())
                                    Constants.BASE_IMAGE_URL + "storage/" + item.menuItemImage
                                else null,
                                contentDescription = item.menuName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                placeholder = painterResource(id = R.drawable.ic_launcher_background),
                                error = painterResource(id = R.drawable.ic_launcher_background)
                            )
                        }
                    }

                    if (itemsList.size > maxImages) {
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
                                text = "+${itemsList.size - maxImages}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                val summaryText = if (itemsList.isNotEmpty()) {
                    itemsList.joinToString(separator = ", ") { "${it.menuName} (${it.jumlah}x)" }
                } else {
                    "Tidak ada detail item"
                }

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
                Text(text = "Total Pendapatan", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = "Rp ${String.format("%,.0f", order.totalPrice.toDouble())}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ----------------------------------------------------
// SKELETON LOADING (dipakai saat state Loading, biar konsisten dgn Process screen)
// ----------------------------------------------------
@Composable
fun EmployeeFinishedOrderCardSkeleton() {
    val color = Color(0xFFE0E0E0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
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
        }
    }
}

// ----------------------------------------------------
// KOMPONEN TAMPILAN KOSONG (EMPTY STATE)
// ----------------------------------------------------
@Composable
fun EmptyFinishedOrderState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.LightGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tidak Ada Pesanan Selesai",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tidak ditemukan pesanan yang sesuai dengan filter atau pencarian Anda.",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}