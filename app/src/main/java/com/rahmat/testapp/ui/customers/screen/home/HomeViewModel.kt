package com.rahmat.testapp.ui.customers.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahmat.testapp.data.local.AuthManager
import com.rahmat.testapp.domain.model.Menu
import com.rahmat.testapp.domain.repository.MenuRepository
import com.rahmat.testapp.ui.common.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MenuRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // ← langsung dari cache repository, tidak perlu _allMenus lagi
    val filteredMenus = combine(repository.menuCache, _searchQuery) { menus, query ->
        if (query.isBlank()) menus.take(4)
        else menus.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    init {
        getHomeData(force = false)
    }

    fun getHomeData(force: Boolean = false) {
        if (_uiState.value !is HomeUiState.Success) {
            _uiState.value = HomeUiState.Loading
        } else {
            _isRefreshing.value = true
        }

        viewModelScope.launch {
            val token = authManager.getToken.first() ?: ""

            repository.getMenus(token, force).collect { result ->
                result.onSuccess { allMenus ->
                    if (allMenus.isNotEmpty()) {
                        _uiState.value = HomeUiState.Success(
                            latestMenus = allMenus.take(4)
                        )
                    }
                }
                result.onFailure { error ->
                    if (_uiState.value !is HomeUiState.Success) {
                        _uiState.value = HomeUiState.Error(error.message ?: "Terjadi kesalahan")
                    }
                }
            }
            _isRefreshing.value = false
        }
    }

    fun addMenuToCart(menuId: Int, menuName: String) {
        viewModelScope.launch {
            val result = repository.addToCart(menuId, 1)

            result.onSuccess {
                _uiEvent.send(UiEvent.ShowToast("Berhasil menambahkan $menuName ke keranjang"))
            }.onFailure { exception ->
                _uiEvent.send(UiEvent.ShowToast(exception.message.toString()))
            }
        }
    }
}