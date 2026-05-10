package com.example.shop.data.repository

import com.example.shop.data.remote.api.UserApi
import com.example.shop.data.remote.dto.ApiUserResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApi: UserApi,
    private val authRepository: AuthRepository
) {
    suspend fun getUsers(): List<ApiUserResponse>? {
        val authorization = authRepository.getAuthorizationHeader() ?: return null
        return runCatching {
            userApi.getUsers(authorization)
        }.getOrNull()
    }

    suspend fun deleteUser(userId: Int): Boolean {
        val authorization = authRepository.getAuthorizationHeader() ?: return false
        return runCatching {
            userApi.deleteUser(authorization, userId)
            true
        }.getOrDefault(false)
    }
}
