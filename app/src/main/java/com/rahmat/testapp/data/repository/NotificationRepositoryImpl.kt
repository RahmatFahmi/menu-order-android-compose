package com.rahmat.testapp.data.repository

import android.content.Context
import com.rahmat.testapp.data.remote.api.AuthApiService
import com.rahmat.testapp.data.remote.dto.DeviceRequest
import com.rahmat.testapp.domain.repository.NotificationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

class NotificationRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    @ApplicationContext private val context: Context
) : NotificationRepository {

    override suspend fun registerDevice(fcmToken: String, bearerToken: String): Result<Unit> {
        return withContext(Dispatchers.IO + NonCancellable) {
            try {
                val uuid = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

                val response = apiService.registerDevice(
                    token = "Bearer $bearerToken",
                    request = DeviceRequest(fcm_token = fcmToken, device_uuid = uuid)
                )

                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Gagal: ${response.code()}"))
                }
            } catch (e: Exception) {
                // Jika koneksi putus atau timeout
                Result.failure(e)
            }
        }
    }
}