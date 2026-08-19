package com.rahmat.testapp.domain.repository

import com.rahmat.testapp.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getOrders(
        bearerToken: String,
        status: String? = null,
        userId: String? = null,
        tableId: String? = null
    ): Flow<Result<List<Order>>>

    suspend fun cancelOrder(bearerToken: String, orderId: Int): Result<Boolean>
    suspend fun processOrder(bearerToken: String, orderId: Int): Result<Boolean>
    suspend fun finishOrder(bearerToken: String, orderId: Int): Result<Boolean>
    suspend fun repayOrder(bearerToken: String, orderId: Int): Result<String>
    suspend fun confirmManualPaymentOrder(bearerToken: String, orderId: Int): Result<Boolean>

}