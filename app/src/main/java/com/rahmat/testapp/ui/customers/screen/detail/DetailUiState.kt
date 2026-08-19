package com.rahmat.testapp.ui.customers.screen.detail

import com.rahmat.testapp.domain.model.Menu
import com.rahmat.testapp.ui.customers.screen.home.HomeUiState

sealed class DetailUiState {
    object Idle : DetailUiState()
    object Loading : DetailUiState()

    data class Success(
        val detailMenus: Menu
    ) : DetailUiState()

    data class Error(val message: String) : DetailUiState()
}