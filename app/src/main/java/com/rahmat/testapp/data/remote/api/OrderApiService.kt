package com.rahmat.testapp.data.remote.api

import com.rahmat.testapp.data.remote.dto.BaseResponse
import com.rahmat.testapp.data.remote.dto.DataOrderDto
import com.rahmat.testapp.data.remote.dto.OrderListResponseDto // Pastikan di-import
import com.rahmat.testapp.data.remote.dto.RepayResponseDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderApiService {
    @GET("api/orders")
    suspend fun getOrders(
        @Header("Authorization") token: String,
        @Query("status") status: String?,
        @Query("user_id") userId: String?,
        @Query("table_id") tableId: String?
    ): BaseResponse<List<DataOrderDto>> // PERBAIKAN: Gunakan DTO pembungkus list, bukan unit single item

    @POST("orders/{id}/cancel")
    suspend fun cancelOrder(
        @Header("Authorization") token: String,
        @Path("id") orderId: Int
    ): BaseResponse<Any>

    @POST("orders/{id}/repay")
    suspend fun repayOrder(
        @Header("Authorization") token: String,
        @Path("id") orderId: Int
    ): BaseResponse<RepayResponseDto>

    @POST("orders/{id}/confirm-manual-payment")
    suspend fun confirmPaymentOrder(
        @Header("Authorization") token: String,
        @Path("id") orderId: Int
    ): BaseResponse<Any>

    @POST("orders/{id}/process")
    suspend fun processOrder(
        @Header("Authorization") token: String,
        @Path("id") orderId: Int
    ): BaseResponse<Any>

    @POST("orders/{id}/finish")
    suspend fun finishOrder(
        @Header("Authorization") token: String,
        @Path("id") orderId: Int
    ): BaseResponse<Any>
}