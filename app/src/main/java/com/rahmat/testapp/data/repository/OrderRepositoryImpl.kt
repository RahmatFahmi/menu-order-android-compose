package com.rahmat.testapp.data.repository

import com.rahmat.testapp.data.mapper.toDomain
import com.rahmat.testapp.data.remote.api.OrderApiService
import com.rahmat.testapp.domain.model.Order
import com.rahmat.testapp.domain.repository.OrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.map

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val apiService: OrderApiService,
) : OrderRepository {


    override fun getOrders(
        bearerToken: String,
        status: String?,
        userId: String?,
        tableId: String?
    ): Flow<Result<List<Order>>> = flow {
        try {
            val response = apiService.getOrders(
                token = "Bearer $bearerToken",
                status = status,
                userId = userId,
                tableId = tableId
            )

            if (response.success) {
                val domainOrders = response.data?.map { dataOrderDto -> dataOrderDto.toDomain() } ?: emptyList()

                emit(Result.success(domainOrders))
            } else {
                emit(Result.failure(Exception("Gagal mengambil data pesanan")))
            }
        } catch (e: Exception) {
            val userFriendlyMessage = when (e) {
                is java.net.ConnectException -> "Gagal terhubung ke server."
                is java.net.UnknownHostException -> "Periksa koneksi internetmu."
                else -> e.localizedMessage ?: "Terjadi kesalahan sistem."
            }
            emit(Result.failure(Exception(userFriendlyMessage)))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun cancelOrder(bearerToken: String, orderId: Int): Result<Boolean> {
        return try {
            val response = apiService.cancelOrder(token = "Bearer $bearerToken", orderId)

            if (response.success) {
                // Cukup kembalikan true, abaikan response.data dari Laravel
                Result.success(true)
            } else {
                Result.failure(Exception(response.message ?: "Gagal membatalkan pesanan"))
            }
        } catch (e: Exception) {
            val userFriendlyMessage = when (e) {
                is java.net.ConnectException -> "Gagal terhubung ke server."
                is java.net.UnknownHostException -> "Periksa koneksi internetmu."
                else -> e.localizedMessage ?: "Terjadi kesalahan sistem."
            }
            Result.failure(Exception(userFriendlyMessage))
        }
    }

    override suspend fun processOrder(
        bearerToken: String,
        orderId: Int
    ): Result<Boolean> {
        return try {
            val response = apiService.processOrder(token = "Bearer $bearerToken", orderId)

            if (response.success) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message ?: "Gagal Proses pesanan"))
            }
        } catch (e: Exception) {
            val userFriendlyMessage = when (e) {
                is java.net.ConnectException -> "Gagal terhubung ke server."
                is java.net.UnknownHostException -> "Periksa koneksi internetmu."
                else -> e.localizedMessage ?: "Terjadi kesalahan sistem."
            }
            Result.failure(Exception(userFriendlyMessage))
        }
    }

    override suspend fun finishOrder(
        bearerToken: String,
        orderId: Int
    ): Result<Boolean> {
        return try {
            val response = apiService.finishOrder(token = "Bearer $bearerToken", orderId)

            if (response.success) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message ?: "Gagal Menyelesaikan pesanan"))
            }
        } catch (e: Exception) {
            val userFriendlyMessage = when (e) {
                is java.net.ConnectException -> "Gagal terhubung ke server."
                is java.net.UnknownHostException -> "Periksa koneksi internetmu."
                else -> e.localizedMessage ?: "Terjadi kesalahan sistem."
            }
            Result.failure(Exception(userFriendlyMessage))
        }
    }

    override suspend fun repayOrder(
        bearerToken: String,
        orderId: Int
    ): Result<String> {
        return try {
            val response = apiService.repayOrder(token = "Bearer $bearerToken", orderId)
            if (response.success && response.data != null) {
                Result.success(response.data.snapUrl) // Mengembalikan snapUrl string
            } else {
                Result.failure(Exception(response.message ?: "Gagal memuat link pembayaran"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Terjadi kesalahan jaringan"))
        }
    }

    override suspend fun confirmManualPaymentOrder(
        bearerToken: String,
        orderId: Int
    ): Result<Boolean> {
        return try {
            val response = apiService.confirmPaymentOrder(token = "Bearer $bearerToken", orderId)

            if (response.success) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message ?: "Gagal membatalkan pesanan"))
            }
        } catch (e: Exception) {
            val userFriendlyMessage = when (e) {
                is java.net.ConnectException -> "Gagal terhubung ke server."
                is java.net.UnknownHostException -> "Periksa koneksi internetmu."
                else -> e.localizedMessage ?: "Terjadi kesalahan sistem."
            }
            Result.failure(Exception(userFriendlyMessage))
        }
    }



}