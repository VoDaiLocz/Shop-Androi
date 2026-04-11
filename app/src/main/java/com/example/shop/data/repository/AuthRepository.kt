package com.example.shop.data.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<String>
    suspend fun signup(name: String, email: String, password: String): Result<String>
}
