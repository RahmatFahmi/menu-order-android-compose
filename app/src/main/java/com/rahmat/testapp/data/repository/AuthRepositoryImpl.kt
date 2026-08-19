package com.rahmat.testapp.data.repository

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.rahmat.testapp.data.mapper.toDomain
import com.rahmat.testapp.data.remote.api.AuthApiService
import com.rahmat.testapp.data.remote.dto.LoginRequest
import com.rahmat.testapp.data.remote.dto.ScanQRRequest
import com.rahmat.testapp.domain.model.User
import com.rahmat.testapp.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor (
    private val apiService: AuthApiService,
    @ApplicationContext private val context: Context
) : AuthRepository {

    override suspend fun login(loginId: String, pass: String): Result<User> {
        return withContext(Dispatchers.IO){
            try {
                val response = apiService.login(LoginRequest(loginId, pass))

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success && body.data != null) {
                        Result.success(body.toDomain())
                    } else {
                        Result.failure(Exception(body?.message ?: "Login Gagal"))
                    }
                } else {
                    val errorJsonString = response.errorBody()?.string()
                    val errorMessage = try {
                        val jsonObject = org.json.JSONObject(errorJsonString)
                        jsonObject.getString("message")
                    } catch (e: Exception) {
                        "Kesalahan Input (${response.message()})"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                val userFriendlyMessage = when (e) {
                    is java.net.ConnectException -> "Gagal konek ke server. "
                    is java.net.SocketTimeoutException -> "Koneksi lemot, server tidak merespon."
                    is java.net.UnknownHostException -> "Tidak ada koneksi internet."
                    else -> "Terjadi kesalahan sistem: ${e.localizedMessage}"
                }
                Result.failure(Exception(userFriendlyMessage))
            }
        }

    }

    override suspend fun loginGuest(
        tableCode: String,
        fcmToken: String
    ): Result<User> {

        return withContext(Dispatchers.IO){
            try {
                val deviceUuid = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                val response = apiService.connectTable(
                    ScanQRRequest(tableCode, deviceUuid, fcmToken)
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success && body.data != null) {
                        Result.success(body.toDomain())
                    } else {
                        Result.failure(Exception(body?.message ?: "Scan Qr Gagal"))
                    }
                } else {
                    val errorJsonString = response.errorBody()?.string()
                    val errorMessage = try {
                        val jsonObject = org.json.JSONObject(errorJsonString)
                        jsonObject.getString("message")
                    } catch (e: Exception) {
                        "Kesalahan Input (${response.message()})"
                    }
                    Result.failure(Exception(errorMessage))
                }

            }catch (e: Exception) {
                val userFriendlyMessage = when (e) {
                    is java.net.ConnectException -> "Gagal konek ke server."
                    is java.net.SocketTimeoutException -> "Koneksi lemot, server tidak merespon."
                    is java.net.UnknownHostException -> "Tidak ada koneksi internet."
                    else -> "Terjadi kesalahan sistem: ${e.localizedMessage}"
                }
                Result.failure(Exception(userFriendlyMessage))
            }
        }

    }


}