package com.rahmat.testapp.ui.customers.screen.detail

import com.rahmat.testapp.domain.model.Menu

sealed class RecommendationUiState {
    object Idle : RecommendationUiState()
    object Loading : RecommendationUiState()

    data class Success(
        val latestMenus: List<Menu>
    ) : RecommendationUiState()

    data class Error(val message: String) : RecommendationUiState()
}