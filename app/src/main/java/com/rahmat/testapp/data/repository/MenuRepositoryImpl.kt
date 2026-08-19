package com.rahmat.testapp.data.repository

import android.util.Log
import com.rahmat.testapp.data.local.AuthManager
import com.rahmat.testapp.data.local.dao.CartDao
import com.rahmat.testapp.data.local.entity.CartEntity
import com.rahmat.testapp.data.mapper.toDomain
import com.rahmat.testapp.data.remote.api.MenuApiService
import com.rahmat.testapp.data.remote.dto.OrderItemRequest
import com.rahmat.testapp.data.remote.dto.OrderRequest
import com.rahmat.testapp.domain.model.CartItem
import com.rahmat.testapp.domain.model.Menu
import com.rahmat.testapp.domain.repository.MenuRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuRepositoryImpl @Inject constructor(
    private val apiService: MenuApiService,
    private val authManager: AuthManager,
    private val cartDao: CartDao
) : MenuRepository {

    // 1. Menggunakan MutableStateFlow sebagai Single Source of Truth
    private val _menuCache = MutableStateFlow<List<Menu>>(emptyList())

    override fun getMenus(bearerToken: String, forceRefresh: Boolean): Flow<Result<List<Menu>>> = flow {
        // 2. Emit data cache yang ada sekarang (Instant UI)
        emit(Result.success(_menuCache.value))

        // 3. Ambil dari API jika dipaksa refresh atau cache kosong
        if (_menuCache.value.isEmpty() || forceRefresh) {
            try {
                val response = apiService.getMenus("Bearer $bearerToken")

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        val currentUserId = authManager.getId.first()
                        val domainData = body.data.map { it.toDomain(currentUserId) }

                        // 4. Update StateFlow (Otomatis trigger semua subscriber/UI)
                        _menuCache.value = domainData
                        emit(Result.success(domainData))
                    } else {
                        emit(Result.failure(Exception(body?.message ?: "Gagal memuat menu")))
                    }
                } else {
                    emit(Result.failure(Exception("Server Error (${response.code()})")))
                }
            } catch (e: Exception) {
                if (_menuCache.value.isEmpty()) {
                    val userFriendlyMessage = when (e) {
                        is java.net.ConnectException -> "Gagal terhubung ke server."
                        is java.net.UnknownHostException -> "Periksa koneksi internetmu."
                        else -> e.localizedMessage
                    }
                    emit(Result.failure(Exception(userFriendlyMessage)))
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun getMenuById(id: Int): Menu? {
        // Mengambil langsung dari nilai StateFlow terbaru
        return _menuCache.value.firstOrNull { it.id == id }
    }

    override suspend fun toggleFavorite(
        bearerToken: String,
        menuId: Int,
        userId: Int
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.toggleFavorite(
                    token = "Bearer $bearerToken",
                    menuId = menuId,
                    userId = userId
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val isFavoriteNow = response.body()?.is_favorite ?: false

                    // 5. UPDATE CACHE SECARA REAKTIF
                    _menuCache.value = _menuCache.value.map { menu ->
                        if (menu.id == menuId) menu.copy(isFavorite = isFavoriteNow) else menu
                    }

                    Result.success(isFavoriteNow)
                } else {
                    Result.failure(Exception("Gagal memperbarui favorit"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun storeRate(
        bearerToken: String,
        menuId: Int,
        userId: Int,
        value: Float
    ): Result<Double> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.storeRate(
                    token = "Bearer $bearerToken",
                    menuId = menuId,
                    userId = userId,
                    value = value
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val newAvg = response.body()?.data?.average_rating ?: value.toDouble()
                    val newTotal = response.body()?.data?.total_ratings ?: 0

                    // 6. UPDATE CACHE SECARA REAKTIF (KUNCI AGAR HOME IKUT UPDATE)
                    _menuCache.value = _menuCache.value.map { menu ->
                        if (menu.id == menuId) {
                            menu.copy(
                                averageRating = newAvg,
                                totalRatings = newTotal
                            )
                        } else {
                            menu
                        }
                    }

                    Result.success(newAvg)
                } else {
                    Result.failure(Exception("Gagal memperbarui rating"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override val menuCache: StateFlow<List<Menu>> = _menuCache.asStateFlow()

    override fun getCartItems(): Flow<List<CartItem>> {
        return cartDao.getAllCartItems().map { entities ->
            entities.mapNotNull { entity ->
                // Gabungkan ID dari Room dengan data lengkap dari menuCache
                val menuData = _menuCache.value.find { it.id == entity.menuId }
                if (menuData != null) {
                    CartItem(menu = menuData, quantity = entity.quantity)
                } else {
                    null
                }
            }
        }
    }

    override suspend fun addToCart(menuId: Int, quantity: Int): Result<Boolean> {
        return try {
            val existingItem = cartDao.getCartItemById(menuId)

            if (existingItem != null) {
                val newQuantity = existingItem.quantity + quantity
                cartDao.upsertCartItem(existingItem.copy(quantity = newQuantity))
                Result.failure(Exception("Menu ini sudah ada di keranjang"))
            } else {
                cartDao.upsertCartItem(CartEntity(menuId = menuId, quantity = quantity))
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCartQuantity(menuId: Int, newQuantity: Int) {
        val existingItem = cartDao.getCartItemById(menuId)
        existingItem?.let {
            cartDao.upsertCartItem(it.copy(quantity = newQuantity))
        }
    }

    override suspend fun removeFromCart(menuId: Int) {
        cartDao.deleteCartItem(CartEntity(menuId, 0))
    }

    // Tambahkan di dalam kelas MenuRepositoryImpl

    override suspend fun createOrder(
        paymentMethod: String,
        totalPrice: Int,
        cartItems: List<CartItem>
    ): Result<String?> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Ambil Token dan User ID secara asinkronus dari AuthManager
                val token = authManager.getToken.first() ?: return@withContext Result.failure(Exception("Sesi habis, silakan login ulang"))
                val tableId = authManager.getTableId.first() ?: return@withContext Result.failure(Exception("Silakan pilih meja terlebih dahulu"))
                val userId = authManager.getId.first() ?: return@withContext Result.failure(Exception("Sesi habis, silakan login ulang"))

                // 2. Petakan data domain CartItem menjadi OrderItemRequest format JSON Laravel
                val orderItems = cartItems.map {
                    OrderItemRequest(
                        menuId = it.menu.id,
                        quantity = it.quantity
                    )
                }

                val request = OrderRequest(
                    tableId = tableId.toInt(),
                    userId = userId.toInt(),
                    paymentMethod = paymentMethod,
                    totalPrice = totalPrice,
                    items = orderItems
                )

                // 4. Eksekusi request jaringan ke Laravel
                val response = apiService.createOrder("Bearer $token", request)

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        // Berhasil: Berikan string url Midtrans (atau null/kosong jika CASH)
                        Result.success(body.midtransSnapUrl)
                    } else {
                        Result.failure(Exception(body.message))
                    }
                } else {
                    Result.failure(Exception("Gagal menghubungi server (${response.code()})"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun clearCart() {
        withContext(Dispatchers.IO) {
            // Panggil fungsi DAO Room bawaan untuk menghapus semua item keranjang
            cartDao.clearCart() // Pastikan fungsi clearCart() / deleteAll() sudah didefinisikan di CartDao kamu
        }
    }
}