package com.rahmat.testapp.data.mapper

import com.rahmat.testapp.data.remote.dto.LoginResponse
import com.rahmat.testapp.domain.model.User

fun LoginResponse.toDomain(): User {
    val loginData = this.data
    val userData = loginData?.user
    return User(
        id = userData?.id ?: 0,
        name = userData?.name ?: "",
        username = userData?.username ?: "",
        role = userData?.role ?: "customer",
        token = loginData?.token ?: "",
        tabelCode = loginData?.table_code,
        tabelId = loginData?.table_id
    )
}