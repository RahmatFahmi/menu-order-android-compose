package com.rahmat.testapp.ui.employee.screen.process

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
class EmployeeProcessOrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<EmployeeProcessOrderUiState>(EmployeeProcessOrderUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        getProcessOrders()
    }

    fun getProcessOrders() {
        viewModelScope.launch {
            if (!_isRefreshing.value) {
                _uiState.value = EmployeeProcessOrderUiState.Loading
            }
            try {
                val token = authManager.getToken.first() ?: ""

                // Memanggil repositori untuk mengambil pesanan berstatus 'processing' / 'proses'
                orderRepository.getOrders(
                    bearerToken = token,
                    status = "proses",
                    userId = "",
                    tableId = ""
                ).collect { result ->
                    result.onSuccess { orders ->
                        // Memastikan filter menyaring status pesanan yang sedang diproses
                        val filtered = orders.filter {
                            it.status.equals("proses", ignoreCase = true)
                        }

                        _uiState.value = if (filtered.isEmpty()) {
                            EmployeeProcessOrderUiState.Empty
                        } else {
                            EmployeeProcessOrderUiState.Success(filtered)
                        }
                    }.onFailure { exception ->
                        _uiState.value = EmployeeProcessOrderUiState.Error(
                            exception.localizedMessage ?: "Gagal memuat pesanan"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = EmployeeProcessOrderUiState.Error(e.localizedMessage ?: "Terjadi kesalahan")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun finishOrder(orderId: Int) {
        viewModelScope.launch {
            val token = authManager.getToken.first() ?: ""
            orderRepository.finishOrder(token, orderId)
                .onSuccess {
                    val currentState = _uiState.value
                    if (currentState is EmployeeProcessOrderUiState.Success) {
                        val updatedList = currentState.orders.filter { it.id != orderId }
                        _uiState.value = if (updatedList.isEmpty()) {
                            EmployeeProcessOrderUiState.Empty
                        } else {
                            EmployeeProcessOrderUiState.Success(updatedList)
                        }
                    }
                    _uiEvent.send(UiEvent.ShowToast("Pesanan berhasil diselesaikan!"))
                }
                .onFailure { exception ->
                    _uiEvent.send(UiEvent.ShowToast(exception.message ?: "Gagal menyelesaikan pesanan"))
                }
        }
    }

    fun refreshData() {
        _isRefreshing.value = true
        getProcessOrders()
    }
}