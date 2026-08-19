package com.rahmat.testapp.ui.customers.screen.cart

import CartUiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahmat.testapp.domain.repository.MenuRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: MenuRepository
) : ViewModel() {

    val uiState: StateFlow<CartUiState> = repository.getCartItems()
        .map { items ->
            if (items.isEmpty()) {
                CartUiState.Empty
            } else {
                val subTotal = items.sumOf { it.menu.price * it.quantity }.toInt()
                val total = items.sumOf { it.menu.finalPrice * it.quantity }.toInt()
                val discount = subTotal - total

                CartUiState.Success(
                    cartItems = items,
                    subTotal = subTotal,
                    discount = discount,
                    total = total
                )
            }
        }
        .onStart { emit(CartUiState.Loading) }
        .catch { e -> emit(CartUiState.Error(e.message ?: "Terjadi kesalahan")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CartUiState.Loading
        )

    fun updateQuantity(menuId: Int, currentQuantity: Int, delta: Int) {
        viewModelScope.launch {
            val nextQuantity = currentQuantity + delta
            if (nextQuantity > 0) {
                // Gunakan fungsi update yang baru (mengganti secara absolut)
                repository.updateCartQuantity(menuId, nextQuantity)
            }
        }
    }

    fun removeItem(menuId: Int) {
        viewModelScope.launch {
            repository.removeFromCart(menuId)
        }
    }

    fun checkoutOrder(
        paymentMethod: String,
        onResult: (url: String?, errorMessage: String?) -> Unit
    ) {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is CartUiState.Success) {
                // Jalankan fungsi createOrder yang berada di dalam repository
                val result = repository.createOrder(
                    paymentMethod = paymentMethod,
                    totalPrice = currentState.total,
                    cartItems = currentState.cartItems
                )

                result.onSuccess { snapUrl ->
                    if (paymentMethod.equals("NON_CASH", ignoreCase = true)) {
                        repository.clearCart()
                        onResult(snapUrl, null)
                    } else {
                        // Jika memilih CASH, hapus keranjang lokal saat itu juga
                        repository.clearCart()
                        onResult("", null)
                    }
                }.onFailure { exception ->
                    onResult(null, exception.localizedMessage ?: "Gagal membuat pesanan")
                }
            } else {
                onResult(null, "Keranjang belanja tidak valid atau kosong")
            }
        }
    }

    // Tambahkan juga fungsi ini untuk membersihkan keranjang setelah pembayaran non-cash sukses di WebView
    fun clearLocalCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }
}