package com.rahmat.testapp.ui.customers.screen.order.processing

import com.rahmat.testapp.domain.model.Order

sealed class ProcessOrderUiState {
    object Loading : ProcessOrderUiState()
    data class Success(val orders: List<Order>) : ProcessOrderUiState()
    data class Error(val message: String) : ProcessOrderUiState()
    object Empty : ProcessOrderUiState()
}