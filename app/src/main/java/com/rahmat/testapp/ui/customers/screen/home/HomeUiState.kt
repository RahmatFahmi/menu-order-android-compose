package com.rahmat.testapp.ui.customers.screen.home

import com.rahmat.testapp.domain.model.Menu

sealed class HomeUiState {
    object Idle : HomeUiState()
    object Loading : HomeUiState()

    data class Success(
        val latestMenus: List<Menu>
    ) : HomeUiState()

    data class Error(val message: String) : HomeUiState()
}