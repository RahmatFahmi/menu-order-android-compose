package com.rahmat.testapp.data.mapper

import com.rahmat.testapp.data.remote.dto.DataOrderDto
import com.rahmat.testapp.data.remote.dto.OrderLineItemDto
import com.rahmat.testapp.domain.model.Order
import com.rahmat.testapp.domain.model.OrderLineItem

fun DataOrderDto.toDomain(): Order {
    return Order(
        id = this.id,
        userId = this.userId,
        tableId = this.tableId,
        tableName = this.table?.name,
        status = this.status,
        paymentMethod = this.paymentMethod,
        paymentStatus = this.paymentStatus,
        totalPrice = this.totalPrice,
        snapToken = this.snapToken,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        timeAgo = this.timeAgo,
        items = this.menuItems?.map { it.toDomain() } ?: emptyList()
    )
}

fun OrderLineItemDto.toDomain(): OrderLineItem {
    return OrderLineItem(
        id = this.id, // ID Menu
        menuItemId = this.pivot?.menuItemId ?: 0,
        menuName = this.pivot?.nama ?: "Unknown Menu",
        menuItemImage = this.pivot?.image,
        harga = this.pivot?.harga ?: 0,
        jumlah = this.pivot?.jumlah ?: 0,
        potongan = this.pivot?.potongan ?: 0,
        total = this.pivot?.total ?: 0
    )
}