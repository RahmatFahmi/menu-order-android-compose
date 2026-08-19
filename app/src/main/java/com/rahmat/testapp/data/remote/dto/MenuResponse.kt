package com.rahmat.testapp.data.remote.dto

data class MenuResponse(
    val success: Boolean,
    val message: String?,
    val data: List<MenuItemDto>
)

data class MenuItemDto(
    val id: Int,
    val category_id: Int,
    val category: CategoryDto?,
    val name: String,
    val description: String?,
    val price: String,
    val is_available: Int,
    val image: String?,
    val preparation_time: String?,
    val discounts: List<DiscountDto>?,
    val favorites: List<FavoriteDto>?,
    val ratings_avg_value: Double?,
    val ratings_count: Int?
)

data class FavoriteDto(
    val id: Int,
    val menu_item_id: Int,
    val user_id: Int
)

data class RatingDto(
    val id: Int,
    val user_id: Int,
    val value: String
)

data class CategoryDto(
    val id: Int,
    val name: String
)

data class DiscountDto(
    val name: String,
    val amount: String,
    val status: String
)