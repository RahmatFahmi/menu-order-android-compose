package com.rahmat.testapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OrderRequest(
    @SerializedName("table_id")
    val tableId: Int,

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("payment_method")
    val paymentMethod: String,

    @SerializedName("total_price")
    val totalPrice: Int,

    @SerializedName("items")
    val items: List<OrderItemRequest>
)

data class OrderItemRequest(
    @SerializedName("menu_item_id")
    val menuId: Int,

    @SerializedName("quantity")
    val quantity: Int
)

data class OrderResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("midtransSnapUrl")
    val midtransSnapUrl: String?
)