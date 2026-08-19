package com.rahmat.testapp.data.remote.api

import com.rahmat.testapp.data.remote.dto.FavoriteResponse
import com.rahmat.testapp.data.remote.dto.MenuResponse
import com.rahmat.testapp.data.remote.dto.OrderRequest
import com.rahmat.testapp.data.remote.dto.OrderResponse
import com.rahmat.testapp.data.remote.dto.RatingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface MenuApiService {
    @GET("menu")
    suspend fun getMenus(
        @Header("Authorization") token: String
    ): Response<MenuResponse>

    @FormUrlEncoded
    @POST("menu/favorite")
    suspend fun toggleFavorite(
        @Header("Authorization") token: String,
        @Field("menu_item_id") menuId: Int,
        @Field("user_id") userId: Int
    ): Response<FavoriteResponse>

    @FormUrlEncoded
    @POST("menu/rate")
    suspend fun storeRate(
        @Header("Authorization") token: String,
        @Field("menu_item_id") menuId: Int,
        @Field("user_id") userId: Int,
        @Field("value") value: Float
    ): Response<RatingResponse>

    @POST("menu/checkout")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body request: OrderRequest
    ): Response<OrderResponse>
}