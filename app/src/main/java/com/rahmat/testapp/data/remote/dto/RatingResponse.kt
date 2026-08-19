package com.rahmat.testapp.data.remote.dto

data class RatingResponse(
    val success: Boolean,
    val message: String,
    val data: RatingData?
)

data class RatingData(
    val user_rating: String,
    val average_rating: Double,
    val total_ratings: Int
)