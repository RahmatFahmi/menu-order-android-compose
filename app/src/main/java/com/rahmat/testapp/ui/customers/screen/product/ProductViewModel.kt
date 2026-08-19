package com.rahmat.testapp.ui.customers.screen.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahmat.testapp.data.local.AuthManager
import com.rahmat.testapp.domain.model.Menu
import com.rahmat.testapp.domain.repository.MenuRepository
import com.rahmat.testapp.ui.common.UiEvent
import com.rahmat.testapp.ui.customers.screen.home.HomeUiState
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
class ProductViewModel @Inject constructor(
    private val repository: MenuRepository,
    private val authManager: AuthManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _categoryName = MutableStateFlow<String?>(null)

    init {
        _categoryName.value = savedStateHandle.get<String>("categoryName")
        getProduct()
    }

    // Filter dari cache repository langsung
    val filteredMenus = combine(repository.menuCache, _searchQuery, _categoryName) { menus, query, category ->
        val categoryFiltered = when (category?.lowercase()) {
            "semua" -> menus
            "diskon" -> menus.filter { it.discountName != null || it.finalPrice < it.price }
            "makanan" -> menus.filter { menu ->
                val cat = menu.categoryName.lowercase()
                cat.contains("makanan") || cat.contains("berat") || cat.contains("ringan")
            }
            "minuman" -> menus.filter { menu ->
                val cat = menu.categoryName.lowercase()
                cat.contains("coffee") || cat.contains("juice") || cat.contains("minuman") || cat.contains("drink")
            }
            else -> menus.filter { it.categoryName.equals(category, ignoreCase = true) }
        }

        if (query.isBlank()) categoryFiltered
        else categoryFiltered.filter {
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

    fun getProduct(force: Boolean = false) {
        if (_uiState.value !is ProductUiState.Success) {
            _uiState.value = ProductUiState.Loading
        } else {
            _isRefreshing.value = true
        }

        viewModelScope.launch {
            val token = authManager.getToken.first() ?: ""

            repository.getMenus(token, force).collect { result ->
                result.onSuccess { allMenus ->
                    if (allMenus.isEmpty()) {
                        _uiState.value = ProductUiState.Error("Belum ada menu tersedia")
                    } else {
                        _uiState.value = ProductUiState.Success(latestMenus = allMenus)
                    }
                }
                result.onFailure { error ->
                    if (_uiState.value !is ProductUiState.Success) {
                        _uiState.value = ProductUiState.Error(error.message ?: "Terjadi kesalahan")
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