package com.example.shop.data.model

data class OrderItem(
    val orderItemId: Int = 0,
    val orderId: Int, // Cột này tham chiếu đến id của bảng Order
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val price: Double
)
