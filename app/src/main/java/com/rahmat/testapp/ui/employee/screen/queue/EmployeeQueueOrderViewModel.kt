package com.rahmat.testapp.ui.employee.screen.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahmat.testapp.data.local.AuthManager
import com.rahmat.testapp.domain.repository.OrderRepository
import com.rahmat.testapp.ui.common.UiEvent
import com.rahmat.testapp.ui.employee.screen.pending.EmployeePendingOrderUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class EmployeeQueueOrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authManager: AuthManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<EmployeeQueueOrderUiState>(EmployeeQueueOrderUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        getQueueOrders()
    }

    fun getQueueOrders() {
        viewModelScope.launch {
            if (!_isRefreshing.value) {
                _uiState.value = EmployeeQueueOrderUiState.Loading
            }
            try {
                val token = authManager.getToken.first() ?: ""

                orderRepository.getOrders(
                    bearerToken = token,
                    status = "pending",
                    userId = "",
                    tableId = ""
                ).collect { result ->
                    result.onSuccess { orders ->
                        val filtered = orders.filter {
                            it.status == "pending" && it.paymentStatus == "paid"
                        }
                        _uiState.value = if (filtered.isEmpty())
                            EmployeeQueueOrderUiState.Empty
                        else
                            EmployeeQueueOrderUiState.Success(filtered)
                    }.onFailure { exception ->
                        _uiState.value = EmployeeQueueOrderUiState.Error(
                            exception.localizedMessage ?: "Gagal memuat pesanan"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = EmployeeQueueOrderUiState.Error(e.localizedMessage ?: "Terjadi kesalahan")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun processOrder(orderId: Int) {
        viewModelScope.launch {
            val token = authManager.getToken.first() ?: ""
            orderRepository.processOrder(token, orderId)
                .onSuccess {
                    val currentState = _uiState.value
                    if (currentState is EmployeeQueueOrderUiState.Success) {
                        val updatedList = currentState.orders.filter { it.id != orderId }
                        _uiState.value = if (updatedList.isEmpty())
                            EmployeeQueueOrderUiState.Empty
                        else
                            EmployeeQueueOrderUiState.Success(updatedList)
                    }
                    _uiEvent.send(UiEvent.ShowToast("Pesanan Mulai diproses"))
                }
                .onFailure { exception ->
                    _uiEvent.send(UiEvent.ShowToast(exception.message ?: "Gagal memproses pesanan"))
                }
        }
    }

    fun refreshData() {
        _isRefreshing.value = true
        getQueueOrders()
    }
}