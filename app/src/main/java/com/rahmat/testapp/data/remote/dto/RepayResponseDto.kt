package com.rahmat.testapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RepayResponseDto (
    @SerializedName("snap_url")
    val snapUrl: String
)