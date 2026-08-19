package com.rahmat.testapp.domain.repository

import com.rahmat.testapp.domain.model.User

interface AuthRepository {
    suspend fun login(loginId: String, pass: String) : Result<User>

    suspend fun loginGuest(tableCode: String, fcmToken: String) : Result<User>
}