package com.rahmat.testapp.data.remote.dto

data class FavoriteResponse(
    val success: Boolean,
    val is_favorite: Boolean,
    val message: String?
)