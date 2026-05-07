package com.example.shop.data.model

data class User(
    val id: Int = 0,
    val username: String,
    val email: String,
    val password: String,
    val role: String = "USER"
)
