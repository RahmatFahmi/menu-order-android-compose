package com.rahmat.testapp.ui.customers.screen.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahmat.testapp.data.local.AuthManager
import com.rahmat.testapp.domain.repository.MenuRepository
import com.rahmat.testapp.ui.customers.screen.home.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val repository: MenuRepository,
    private val authManager: AuthManager
): ViewModel() {
    private val _uiState = MutableStateFlow<FavoriteUiState>(FavoriteUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init{
        loadFavorite(force = false)
    }

    fun loadFavorite(force: Boolean = false) {
        if (_uiState.value !is FavoriteUiState.Success) {
            _uiState.value = FavoriteUiState.Loading
        } else {
            _isRefreshing.value = true
        }

        viewModelScope.launch {
            val token = authManager.getToken.first() ?: ""

            repository.getMenus(token, force).collect { result ->
                result.onSuccess { allMenus ->
                    if (allMenus.isEmpty()) {
                        _uiState.value = FavoriteUiState.Error("Belum ada menu favorit")
                    } else {
                        _uiState.value = FavoriteUiState.Success(
                            favoriteMenus = allMenus.filter {it.isFavorite}
                        )
                    }
                }
                result.onFailure { error ->
                    if (_uiState.value !is FavoriteUiState.Success) {
                        _uiState.value = FavoriteUiState.Error(error.message ?: "Terjadi kesalahan")
                    }
                }
            }
            _isRefreshing.value = false
        }
    }
}