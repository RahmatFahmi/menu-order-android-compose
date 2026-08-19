package com.rahmat.testapp.data.mapper

import com.rahmat.testapp.data.remote.dto.MenuItemDto
import com.rahmat.testapp.domain.model.Menu
import kotlin.math.roundToInt

fun MenuItemDto.toDomain(currentUserId: String?): Menu {
    val basePrice = this.price.toDoubleOrNull() ?: 0.0
    val activeDiscount = this.discounts?.firstOrNull { it.status == "active" }
    val discAmount = activeDiscount?.amount?.toDoubleOrNull() ?: 0.0
    val discAmountLabel = activeDiscount?.amount?.toIntOrNull() ?: 0

    val discountPercent = if (basePrice > 0 && discAmount > 0) {
        (discAmount * basePrice) / 100
    } else {
        0.0
    }

    val userIdInt = currentUserId?.toIntOrNull()
    val isFav = this.favorites?.any { it.user_id == userIdInt } ?: false

    val finalPrice = basePrice - discountPercent


    return Menu(
        id = this.id,
        categoryId = this.category_id,
        categoryName = this.category?.name ?: "coffee",
        name = this.name,
        description = this.description ?: "",
        price = basePrice,
        finalPrice = finalPrice,
        discountName = activeDiscount?.name,
        discountLabel = if (discAmountLabel > 0) "$discAmountLabel%" else null,
        isAvailable = this.is_available == 1,
        imageUrl = this.image,
        preparationTime = this.preparation_time?.let { "$it Min" } ?: "15 Min",
        averageRating = (Math.round((this.ratings_avg_value ?: 0.0) * 10.0) / 10.0),
        totalRatings = this.ratings_count ?: 0,
        isFavorite = isFav
    )
}