package com.rahmat.testapp.ui.customers.screen.order.finished

import com.rahmat.testapp.domain.model.Order

sealed class FinishedOrderUiState {
    object Loading : FinishedOrderUiState()
    data class Success(val orders: List<Order>) : FinishedOrderUiState()
    data class Error(val message: String) : FinishedOrderUiState()
    object Empty : FinishedOrderUiState()
}