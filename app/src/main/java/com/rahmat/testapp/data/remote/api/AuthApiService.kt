package com.rahmat.testapp.data.remote.api

import com.rahmat.testapp.data.remote.dto.DeviceRequest
import com.rahmat.testapp.data.remote.dto.LoginRequest
import com.rahmat.testapp.data.remote.dto.LoginResponse
import com.rahmat.testapp.data.remote.dto.ScanQRRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("register-device")
    suspend fun registerDevice(
        @Header("Authorization") token: String,
        @Body request: DeviceRequest
    ): Response<Unit>

    @POST("connect-table")
    suspend fun connectTable(
        @Body request: ScanQRRequest
    ): Response<LoginResponse>
}