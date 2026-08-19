package com.rahmat.testapp.data.remote.dto

data class DeviceRequest(
    val fcm_token: String,
    val device_uuid: String,
    val platform: String = "android"
)