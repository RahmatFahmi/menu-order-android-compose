package com.rahmat.testapp.ui.employee.screen.pending

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
import javax.inject.Inject

@HiltViewModel
class EmployeePendingOrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _rawOrders = MutableStateFlow<List<Order>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    // Kombinasi state loading & error jadi satu flow, supaya combine() di bawah tetap 4 flow (masuk typed overload)
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

    val uiState: StateFlow<EmployeePendingOrderUiState> = combine(
        _rawOrders,
        _searchQuery,
        _loadStatus
    ) { orders, query, status ->
        when {
            status.isLoading -> EmployeePendingOrderUiState.Loading
            status.errorMsg != null -> EmployeePendingOrderUiState.Error(status.errorMsg)
            else -> {
                // Filter khusus by Order ID saja
                val filtered = if (query.isBlank()) {
                    orders
                } else {
                    orders.filter { order ->
                        order.id.toString().contains(query, ignoreCase = true)
                    }
                }

                if (filtered.isEmpty())
                    EmployeePendingOrderUiState.Empty
                else
                    EmployeePendingOrderUiState.Success(filtered)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EmployeePendingOrderUiState.Loading
    )

    init {
        getPendingOrders()
    }

    fun getPendingOrders() {
        viewModelScope.launch {
            if (!_isRefreshing.value) {
                _isLoading.value = true
            }
            _errorMessage.value = null

            try {
                val token = authManager.getToken.first() ?: ""

                orderRepository.getOrders(
                    bearerToken = token,
                    status = "pending",
                    userId = "",
                    tableId = ""
                ).collect { result ->
                    _isLoading.value = false
                    result.onSuccess { orders ->
                        val filtered = orders.filter {
                            it.status == "pending" && it.paymentStatus == "unpaid"
                        }
                        _rawOrders.value = filtered
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

    fun rejectOrder(orderId: Int) {
        viewModelScope.launch {
            val token = authManager.getToken.first() ?: ""
            orderRepository.cancelOrder(token, orderId)
                .onSuccess { isSuccess ->
                    if (isSuccess) {
                        // Hapus langsung dari raw data (bukan dari uiState) tanpa reload
                        _rawOrders.value = _rawOrders.value.filter { it.id != orderId }
                        _uiEvent.send(UiEvent.ShowToast("Pesanan berhasil ditolak"))
                    }
                }
                .onFailure { exception ->
                    _uiEvent.send(UiEvent.ShowToast(exception.message ?: "Gagal menolak pesanan"))
                }
        }
    }

    fun confirmManualPaymentOrder(orderId: Int) {
        viewModelScope.launch {
            val token = authManager.getToken.first() ?: ""
            orderRepository.confirmManualPaymentOrder(token, orderId)
                .onSuccess { isSuccess ->
                    if (isSuccess) {
                        _rawOrders.value = _rawOrders.value.filter { it.id != orderId }
                        _uiEvent.send(UiEvent.ShowToast("Pembayaran berhasil dikonfirmasi. Pesanan siap dimasak!"))
                    }
                }
                .onFailure { exception ->
                    _uiEvent.send(UiEvent.ShowToast(exception.message ?: "Gagal konfirmasi pembayaran pesanan"))
                }
        }
    }

    fun refreshData() {
        _isRefreshing.value = true
        getPendingOrders()
    }

    fun showToast(message: String) {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ShowToast(message))
        }
    }
}