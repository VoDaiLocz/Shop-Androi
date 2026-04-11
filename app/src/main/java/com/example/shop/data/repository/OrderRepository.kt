package com.example.shop.data.repository

import com.example.shop.data.model.Order

interface OrderRepository {
    suspend fun placeOrder(order: Order): Result<String>
    suspend fun getOrders(userId: String): List<Order>
}
