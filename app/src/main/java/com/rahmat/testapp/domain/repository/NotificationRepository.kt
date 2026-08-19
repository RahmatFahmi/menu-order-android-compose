package com.rahmat.testapp.domain.repository

interface NotificationRepository {
    suspend fun registerDevice(fcmToken: String, bearerToken: String): Result<Unit>
}