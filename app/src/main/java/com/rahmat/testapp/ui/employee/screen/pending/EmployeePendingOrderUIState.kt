package com.rahmat.testapp.ui.employee.screen.pending

import com.rahmat.testapp.domain.model.Order

sealed class EmployeePendingOrderUiState {
    object Loading : EmployeePendingOrderUiState()
    data class Success(val orders: List<Order>) : EmployeePendingOrderUiState()
    data class Error(val message: String) : EmployeePendingOrderUiState()
    object Empty : EmployeePendingOrderUiState()
}