package com.rahmat.testapp.ui.customers.screen.order.finished

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahmat.testapp.data.local.AuthManager
import com.rahmat.testapp.domain.model.Order
import com.rahmat.testapp.domain.repository.OrderRepository
import com.rahmat.testapp.ui.common.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

enum class FinishedDateFilterPeriod(val label: String) {
    ALL("Semua"),
    TODAY("Hari Ini"),
    THIS_MONTH("Bulan Ini"),
    CUSTOM("Pilih Tanggal")
}

@HiltViewModel
class FinishedOrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _rawOrders = MutableStateFlow<List<Order>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val _selectedPeriod = MutableStateFlow(FinishedDateFilterPeriod.ALL)
    val selectedPeriod: StateFlow<FinishedDateFilterPeriod> = _selectedPeriod.asStateFlow()

    private val _selectedCustomDate = MutableStateFlow<Long?>(null)
    val selectedCustomDate: StateFlow<Long?> = _selectedCustomDate.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private data class LoadStatus(val isLoading: Boolean, val errorMsg: String?)

    private val _loadStatus: StateFlow<LoadStatus> = combine(
        _isLoading,
        _errorMessage
    ) { isLoading, errorMsg ->
        LoadStatus(isLoading, errorMsg)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LoadStatus(isLoading = true, errorMsg = null)
    )

    val uiState: StateFlow<FinishedOrderUiState> = combine(
        _rawOrders,
        _selectedPeriod,
        _selectedCustomDate,
        _loadStatus
    ) { orders, period, customDate, status ->
        when {
            status.isLoading -> FinishedOrderUiState.Loading
            status.errorMsg != null -> FinishedOrderUiState.Error(status.errorMsg)
            else -> {
                val filtered = filterByPeriod(orders, period, customDate)
                if (filtered.isEmpty())
                    FinishedOrderUiState.Empty
                else
                    FinishedOrderUiState.Success(filtered)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinishedOrderUiState.Loading
    )

    init {
        getFinishOrders()
    }

    fun getFinishOrders() {
        viewModelScope.launch {
            if (!_isRefreshing.value) {
                _isLoading.value = true
            }
            _errorMessage.value = null

            try {
                val token = authManager.getToken.first() ?: ""
                val userId = authManager.getId.first() ?: ""
                val tableId = authManager.getTableId.first() ?: ""

                orderRepository.getOrders(
                    bearerToken = token,
                    status = "selesai",
                    userId = userId,
                    tableId = tableId
                ).collect { result ->
                    _isLoading.value = false
                    result.onSuccess { activeOrders ->
                        _rawOrders.value = activeOrders
                    }.onFailure { exception ->
                        _errorMessage.value = exception.localizedMessage ?: "Gagal memuat data pesanan"
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.localizedMessage ?: "Terjadi kesalahan"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun onPeriodSelected(period: FinishedDateFilterPeriod) {
        _selectedPeriod.value = period
    }

    fun onCustomDateSelected(timeInMillis: Long) {
        _selectedCustomDate.value = timeInMillis
        _selectedPeriod.value = FinishedDateFilterPeriod.CUSTOM
    }

    fun refreshData() {
        _isRefreshing.value = true
        getFinishOrders()
    }

    private fun filterByPeriod(
        orders: List<Order>,
        period: FinishedDateFilterPeriod,
        customDateMillis: Long?
    ): List<Order> {
        if (period == FinishedDateFilterPeriod.ALL) return orders

        val calendarToday = Calendar.getInstance()

        return orders.filter { order ->
            val orderDate = parseOrderDate(order.createdAt) ?: return@filter false

            when (period) {
                FinishedDateFilterPeriod.TODAY -> isSameDay(orderDate, calendarToday.time)

                FinishedDateFilterPeriod.THIS_MONTH -> {
                    val orderCal = Calendar.getInstance().apply { time = orderDate }
                    orderCal.get(Calendar.YEAR) == calendarToday.get(Calendar.YEAR) &&
                            orderCal.get(Calendar.MONTH) == calendarToday.get(Calendar.MONTH)
                }

                FinishedDateFilterPeriod.CUSTOM -> {
                    if (customDateMillis == null) true
                    else {
                        val targetCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = customDateMillis
                        }
                        val orderCal = Calendar.getInstance().apply { time = orderDate }

                        orderCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR) &&
                                orderCal.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR)
                    }
                }

                else -> true
            }
        }
    }

    private fun parseOrderDate(dateString: String): Date? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            format.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return fmt.format(date1) == fmt.format(date2)
    }
}