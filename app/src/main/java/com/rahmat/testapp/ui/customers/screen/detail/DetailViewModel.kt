package com.rahmat.testapp.ui.customers.screen.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahmat.testapp.data.local.AuthManager
import com.rahmat.testapp.domain.model.Menu
import com.rahmat.testapp.domain.repository.MenuRepository
import com.rahmat.testapp.ui.common.UiEvent
import com.rahmat.testapp.ui.customers.screen.product.ProductUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: MenuRepository,
    savedStateHandle: SavedStateHandle,
    private val authManager: AuthManager
) : ViewModel() {
    private val _menu = MutableStateFlow<Menu?>(null)
    val menu = _menu.asStateFlow()

    private val _uiState = MutableStateFlow<RecommendationUiState>(RecommendationUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        val id = savedStateHandle.get<Int>("menuId") ?: 0
        val foundMenu = repository.getMenuById(id)
        _menu.value = foundMenu

        foundMenu?.let { getMenuRecommendation(it) }
    }

    fun getMenuRecommendation(currentMenu: Menu){
        viewModelScope.launch {
            val token = authManager.getToken.first() ?: ""

            repository.getMenus(token, false).collect { result ->
                result.onSuccess { allMenus ->
                     when{
                         allMenus.isNotEmpty() -> {
                             val filtered = when {
                                 currentMenu.categoryName.lowercase().let {
                                     it.contains("makanan") || it.contains("berat") || it.contains("ringan") || it.contains("food")
                                 } -> {
                                     allMenus.filter { menu ->
                                         val cat = menu.categoryName.lowercase()
                                         !cat.contains("makanan") && !cat.contains("berat") && !cat.contains("ringan") && !cat.contains("food")
                                     }
                                 }

                                 currentMenu.categoryName.lowercase().let {
                                     it.contains("minuman") || it.contains("coffee") || it.contains("juice") || it.contains("drink")
                                 } -> {
                                     allMenus.filter { menu ->
                                         val cat = menu.categoryName.lowercase()
                                         !cat.contains("minuman") && !cat.contains("coffee") && !cat.contains("juice") && !cat.contains("drink")
                                     }
                                 }

                                 else -> allMenus.filter { it.id != currentMenu.id }
                             }
                             val finalRecommendation = filtered.shuffled().take(5)

                             _uiState.value = RecommendationUiState.Success(
                                 latestMenus = finalRecommendation
                             )
                         }
                         else -> {
                             _uiState.value = RecommendationUiState.Error("Belum ada menu tersedia")
                         }
                     }
                }
                result.onFailure { error ->
                    if (_uiState.value !is RecommendationUiState.Success) {
                        _uiState.value = RecommendationUiState.Error(error.message ?: "Terjadi kesalahan")
                    }
                }
            }
        }

    }

    fun toggleFavorite() {
        val currentMenu = _menu.value ?: return
        viewModelScope.launch {
            val token = authManager.getToken.first() ?: ""
            val userId = authManager.getId.first()?.toIntOrNull() ?: 0

            val result = repository.toggleFavorite(token, currentMenu.id, userId)

            result.onSuccess { isFavoriteNow ->
                _menu.value = currentMenu.copy(isFavorite = isFavoriteNow)
                val message = if (isFavoriteNow) "Ditambahkan ke favorit ❤️" else "Dihapus dari favorit"
                _uiEvent.send(UiEvent.ShowToast(message))
            }
            result.onFailure {
                _uiEvent.send(UiEvent.ShowToast("Gagal memperbarui favorit"))
            }
        }
    }

    fun storeRate(value: Float){
        val currentMenu = _menu.value ?: return
        viewModelScope.launch {
            val token = authManager.getToken.first() ?: ""
            val userId = authManager.getId.first()?.toIntOrNull() ?: 0

            val result = repository.storeRate(token, currentMenu.id, userId, value)
            result.onSuccess { newAvg ->
                // Update State Detail agar UI langsung berubah
                _menu.value = _menu.value?.copy(
                    averageRating = newAvg
                )
                _uiEvent.send(UiEvent.ShowToast("Terima kasih atas ratingnya!"))
            }

            result.onFailure {
                _uiEvent.send(UiEvent.ShowToast("Gagal mengirim rating"))
            }
        }
    }

    fun addMenuToCart(menuId: Int, quantity: Int) {
        viewModelScope.launch {
            val result = repository.addToCart(menuId, quantity)

            result.onSuccess {
                _uiEvent.send(UiEvent.ShowToast("Berhasil ditambah ke keranjang"))
            }.onFailure { exception ->
                _uiEvent.send(UiEvent.ShowToast(exception.message.toString()))
            }
        }
    }
}