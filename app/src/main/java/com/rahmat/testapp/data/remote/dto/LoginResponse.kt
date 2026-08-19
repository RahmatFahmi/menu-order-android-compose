package com.rahmat.testapp.data.remote.dto

import com.rahmat.testapp.domain.model.User

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData?
)

data class LoginData(
    val token: String,
    val user: UserDto,
    val table_code: String? = null,
    val table_id: String?=null
)


