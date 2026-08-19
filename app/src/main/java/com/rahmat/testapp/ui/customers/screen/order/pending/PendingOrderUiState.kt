package com.rahmat.testapp.ui.customers.screen.order.pending

import com.rahmat.testapp.domain.model.Order

sealed class PendingOrderUiState {
    object Loading : PendingOrderUiState()
    data class Success(val orders: List<Order>) : PendingOrderUiState()
    data class Error(val message: String) : PendingOrderUiState()
    object Empty : PendingOrderUiState()
}