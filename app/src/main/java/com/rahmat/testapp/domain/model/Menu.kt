package com.rahmat.testapp.domain.model

data class Menu(
    val id: Int,
    val categoryId: Int,
    val categoryName: String,
    val name: String,
    val description: String,
    val price: Double,
    val finalPrice: Double,
    val discountName: String?,
    val discountLabel: String?,
    val isAvailable: Boolean,
    val imageUrl: String?,
    val preparationTime: String?,
    val averageRating: Double,
    val totalRatings: Int,
    val isFavorite: Boolean = false
)