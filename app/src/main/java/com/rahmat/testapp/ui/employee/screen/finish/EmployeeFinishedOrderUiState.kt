package com.rahmat.testapp.ui.employee.screen.finish

import com.rahmat.testapp.domain.model.Order

sealed class EmployeeFinishedOrderUiState {
    object Loading : EmployeeFinishedOrderUiState()
    data class Success(val orders: List<Order>) : EmployeeFinishedOrderUiState()
    data class Error(val message: String) : EmployeeFinishedOrderUiState()
    object Empty : EmployeeFinishedOrderUiState()
}