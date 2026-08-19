package com.rahmat.testapp.ui.publicscreen.auth.login

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: com.rahmat.testapp.domain.model.User) : LoginUiState()
    data class Error(val message: String?) : LoginUiState()
}