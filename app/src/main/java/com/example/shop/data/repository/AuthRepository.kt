package com.example.shop.data.repository

import com.example.shop.data.model.User
import com.example.shop.data.remote.api.AuthApi
import com.example.shop.data.remote.dto.ApiUserResponse
import com.example.shop.data.remote.dto.GoogleLoginRequest
import com.example.shop.data.remote.dto.LoginRequest
import com.example.shop.data.remote.dto.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi
) {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _currentToken = MutableStateFlow<String?>(null)
    val currentToken: StateFlow<String?> = _currentToken.asStateFlow()

    suspend fun login(email: String, password: String): User? {
        return runCatching {
            val response = authApi.login(LoginRequest(email.trim(), password))
            saveSession(response.token, response.user)
        }.getOrNull()
    }

    suspend fun loginWithGoogle(idToken: String): User? {
        return runCatching {
            val response = authApi.googleLogin(GoogleLoginRequest(idToken))
            saveSession(response.token, response.user)
        }.getOrNull()
    }

    suspend fun register(user: User): Boolean {
        return runCatching {
            authApi.register(
                RegisterRequest(
                    username = user.username.trim(),
                    email = user.email.trim(),
                    password = user.password
                )
            )
            true
        }.getOrDefault(false)
    }

    fun logout() {
        _currentUser.value = null
        _currentToken.value = null
    }

    fun getAuthorizationHeader(): String? {
        return _currentToken.value?.let { token -> "Bearer $token" }
    }

    private fun saveSession(token: String, userResponse: ApiUserResponse): User {
        val user = userResponse.toUser()
        _currentToken.value = token
        _currentUser.value = user
        return user
    }

    private fun ApiUserResponse.toUser(): User {
        return User(
            id = id,
            username = username,
            email = email,
            password = "",
            role = role
        )
    }
}
