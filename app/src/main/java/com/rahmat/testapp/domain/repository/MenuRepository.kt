package com.rahmat.testapp.domain.repository

import com.rahmat.testapp.domain.model.CartItem
import com.rahmat.testapp.domain.model.Menu
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MenuRepository {
    val menuCache: StateFlow<List<Menu>>
    fun getMenus(bearerToken: String, forceRefresh: Boolean = false): Flow<Result<List<Menu>>>
    fun getMenuById(id: Int): Menu?

    suspend fun toggleFavorite(bearerToken: String, menuId: Int, userId: Int): Result<Boolean>

    suspend fun storeRate(bearerToken: String, menuId: Int, userId: Int, value: Float): Result<Double>


    fun getCartItems(): Flow<List<CartItem>>
    suspend fun addToCart(menuId: Int, quantity: Int): Result<Boolean>
    suspend fun removeFromCart(menuId: Int)
    suspend fun updateCartQuantity(menuId: Int, newQuantity: Int)

    suspend fun createOrder(paymentMethod: String, totalPrice: Int, cartItems: List<CartItem>): Result<String?>
    suspend fun clearCart()
}