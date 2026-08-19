import com.rahmat.testapp.domain.model.CartItem

sealed class CartUiState {
    object Loading : CartUiState()
    object Empty : CartUiState()

    data class Success(
        val cartItems: List<CartItem>,
        val subTotal: Int,
        val discount: Int,
        val total: Int
    ) : CartUiState()

    data class Error(val message: String) : CartUiState()
}