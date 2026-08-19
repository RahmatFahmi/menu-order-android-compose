package com.rahmat.testapp.data.remote.dto

data class ScanQRRequest(
    val table_code: String,
    val device_uuid: String,
    val fcm_token: String
)