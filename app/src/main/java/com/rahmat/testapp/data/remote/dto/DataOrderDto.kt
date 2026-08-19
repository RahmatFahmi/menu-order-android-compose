package com.rahmat.testapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OrderListResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<DataOrderDto>
)

data class DataOrderDto(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("table_id") val tableId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("payment_method") val paymentMethod: String?,
    @SerializedName("payment_status") val paymentStatus: String,
    @SerializedName("total_price") val totalPrice: Int,
    @SerializedName("snap_token") val snapToken: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("time_ago") val timeAgo: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("menu_items") val menuItems: List<OrderLineItemDto>?,
    @SerializedName("table") val table: TableDto?
)

data class TableDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class OrderLineItemDto(
    @SerializedName("id") val id: Int, // Ini mengambil menu_items.id (yaitu angka 2)

    // Bungkus data transaksional yang berada di dalam objek "pivot"
    @SerializedName("pivot") val pivot: PivotDto?
)

data class PivotDto(
    @SerializedName("menu_item_id") val menuItemId: Int,
    @SerializedName("harga") val harga: Int,
    @SerializedName("jumlah") val jumlah: Int,
    @SerializedName("potongan") val potongan: Int?,
    @SerializedName("total") val total: Int,
    @SerializedName("nama") val nama: String?,
    @SerializedName("image") val image: String?
)