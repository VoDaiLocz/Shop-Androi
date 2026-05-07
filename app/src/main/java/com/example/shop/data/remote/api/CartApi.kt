package com.example.shop.data.remote.api

import com.example.shop.data.remote.dto.AddCartItemRequest
import com.example.shop.data.remote.dto.CartResponse
import com.example.shop.data.remote.dto.UpdateCartItemRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CartApi {
    @GET("api/cart")
    suspend fun getCart(@Header("Authorization") authorization: String): CartResponse

    @POST("api/cart/items")
    suspend fun addItem(
        @Header("Authorization") authorization: String,
        @Body request: AddCartItemRequest
    ): CartResponse

    @PUT("api/cart/items/{id}")
    suspend fun updateItem(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int,
        @Body request: UpdateCartItemRequest
    ): CartResponse

    @DELETE("api/cart/items/{id}")
    suspend fun deleteItem(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): CartResponse

    @DELETE("api/cart")
    suspend fun clearCart(@Header("Authorization") authorization: String)
}
