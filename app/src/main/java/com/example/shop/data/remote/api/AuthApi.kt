package com.example.shop.data.remote.api

import com.example.shop.data.remote.dto.ApiUserResponse
import com.example.shop.data.remote.dto.LoginRequest
import com.example.shop.data.remote.dto.LoginResponse
import com.example.shop.data.remote.dto.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiUserResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("api/auth/me")
    suspend fun me(@Header("Authorization") authorization: String): ApiUserResponse
}
