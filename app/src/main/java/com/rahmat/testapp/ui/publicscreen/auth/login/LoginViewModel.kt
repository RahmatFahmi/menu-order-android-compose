package com.rahmat.testapp.ui.publicscreen.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.rahmat.testapp.data.local.AuthManager
import com.rahmat.testapp.domain.repository.AuthRepository
import com.rahmat.testapp.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

@HiltViewModel
class LoginViewModel @Inject constructor (
    private val repository: AuthRepository,
    private val notificationRepository: NotificationRepository,
    private val authManager: AuthManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun doLogin(loginId: String, pass: String){
        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            repository.login(loginId, pass)
                .onSuccess { user ->
                    authManager.saveToken(user.token, user.role, user.id.toString())
                    registerNotificationDevice(user.token)
                    _uiState.value = LoginUiState.Success(user)
                }
                .onFailure {
                        error -> _uiState.value = LoginUiState.Error(error.message?: "Login Gagal")
                }
        }
    }

    fun connectTable(tableCode: String){
        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            try {
                val fcmToken = FirebaseMessaging.getInstance().token.await()
                val result = repository.loginGuest(tableCode, fcmToken)

                result.onSuccess { user ->
                    authManager.saveToken(user.token, user.role, user.id.toString())
                    authManager.saveTabelCode(user.tabelCode.toString(), user.tabelId.toString())
                    _uiState.value = LoginUiState.Success(user)
                    registerNotificationDevice(user.token)
                }.onFailure { e ->
                    _uiState.value = LoginUiState.Error(e.message)
                }

            }catch (e: Exception) {
                Log.e("FCM", "Gagal: ${e.message}")
                _uiState.value = LoginUiState.Error("Gagal sinkronisasi perangkat: ${e.message}")
            }
        }
    }

    private fun registerNotificationDevice(bearerToken: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result

                viewModelScope.launch {
                    notificationRepository.registerDevice(fcmToken, bearerToken)
                        .onSuccess {
                            Log.d("FCM", "Device berhasil terdaftar di Laravel")
                        }
                        .onFailure {
                            Log.e("FCM", "Gagal daftar device: ${it.message}")
                        }
                }
            }
        }
    }


    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}