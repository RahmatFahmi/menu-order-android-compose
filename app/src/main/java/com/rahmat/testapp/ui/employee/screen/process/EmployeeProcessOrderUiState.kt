package com.rahmat.testapp.ui.employee.screen.process

import com.rahmat.testapp.domain.model.Order

sealed class EmployeeProcessOrderUiState {
    object Loading : EmployeeProcessOrderUiState()
    data class Success(val orders: List<Order>) : EmployeeProcessOrderUiState()
    data class Error(val message: String) : EmployeeProcessOrderUiState()
    object Empty : EmployeeProcessOrderUiState()
}