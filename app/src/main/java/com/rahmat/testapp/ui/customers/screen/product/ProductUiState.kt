package com.rahmat.testapp.ui.customers.screen.product

import com.rahmat.testapp.domain.model.Menu

sealed class ProductUiState {
    object Idle : ProductUiState()
    object Loading : ProductUiState()

    data class Success(
        val latestMenus: List<Menu>
    ) : ProductUiState()

    data class Error(val message: String) : ProductUiState()
}