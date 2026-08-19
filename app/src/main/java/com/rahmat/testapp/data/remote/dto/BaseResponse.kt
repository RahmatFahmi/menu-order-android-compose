package com.rahmat.testapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    // Data bisa berisi objek, list, atau null jika tidak ada data yang dikembalikan
    @SerializedName("data")
    val data: T? = null
)