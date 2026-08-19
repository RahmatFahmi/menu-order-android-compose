package com.rahmat.testapp.domain.model

data class Order(
    val id: Int,
    val userId: Int,
    val tableId: Int,
    val tableName: String?,
    val status: String,
    val paymentMethod: String?,
    val paymentStatus: String,
    val totalPrice: Int,
    val snapToken: String?,
    val createdAt: String,
    val updatedAt: String,
    val timeAgo: String,
    val items: List<OrderLineItem>
){
    val displayStatus: OrderDisplayStatus
        get() = when {
            status == "pending" && paymentStatus == "unpaid" -> OrderDisplayStatus.WaitingPayment
            status == "pending" && paymentStatus == "paid" -> OrderDisplayStatus.KitchenQueue
            status == "process" -> OrderDisplayStatus.Processing
            status == "finish" -> OrderDisplayStatus.Finished
            else -> OrderDisplayStatus.Unknown
        }
}

data class OrderLineItem(
    val id: Int,
    val menuItemId: Int,
    val menuName: String,
    val menuItemImage: String?,
    val harga: Int,
    val jumlah: Int,
    val potongan: Int,
    val total: Int
)

enum class OrderDisplayStatus {
    WaitingPayment,
    KitchenQueue,
    Processing,
    Finished,
    Unknown
}