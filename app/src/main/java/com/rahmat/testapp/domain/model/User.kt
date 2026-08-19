package com.rahmat.testapp.domain.model

data class User(
    val id: Int,
    val name: String,
    val username: String,
    val role: String,
    val token: String,
    val tabelCode: String? = null,
    val tabelId: String? = null
)