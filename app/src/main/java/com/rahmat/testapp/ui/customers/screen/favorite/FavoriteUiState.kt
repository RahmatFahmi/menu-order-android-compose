package com.rahmat.testapp.ui.customers.screen.favorite

import com.rahmat.testapp.domain.model.Menu

sealed class FavoriteUiState {
    object Idle : FavoriteUiState()
    object Loading : FavoriteUiState()

    data class Success(
        val favoriteMenus: List<Menu>
    ) : FavoriteUiState()

    data class Error(val message: String) : FavoriteUiState()
}