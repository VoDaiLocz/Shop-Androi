package com.example.shop.data.remote.api

import com.example.shop.data.remote.dto.CreateOrderRequest
import com.example.shop.data.remote.dto.OrderPaymentStatusResponse
import com.example.shop.data.remote.dto.OrderResponse
import com.example.shop.data.remote.dto.UpdateOrderStatusRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface OrderApi {
    @POST("api/orders")
    suspend fun createOrder(
        @Header("Authorization") authorization: String,
        @Body request: CreateOrderRequest
    ): OrderResponse

    @GET("api/orders/my")
    suspend fun getMyOrders(@Header("Authorization") authorization: String): List<OrderResponse>

    @GET("api/orders/{id}/payment-status")
    suspend fun getPaymentStatus(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): OrderPaymentStatusResponse

    @GET("api/orders")
    suspend fun getAllOrders(@Header("Authorization") authorization: String): List<OrderResponse>

    @PUT("api/orders/{id}/status")
    suspend fun updateStatus(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int,
        @Body request: UpdateOrderStatusRequest
    ): OrderResponse
}
