package com.example.shop.data.remote.dto

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class GoogleLoginRequest(
    val idToken: String
)

data class ApiUserResponse(
    val id: Int,
    val username: String,
    val email: String,
    val role: String
)

data class LoginResponse(
    val token: String,
    val user: ApiUserResponse
)
