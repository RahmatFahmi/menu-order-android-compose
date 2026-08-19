package com.rahmat.testapp.ui.employee.screen.queue

import com.rahmat.testapp.domain.model.Order

sealed class EmployeeQueueOrderUiState {
    object Loading : EmployeeQueueOrderUiState()
    data class Success(val orders: List<Order>) : EmployeeQueueOrderUiState()
    object Empty : EmployeeQueueOrderUiState()
    data class Error(val message: String) : EmployeeQueueOrderUiState()
}