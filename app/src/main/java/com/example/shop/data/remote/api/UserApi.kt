package com.example.shop.data.remote.api

import com.example.shop.data.remote.dto.ApiUserResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface UserApi {
    @GET("api/users")
    suspend fun getUsers(
        @Header("Authorization") authorization: String
    ): List<ApiUserResponse>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    )
}
