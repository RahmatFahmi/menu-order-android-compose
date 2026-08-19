package com.rahmat.testapp.ui.employee.screen.finish

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

enum class DateFilterPeriod(val label: String) {
    ALL("Semua"),
    TODAY("Hari Ini"),
    THIS_MONTH("Bulan Ini"),
    CUSTOM("Pilih Tanggal")
}

@HiltViewModel
class EmployeeFinishedViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _rawOrders = MutableStateFlow<List<Order>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(DateFilterPeriod.ALL)
    val selectedPeriod: StateFlow<DateFilterPeriod> = _selectedPeriod.asStateFlow()

    private val _selectedCustomDate = MutableStateFlow<Long?>(null)
    val selectedCustomDate: StateFlow<Long?> = _selectedCustomDate.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

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

    val uiState: StateFlow<EmployeeFinishedOrderUiState> = combine(
        _rawOrders,
        _searchQuery,
        _selectedPeriod,
        _selectedCustomDate,
        _loadStatus
    ) { orders, query, period, customDate, status ->
        when {
            status.isLoading -> EmployeeFinishedOrderUiState.Loading
            status.errorMsg != null -> EmployeeFinishedOrderUiState.Error(status.errorMsg)
            else -> {
                val timeFiltered = filterByPeriod(orders, period, customDate)

                val searchFiltered = if (query.isBlank()) {
                    timeFiltered
                } else {
                    timeFiltered.filter { order ->
                        val matchId = order.id.toString().contains(query, ignoreCase = true)
                        val matchMenu = order.items?.any { item ->
                            item.menuName.contains(query, ignoreCase = true)
                        } == true
                        matchId || matchMenu
                    }
                }

                if (searchFiltered.isEmpty()) {
                    EmployeeFinishedOrderUiState.Empty
                } else {
                    EmployeeFinishedOrderUiState.Success(searchFiltered)
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EmployeeFinishedOrderUiState.Loading
    )

    init {
        getFinishedOrders()
    }

    fun getFinishedOrders() {
        viewModelScope.launch {
            if (!_isRefreshing.value) {
                _isLoading.value = true
            }
            _errorMessage.value = null

            try {
                val token = authManager.getToken.first() ?: ""

                orderRepository.getOrders(
                    bearerToken = token,
                    status = "selesai",
                    userId = "",
                    tableId = ""
                ).collect { result ->
                    _isLoading.value = false
                    result.onSuccess { orders ->
                        val finished = orders.filter {
                            it.status.equals("selesai", ignoreCase = true)
                        }
                        _rawOrders.value = finished
                    }.onFailure { exception ->
                        _errorMessage.value = exception.localizedMessage ?: "Gagal memuat pesanan"
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

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onPeriodSelected(period: DateFilterPeriod) {
        _selectedPeriod.value = period
    }

    fun onCustomDateSelected(timeInMillis: Long) {
        _selectedCustomDate.value = timeInMillis
        _selectedPeriod.value = DateFilterPeriod.CUSTOM
    }

    fun refreshData() {
        _isRefreshing.value = true
        getFinishedOrders()
    }

    private fun filterByPeriod(
        orders: List<Order>,
        period: DateFilterPeriod,
        customDateMillis: Long?
    ): List<Order> {
        if (period == DateFilterPeriod.ALL) return orders

        val calendarToday = Calendar.getInstance()

        return orders.filter { order ->
            val orderDate = parseOrderDate(order.createdAt ?: "") ?: return@filter false

            when (period) {
                DateFilterPeriod.TODAY -> isSameDay(orderDate, calendarToday.time)

                DateFilterPeriod.THIS_MONTH -> {
                    val orderCal = Calendar.getInstance().apply { time = orderDate }
                    orderCal.get(Calendar.YEAR) == calendarToday.get(Calendar.YEAR) &&
                            orderCal.get(Calendar.MONTH) == calendarToday.get(Calendar.MONTH)
                }

                DateFilterPeriod.CUSTOM -> {
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