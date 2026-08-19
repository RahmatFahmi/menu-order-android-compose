package com.rahmat.testapp.ui.customers.screen.order

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import com.rahmat.testapp.ui.customers.screen.order.finished.FinishedOrderScreen
import com.rahmat.testapp.ui.customers.screen.order.pending.PendingOrderScreen
import com.rahmat.testapp.ui.customers.screen.order.processing.ProcessingOrderScreen

@Composable
fun MainOrderScreen(
    onNavigateToPayment: (String) -> Unit,
) {
    // 1. Daftar nama tab
    val tabs = listOf("Pending", "Diproses", "Selesai")

    // 2. Ganti mutableIntStateOf dengan PagerState bawaan Compose
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    // 3. CoroutineScope dibutuhkan untuk menjalankan animasi saat Tab diklik
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {

        // Komponen Tab Bar (Atas)
        TabRow(
            selectedTabIndex = pagerState.currentPage, // Sinkronkan Tab dengan halaman Pager saat ini
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            divider = { HorizontalDivider() }
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = pagerState.currentPage == index
                Tab(
                    selected = isSelected,
                    onClick = {
                        // Jalankan animasi geser Pager ke tab yang diklik
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }
        }

        // 4. Komponen HorizontalPager (Pengganti if/when dinamis)
        // Memberikan Modifier.weight(1f) agar Pager mengisi seluruh sisa layar di bawah TabRow
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            // Isi dari masing-masing halaman berdasarkan index (0, 1, 2)
            when (page) {
                0 -> PendingOrderScreen(onNavigateToPayment = onNavigateToPayment)
                1 -> ProcessingOrderScreen()
                2 -> FinishedOrderScreen()
            }
        }
    }
}