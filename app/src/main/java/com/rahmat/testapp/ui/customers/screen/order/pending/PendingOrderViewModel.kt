package com.rahmat.testapp.ui.customers.screen.order.pending

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahmat.testapp.data.local.AuthManager
import com.rahmat.testapp.domain.repository.OrderRepository
import com.rahmat.testapp.ui.common.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PendingOrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<PendingOrderUiState>(PendingOrderUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        getPendingOrders()
    }

    fun getPendingOrders() {
        viewModelScope.launch {
            // Tampilkan loading jika bukan di-trigger dari pull-to-refresh
            if (!_isRefreshing.value) {
                _uiState.value = PendingOrderUiState.Loading
            }

            try {
                // Ambil data autentikasi dari AuthManager secara asinkronus
                val token = authManager.getToken.first() ?: ""
                val userId = authManager.getId.first() ?: ""
                val tableId = authManager.getTableId.first() ?: ""

                // Panggil repositori dengan filter wajib untuk status pending milik pelanggan
                orderRepository.getOrders(
                    bearerToken = token,
                    status = "pending",
                    userId = userId,
                    tableId = tableId
                ).collect { result ->
                    result.onSuccess { activeOrders ->
                        if (activeOrders.isEmpty()) {
                            _uiState.value = PendingOrderUiState.Empty
                        } else {
                            _uiState.value = PendingOrderUiState.Success(activeOrders)
                        }
                    }.onFailure { exception ->
                        _uiState.value = PendingOrderUiState.Error(
                            exception.localizedMessage ?: "Gagal memuat data pesanan"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PendingOrderUiState.Error(e.localizedMessage ?: "Terjadi kesalahan")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun cancelOrder(orderId: Int) {
        viewModelScope.launch {
            val token = authManager.getToken.first() ?: ""

            orderRepository.cancelOrder(token, orderId)
                .onSuccess { isSuccess ->
                    if (isSuccess) {
                        val currentState = _uiState.value
                        if (currentState is PendingOrderUiState.Success) {
                            val updatedList = currentState.orders.filter { it.id != orderId }
                            _uiState.value = if (updatedList.isEmpty()) {
                                PendingOrderUiState.Empty
                            } else {
                                PendingOrderUiState.Success(updatedList)
                            }
                        }
                        _uiEvent.send(UiEvent.ShowToast("Pesanan berhasil dibatalkan"))
                    }
                }
                .onFailure { exception ->
                    Log.d("Gagal Hapus Order", "cancelOrder: ${exception.message}")
                    _uiEvent.send(UiEvent.ShowToast(exception.message ?: "Gagal membatalkan pesanan"))
                }
        }
    }

    fun repayOrder(
        orderId: Int,
        onResult: (snapUrl: String?, errorMessage: String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val token = authManager.getToken.first() ?: ""

                // Panggil repositori untuk mengambil snapUrl asli pesanan ini
                orderRepository.repayOrder(token, orderId)
                    .onSuccess { snapUrl ->
                        onResult(snapUrl, null)
                    }
                    .onFailure { exception ->
                        Log.d("Gagal Bayar Order", "bayarOrder: ${exception.message}")
                        onResult(null, exception.localizedMessage ?: "Gagal memproses pembayaran ulang")
                    }
            } catch (e: Exception) {
                onResult(null, e.localizedMessage ?: "Terjadi kesalahan sistem")
            }
        }
    }

    fun refreshData() {
        _isRefreshing.value = true
        getPendingOrders()
    }
}