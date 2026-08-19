package com.rahmat.testapp.ui.customers.screen.order.processing

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
class ProcessOrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProcessOrderUiState>(ProcessOrderUiState.Loading)
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
                _uiState.value = ProcessOrderUiState.Loading
            }

            try {
                val token = authManager.getToken.first() ?: ""
                val userId = authManager.getId.first() ?: ""
                val tableId = authManager.getTableId.first() ?: ""

                // Memanggil repository khusus status pesanan diproses/dimasak
                orderRepository.getOrders(
                    bearerToken = token,
                    status = "proses", // Sesuaikan string status backend (misal: "proses" atau "processing")
                    userId = userId,
                    tableId = tableId
                ).collect { result ->
                    result.onSuccess { activeOrders ->
                        if (activeOrders.isEmpty()) {
                            _uiState.value = ProcessOrderUiState.Empty
                        } else {
                            _uiState.value = ProcessOrderUiState.Success(activeOrders)
                        }
                    }.onFailure { exception ->
                        _uiState.value = ProcessOrderUiState.Error(
                            exception.localizedMessage ?: "Gagal memuat data pesanan diproses"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ProcessOrderUiState.Error(e.localizedMessage ?: "Terjadi kesalahan")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refreshData() {
        _isRefreshing.value = true
        getProcessOrders()
    }
}